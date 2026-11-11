package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;

/** Visor del escenario 00. Las medidas del mapa usan una altura de jugador como unidad. */
public class TestLevelScreen extends ScreenAdapter {
    private final Game game;
    private final MenuScreen menu;
    private SpriteBatch batch;
    private Stage hud;
    private GameHud gameHud;
    private final TouchControls touch=new TouchControls();
    private BitmapFont font;
    private Label movementStatus, hookStatus, hookInputStatus, debugSummary;
    private Table helpPanel, debugPanel;
    private final HookControls hookControls = new HookControls();
    private float shotTime, shotX, shotY, shotEndX, shotEndY;
    private float aimX = 1, aimY, hookMissTimer;
    private float commandFeedbackTime;
    private String lastHookDirection = "SIN COMANDO";
    private String lastHookMode = "U / I";
    private Texture platformTexture, wallTexture, backgroundTexture, pixel;
    private RedesignSprite playerSprite;
    private ReadableLevelScene levelScene;
    private final RedesignAnimation playerAnimation=new RedesignAnimation();

    private TextureRegion platform, wall;
    private JsonValue map;
    private final OrthographicCamera camera = new OrthographicCamera();
    private InputMultiplexer input;
    private boolean overview;
    private float inspectionZoom = 1;
    private float cameraAheadX, cameraAheadY, motionZoom = 1;
    private float worldWidth, worldHeight;
    private String levelJsonPath;
    private final PlayerBody player = new PlayerBody();
    private final CombatSystem combat = new CombatSystem();
    private Label combatStatus, lifeStatus;
    private final PlayerVitals life = new PlayerVitals();
    private final EnemySprites enemySprites=new EnemySprites();
    private final EnemyAI enemyAI = new EnemyAI();
    private Color meleeColor, flyingColor;
    private float animationTime;
    private Color attackColor, auraColor;
    // Nivel 1 / guion: Salto Frame, Capataz Brutal, Matones y escenas.
    private final FrameJump frameJump = new FrameJump();
    private String frameUnlockFlag = "frame";
    /** Segundos que se ve la derrota antes de reiniciar solo (types.json, player.respawnDelay). */
    private float respawnDelay = 1.5f, deathTimer;
    private ForemanBoss foreman;
    private final StoryFoesRenderer foes = new StoryFoesRenderer();
    private final DialoguePlayer dialogue = new DialoguePlayer();
    private DialogueOverlay dialogueOverlay;
    private java.util.Map<String, java.util.List<DialoguePlayer.Line>> scenes = new java.util.HashMap<>();


    public TestLevelScreen(Game game, MenuScreen menu) {
        this(game, menu, "levels/section_1.json");
    }

    public TestLevelScreen(Game game, MenuScreen menu, String levelJsonPath) {
        this.game = game;
        this.menu = menu;
        this.levelJsonPath = levelJsonPath;
    }

    private SaveStore.Snapshot checkpoint;
    private java.util.Set<String> flags = new java.util.HashSet<>();
    private JsonValue arrival;
    private boolean arrivedByDoor;

    private boolean enterDoor(JsonValue door) {
        if (door == null || (door.has("requires") && !flags.contains(door.getString("requires")))) return false;
        float x = door.getFloat("x"), y = door.getFloat("y");
        if (player.x + PlayerBody.WIDTH <= x || player.x >= x + door.getFloat("width")
            || player.y + PlayerBody.HEIGHT <= y || player.y >= y + door.getFloat("height")) return false;
        if (door.has("grant")) flags.add(door.getString("grant"));
        String next = door.getString("next");
        if (next.equals("menu")) {
            returnToMenu();
        } else {
            recordRoom();
            TestLevelScreen destination = new TestLevelScreen(game, menu, next);
            destination.arrival = door.get("arrival");
            destination.arrivedByDoor = true;
            destination.worldHealth = worldHealth;
            destination.flags = flags;
            game.setScreen(destination);
            dispose();
        }
        return true;
    }
    private java.util.HashMap<String, Float> worldHealth = new java.util.HashMap<>();

    TestLevelScreen(Game game, MenuScreen menu, SaveStore.Snapshot checkpoint) {
        this(game, menu, checkpoint.level);
        this.checkpoint = checkpoint;
        this.worldHealth.putAll(checkpoint.enemies);
        java.util.Collections.addAll(flags, checkpoint.flags);
    }

    private final java.util.IdentityHashMap<CombatSystem.Enemy, String> enemyIds = new java.util.IdentityHashMap<>();

    private String legacyEnemyKey(CombatSystem.Enemy enemy) {
        return levelJsonPath + "/" + enemy.type.name() + "_" + enemy.spawnX + "_" + enemy.spawnY;
    }

    private void recordRoom() {
        for (CombatSystem.Enemy enemy : combat.enemies)
            worldHealth.put(enemyIds.get(enemy), enemy.health);
    }

    private String saveCheckpoint() {
        if (!life.alive() || player.y < 0) return "Busca un lugar seguro";
        SaveStore.Snapshot data = new SaveStore.Snapshot();
        data.version = SaveStore.CURRENT_VERSION;
        data.facing = player.getFacing() < 0 ? -1 : 1;
        data.level = levelJsonPath;
        data.x = player.x;
        data.y = player.y;
        data.health = life.getHealth();
        recordRoom();
        data.flags = flags.toArray(new String[0]);
        data.enemies = new java.util.HashMap<>(worldHealth);
        try {
            SaveStore.local().write(data);
            return "Guardado";
        } catch (Exception e) {
            Gdx.app.error("Guardado", "Error al escribir la partida", e);
            return "Error al guardar";
        }
    }

    @Override public void show() {
        batch = new SpriteBatch();
        map = new JsonReader().parse(Gdx.files.internal(levelJsonPath));
        worldWidth = map.getFloat("width");
        worldHeight = map.getFloat("height");
        if(map.getString("style", "").equals("readable")) levelScene=new ReadableLevelScene(map);
        for (JsonValue piece = map.get("pieces").child; piece != null; piece = piece.next) {
            player.addSolid(piece.getFloat("x"), piece.getFloat("y"), piece.getFloat("width"), piece.getFloat("height"), "pipe".equals(piece.getString("role", "")));
        }
        if (arrival == null) arrival = map.get("spawn");
        player.reset(arrival.getFloat("x"), arrival.getFloat("y"));
        JsonValue attacks = new JsonReader().parse(Gdx.files.internal("combat/prototype/attacks.json"));
        combat.duration=attacks.getFloat("duration"); combat.reach=attacks.getFloat("reach");
        combat.attackOriginHeight=attacks.getFloat("attackOriginHeight",PlayerBody.HEIGHT/2);
        combat.downReach=attacks.getFloat("downReach",-1);
        combat.whirlwindContactRadius=attacks.getFloat("whirlwindContactRadius",.75f);
        combat.baseDamage=attacks.getFloat("damage"); combat.auraMultiplier=attacks.getFloat("auraMultiplier");
        combat.stunDuration=attacks.getFloat("stunDuration",0.75f);
        combat.reboundMultiplier=attacks.getFloat("reboundMultiplier",0.85f);
        combat.bounceSpeed=attacks.getFloat("bounceSpeed",20);
        combat.hookRiseSpeed=attacks.getFloat("hookRiseSpeed",18);
        combat.thrustMinimum=attacks.getFloat("thrustMinimum",18);
        combat.thrustMaximum=attacks.getFloat("thrustMaximum",32);
        attackColor=Color.valueOf(attacks.getString("attackColor"));
        auraColor=Color.valueOf(attacks.getString("auraColor"));
        JsonValue enemyConfig=new JsonReader().parse(Gdx.files.internal("enemies/prototype/types.json"));
        life.maxHealth=enemyConfig.get("player").getFloat("health");
        life.invulnerabilityDuration=enemyConfig.get("player").getFloat("invulnerability");
        respawnDelay=enemyConfig.get("player").getFloat("respawnDelay",respawnDelay);
        loadEnemyProfile(enemyAI.robot,enemyConfig.get("robot"));
        loadEnemyProfile(enemyAI.scorp,enemyConfig.get("scorp"));
        loadEnemyProfile(enemyAI.melee,enemyConfig.get("melee"));
        loadEnemyProfile(enemyAI.flying,enemyConfig.get("flying"));
        if (enemyConfig.has("thug")) {
            loadEnemyProfile(enemyAI.thug,enemyConfig.get("thug"));
            enemyAI.fleeDuration=enemyConfig.get("thug").getFloat("fleeDuration",enemyAI.fleeDuration);
            enemyAI.fleeSpeedMultiplier=enemyConfig.get("thug").getFloat("fleeSpeedMultiplier",enemyAI.fleeSpeedMultiplier);
            if (enemyConfig.get("thug").has("tint")) enemySprites.thugTint.set(Color.valueOf(enemyConfig.get("thug").getString("tint")));
        }
        meleeColor=Color.valueOf(enemyConfig.get("melee").getString("color"));
        flyingColor=Color.valueOf(enemyConfig.get("flying").getString("color"));
        JsonValue targets = new JsonReader().parse(Gdx.files.internal("levels/test_combat_targets.json"));
        combat.enemies.clear();
        foreman = null;
        for(JsonValue e=(map.has("targets")?map.get("targets"):targets.get("targets")).child;e!=null;e=e.next) {
            CombatSystem.Enemy enemy = new CombatSystem.Enemy(e.getFloat("x"),e.getFloat("y"),e.getFloat("radius"),e.getFloat("health"),e.getBoolean("stunImmune",false),CombatSystem.EnemyType.valueOf(e.getString("type","DUMMY")));
            enemy.squad = e.getString("squad", "");
            enemy.fleeWhenSquadFalls = e.getBoolean("fleeWhenSquadFalls", false);
            if (enemy.type == CombatSystem.EnemyType.FOREMAN) {
                foreman = new ForemanBoss(enemy);
                loadForemanProfile(foreman, enemyConfig.get("foreman"));
            }
            combat.enemies.add(enemy);
            enemyIds.put(enemy, e.has("id") ? levelJsonPath + "/" + e.getString("id") : legacyEnemyKey(enemy));
        }
        combat.reset(); life.reset();
        if (checkpoint != null) {
            if (checkpoint.x >= 0 && checkpoint.x + PlayerBody.WIDTH <= worldWidth
                && checkpoint.y >= 0 && checkpoint.y <= worldHeight
                && player.isAreaFree(checkpoint.x, checkpoint.y, PlayerBody.WIDTH, PlayerBody.HEIGHT))
                player.restoreCheckpoint(checkpoint.x, checkpoint.y, checkpoint.facing);
            life.restoreHealth(checkpoint.health);
        }
        for (CombatSystem.Enemy enemy : combat.enemies) {
            String key = enemyIds.get(enemy);
            Float legacyHealth = worldHealth.remove(legacyEnemyKey(enemy));
            Float hp = worldHealth.containsKey(key) ? worldHealth.get(key) : legacyHealth;
            if (hp != null) enemy.health = Math.min(enemy.maxHealth, hp);
        }
        String root = "environments/industrial_v1/";
        String platformPath = root + "platform_metal_orange_v1.png";
        String wallPath = root + "wall_metal_conduit_v1.png";
        String bgPath = root + "background_hangar_v1.png";
        if (map.has("assets")) {
            JsonValue assets = map.get("assets");
            if (assets.has("platform")) platformPath = assets.getString("platform");
            if (assets.has("wall")) wallPath = assets.getString("wall");
            if (assets.has("background")) bgPath = assets.getString("background");
        }
        platformTexture = new Texture(Gdx.files.internal(platformPath));
        wallTexture = new Texture(Gdx.files.internal(wallPath));
        backgroundTexture = new Texture(Gdx.files.internal(bgPath));
        playerSprite=new RedesignSprite();
        playerAnimation.reset(life.getHealth());
        platform = trim(platformTexture);
        wall = trim(wallTexture);
        for (Texture texture : new Texture[]{platformTexture, wallTexture, backgroundTexture}) {
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        pixel = new Texture(p);
        p.dispose();
        createHud();
        setupStory();
        input = new InputMultiplexer(hud, new InputAdapter() {
            @Override public boolean scrolled(float x, float y) {
                if (!overview) inspectionZoom = MathUtils.clamp(inspectionZoom + y * 0.1f, 0.6f, 2f);
                return true;
            }
            @Override public boolean touchDragged(int x, int y, int pointer) {
                if (!overview && !gameHud.touchVisible() && !gameHud.isPaused()) {
                    camera.position.x -= Gdx.input.getDeltaX(pointer) * camera.viewportWidth * camera.zoom / Gdx.graphics.getWidth();
                    camera.position.y += Gdx.input.getDeltaY(pointer) * camera.viewportHeight * camera.zoom / Gdx.graphics.getHeight();
                }
                return true;
            }
        });
        Gdx.input.setInputProcessor(input);
        Gdx.input.setCatchKey(Input.Keys.BACK,true);
        camera.position.set(player.x, player.y + 3.5f, 0);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Elimina el margen transparente al dibujar, conservando intacto el PNG original. */
    private TextureRegion trim(Texture texture) {
        texture.getTextureData().prepare();
        Pixmap p = texture.getTextureData().consumePixmap();
        int minX = p.getWidth(), minY = p.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < p.getHeight(); y++) for (int x = 0; x < p.getWidth(); x++) {
            if ((p.getPixel(x, y) & 255) > 32) {
                minX = Math.min(minX, x); minY = Math.min(minY, y);
                maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
            }
        }
        if (texture.getTextureData().disposePixmap()) p.dispose();
        return maxX < 0 ? new TextureRegion(texture) : new TextureRegion(texture, minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /** Guion del nivel 1: escenas, desbloqueo del Salto Frame y estado del Capataz. */
    private void setupStory() {
        if (Gdx.files.internal("combat/prototype/frame_jump.json").exists()) {
            JsonValue fj = new JsonReader().parse(Gdx.files.internal("combat/prototype/frame_jump.json"));
            frameJump.contactMargin = fj.getFloat("contactMargin", frameJump.contactMargin);
            frameJump.exitGap = fj.getFloat("exitGap", frameJump.exitGap);
            frameJump.intangibility = fj.getFloat("intangibility", frameJump.intangibility);
            frameJump.slowDuration = fj.getFloat("slowDuration", frameJump.slowDuration);
            frameJump.slowScale = fj.getFloat("slowScale", frameJump.slowScale);
            frameUnlockFlag = fj.getString("unlockFlag", frameUnlockFlag);
        }
        JsonValue story = map.get("story");
        boolean mapUnlocksFrame = false;
        if (story != null) for (JsonValue t = story.child; t != null; t = t.next)
            if (frameUnlockFlag.equals(t.getString("grant", ""))) mapUnlocksFrame = true;
        // Fuera del mapa que lo desbloquea (otras salas, escenario 00), el Salto Frame ya se tiene.
        if (!mapUnlocksFrame) flags.add(frameUnlockFlag);
        if (story != null && Gdx.files.internal(map.getString("storyFile", "story/nivel_1.json")).exists())
            scenes = DialoguePlayer.parse(new JsonReader().parse(Gdx.files.internal(map.getString("storyFile", "story/nivel_1.json"))));
        boolean freshStart = checkpoint == null && !arrivedByDoor;
        if (story != null) for (JsonValue t = story.child; t != null; t = t.next) {
            String on = t.getString("on", "zone");
            // Partidas cargadas o llegadas por puerta: lo ya recorrido no se repite.
            boolean passed = on.equals("start") ? !freshStart
                : on.equals("zone") && !freshStart && player.x > t.getFloat("x") + t.getFloat("width");
            if (passed && !flags.contains(StoryTriggers.flagFor(t.getString("scene", "")))) StoryTriggers.consume(t, flags);
        }
        if (foreman != null) {
            foreman.reset();
            if (!foreman.body.alive()) flags.add("capataz");
        }
        frameJump.reset();
        frameJump.enabled = flags.contains(frameUnlockFlag);
        dialogueOverlay = new DialogueOverlay(hud, pixel, font, dialogue, this::advanceDialogue, this::skipDialogue);
        if (freshStart) playStory("start");
    }
    private void playStory(String event) {
        JsonValue trigger = StoryTriggers.find(map.get("story"), flags, event,
            player.x + PlayerBody.WIDTH / 2, player.y + PlayerBody.HEIGHT / 2);
        if (trigger == null) return;
        StoryTriggers.consume(trigger, flags);
        frameJump.enabled = flags.contains(frameUnlockFlag);
        java.util.List<DialoguePlayer.Line> lines = scenes.get(trigger.getString("scene"));
        if (lines == null || lines.isEmpty()) return;
        gameHud.closePanels(); // cancela toques activos: ningun boton queda mantenido
        dialogue.start(trigger.getString("scene"), lines);
    }
    private void advanceDialogue() { dialogue.advance(); if (!dialogue.isActive()) gameHud.cancelTouches(); }
    private void skipDialogue() { dialogue.skip(); gameHud.cancelTouches(); }
    private void loadForemanProfile(ForemanBoss boss, JsonValue d) {
        if (d == null) return;
        boss.aggroRange=d.getFloat("aggroRange",boss.aggroRange); boss.telegraph=d.getFloat("telegraph",boss.telegraph);
        boss.chargeSpeed=d.getFloat("chargeSpeed",boss.chargeSpeed); boss.chargeDistance=d.getFloat("chargeDistance",boss.chargeDistance);
        boss.exposedTime=d.getFloat("exposedTime",boss.exposedTime); boss.recoverTime=d.getFloat("recoverTime",boss.recoverTime);
        boss.chargeDamage=d.getFloat("chargeDamage",boss.chargeDamage); boss.slamWindup=d.getFloat("slamWindup",boss.slamWindup);
        boss.waveSpeed=d.getFloat("waveSpeed",boss.waveSpeed); boss.waveRange=d.getFloat("waveRange",boss.waveRange);
        boss.waveHeight=d.getFloat("waveHeight",boss.waveHeight); boss.waveDamage=d.getFloat("waveDamage",boss.waveDamage);
        boss.cycles=d.getInt("cycles",boss.cycles);
    }

    private void createHud() {
        font=new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        gameHud=new GameHud(pixel,font,touch,()->restartLevel(),()->returnToMenu(),()->overview=!overview,this::saveCheckpoint);
        hud=gameHud.stage;
        lifeStatus=gameHud.lifeStatus;movementStatus=gameHud.movementStatus;hookStatus=gameHud.hookStatus;
        combatStatus=gameHud.combatStatus;hookInputStatus=gameHud.hookInputStatus;debugSummary=gameHud.debugSummary;
        helpPanel=gameHud.helpPanel;debugPanel=gameHud.debugPanel;
    }
    private void toggleHelp() { gameHud.toggleHelp(); }
    private void toggleDebug() { gameHud.toggleDebug(); }
    private void restartLevel() {
        gameHud.cancelTouches();hookControls.reset();combat.reset();life.reset();shotTime=hookMissTimer=commandFeedbackTime=0;
        cameraAheadX=cameraAheadY=0;motionZoom=1;overview=false;inspectionZoom=1;
        player.reset(arrival.getFloat("x"), arrival.getFloat("y"));
        playerAnimation.reset(life.getHealth());
        frameJump.reset();
        if (foreman != null) foreman.reset();
        deathTimer = 0;
    }
    private void updateGame(float delta) {
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A)||touch.aimX()<0;
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D)||touch.aimX()>0;
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W)||touch.aimY()>0;
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S)||touch.aimY()<0;
        float direction=MathUtils.clamp((Gdx.input.isKeyPressed(Input.Keys.D)?1:0)
            -(Gdx.input.isKeyPressed(Input.Keys.A)?1:0)+touch.moveX(),-1,1);
        boolean dash=Gdx.input.isKeyJustPressed(Input.Keys.J)||touch.pressed(TouchControls.Action.DASH);
        boolean sprintHeld=Gdx.input.isKeyPressed(Input.Keys.J)||touch.held(TouchControls.Action.DASH);
        boolean jump=Gdx.input.isKeyJustPressed(Input.Keys.H)||touch.pressed(TouchControls.Action.JUMP);
        boolean jumpHeld=Gdx.input.isKeyPressed(Input.Keys.H)||touch.held(TouchControls.Action.JUMP);
        boolean propulsionHeld=Gdx.input.isKeyPressed(Input.Keys.U)||touch.held(TouchControls.Action.PROPULSION);
        boolean pendulumHeld=Gdx.input.isKeyPressed(Input.Keys.I)||touch.held(TouchControls.Action.PENDULUM);
        HookControls.Command hookCommand = hookControls.update(delta,left,right,up,down,
            Gdx.input.isKeyJustPressed(Input.Keys.U)||touch.pressed(TouchControls.Action.PROPULSION),propulsionHeld,
            Gdx.input.isKeyJustPressed(Input.Keys.I)||touch.pressed(TouchControls.Action.PENDULUM),pendulumHeld,player.getFacing());
        boolean fireHook = life.alive() && hookCommand != HookControls.Command.NONE;
        boolean firePendulum = hookCommand == HookControls.Command.PENDULUM;
        aimX = hookControls.getAimX(); aimY = hookControls.getAimY();
        shotTime = Math.max(0, shotTime - delta);
        commandFeedbackTime = Math.max(0, commandFeedbackTime - delta);
        if (fireHook) {
            lastHookDirection = hookDirectionName(aimX, aimY);
            commandFeedbackTime = 1.5f;
            lastHookMode = firePendulum ? "I" : "U";
            boolean pulled = !firePendulum && combat.tryPull(player,aimX,aimY);
            boolean attached = pulled || (firePendulum ? player.launchPendulum(aimX,aimY)
                : player.launchPropulsion(aimX,aimY));
            hookMissTimer = attached ? 0 : 1.5f;
            shotTime = 0.22f;
            shotX = player.x + PlayerBody.WIDTH / 2;
            shotY = player.y + PlayerBody.HEIGHT / 2;
            float length = (float) Math.hypot(aimX, aimY);
            shotEndX = attached ? player.getAnchorX() : shotX + aimX / length * PlayerBody.HOOK_RANGE;
            shotEndY = attached ? player.getAnchorY() : shotY + aimY / length * PlayerBody.HOOK_RANGE;
            if(pulled) { shotEndX=combat.getPulledEnemy().x; shotEndY=combat.getPulledEnemy().y; }
            Gdx.app.log("GANCHO", lastHookMode + " / " + lastHookDirection + (attached ? " / ANCLADO" : " / SIN SUPERFICIE ALCANZABLE"));
        }
        // Soltar I solo libera el pendulo; nunca cancela la propulsion activada con U.
        if (!pendulumHeld && !firePendulum) player.releaseHook();
        hookInputStatus.setText("ENTRADAS: " + (left ? "A " : "") + (right ? "D " : "")
            + (up ? "W " : "") + (down ? "S " : "")
            + (jumpHeld ? "H " : "") + (sprintHeld ? "J " : "")
            + (Gdx.input.isKeyPressed(Input.Keys.K)||touch.held(TouchControls.Action.ATTACK) ? "K " : "") + (propulsionHeld ? "U " : "") + (pendulumHeld ? "I" : "")
            + "\nULTIMO: " + lastHookMode + " / " + lastHookDirection);
        // Subpasos para que una estocada rapida no atraviese objetivos entre fotogramas.
        float frameDelta=MathUtils.clamp(delta,0,0.25f);
        int steps=Math.max(1,(int)Math.ceil(frameDelta*240));
        boolean attackPressed=Gdx.input.isKeyJustPressed(Input.Keys.K)||touch.pressed(TouchControls.Action.ATTACK);
        boolean attackHeld=Gdx.input.isKeyPressed(Input.Keys.K)||touch.held(TouchControls.Action.ATTACK);
        for(int step=0;step<steps;step++) {
            float dt=frameDelta/steps;
            life.update(dt);
            boolean alive=life.alive();
            player.update(dt,alive?direction:0,alive && step==0 && jump,
                alive && step==0 && dash,alive && sprintHeld,alive && jumpHeld);
            if(alive) frameJump.update(player,life,combat.enemies);
            if(alive) combat.update(dt,player,step==0 && attackPressed,attackHeld,up&&!down,down&&!up);
            else { combat.attack=CombatSystem.Attack.NONE;player.cancelCombatTravel(); }
            SteamVent.contact(map.get("vents"),animationTime+(step+1)*dt,player,life);
            enemyAI.update(dt,player,life,combat.enemies);
            if(foreman!=null) foreman.update(dt,player,life);
            playerAnimation.update(dt,player,combat,life,shotTime);
        }
        updateStoryEvents();
        combatStatus.setText(combat.auraRemaining>0 ? "AURA x"+combat.auraMultiplier : attackName());
        if(player.hasAttackAirJump()) combatStatus.setText("SALTO EXTRA LISTO");
        lifeStatus.setText("INTEGRIDAD");
        lifeStatus.setColor(life.getHealth()<=life.maxHealth*.3f?Color.SALMON:Color.WHITE);
        if(!life.alive()) combatStatus.setText("DERROTADO / PAUSA > REINICIAR");
        animationTime+=Math.min(delta,.25f);
        if (!pendulumHeld && firePendulum) player.releaseHook();
        hookMissTimer = Math.max(0, hookMissTimer - delta);
        hookStatus.setText(player.isPropelling() ? "PROPULSION"
            : player.isHooked() ? (player.isReeling() ? "ACERCANDO CUERDA" : "PENDULO")
            : commandFeedbackTime > 0 ? (lastHookMode + " | " + lastHookDirection + (hookMissTimer > 0 ? ": VACIO" : ": OK"))
            : "GANCHO LISTO");
        hookStatus.setColor(hookMissTimer > 0 && !player.isHooked()
            ? Color.valueOf("FFAB57") : Color.CYAN);
        movementStatus.setText(player.isDashing() ? "DASH"
            : player.isBoosted() ? "SPRINT x2.3"
            : player.canDash() ? "DASH LISTO" : "RECARGANDO");
        movementStatus.setColor(player.canDash() ? Color.valueOf("FFE066") : Color.valueOf("FFAB57"));
        if (debugPanel.isVisible()) {
            debugSummary.setText(String.format(java.util.Locale.ROOT,
                "Velocidad: %.1f\nGancho: %.0f / Ayuda: %.2f\nCuerda: %.1f / Contactos: %d\nDash: %s / Camara: %s\nSuelo: %s / Salto en espera: %s",
                Math.hypot(player.getVelocityX(), player.getVelocityY()),
                PlayerBody.HOOK_RANGE, PlayerBody.HOOK_ASSIST_RADIUS,
                player.isHooked() ? player.getRopeUsedLength() : 0,
                player.isHooked() ? Math.max(0, player.getRopePointCount() - 1) : 0,
                player.canDash() ? "listo" : "recargando", overview ? "general" : "seguimiento",
                player.isGrounded() ? "SI" : "NO", player.isJumpQueued() ? "SI" : "NO"));
        }
        if (player.y < -10) restartLevel();
        // Al morir se ve la derrota un momento y el nivel se reinicia solo (R sigue funcionando).
        if (!life.alive()) { deathTimer += delta; if (deathTimer >= respawnDelay) restartLevel(); }
        else deathTimer = 0;
        touch.syncSwing(player.isHooked(),firePendulum);
        touch.endFrame();
    }
    private void updateStoryEvents() {
        if (foreman != null && foreman.consumeDefeat()) { flags.add("capataz"); playStory("boss"); }
        if (dialogue.isActive()) return;
        for (CombatSystem.Enemy e : combat.enemies) if (e.fleeing && !e.fled) { playStory("flee"); break; }
        if (!dialogue.isActive()) playStory("zone");
    }
    /** Durante una escena la simulacion se detiene como en la pausa. Devuelve true si la hay. */
    private boolean updateDialogue(float realDelta) {
        if (!dialogue.isActive()) { if (dialogueOverlay != null) dialogueOverlay.update(realDelta); return false; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACK)) skipDialogue();
        else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isKeyJustPressed(Input.Keys.K) || Gdx.input.isKeyJustPressed(Input.Keys.H)) advanceDialogue();
        dialogue.update(realDelta);
        dialogueOverlay.update(realDelta);
        touch.endFrame();
        return true;
    }

    @Override public void render(float delta) {
        float hudDelta=delta;
        boolean inScene=updateDialogue(delta);
        // Bullet-time del Salto Frame: escala solo la simulacion; el HUD sigue en tiempo real.
        frameJump.updateRealTime(delta);
        float simDelta=delta*frameJump.timeScale();
        if(!inScene && (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)||Gdx.input.isKeyJustPressed(Input.Keys.BACK))) gameHud.togglePause();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F1)) toggleHelp();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)) toggleDebug();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F3)) gameHud.toggleTouch();
        if(Gdx.input.isKeyJustPressed(Input.Keys.M)) overview=!overview;
        if(!inScene && Gdx.input.isKeyJustPressed(Input.Keys.R)) { gameHud.closePanels();restartLevel(); }
        if(inScene) delta=0;
        else if(!gameHud.isPaused()) { updateGame(simDelta); delta=simDelta; }
        else { touch.endFrame();delta=0; }
        gameHud.update(player,life,combat);
        if (!inScene && !gameHud.isPaused() && life.alive() && menu != null) {
            if (enterDoor(map.get("exit"))) return;
            JsonValue portals = map.get("portals");
            if (portals != null)
                for (JsonValue door = portals.child; door != null; door = door.next)
                    if (enterDoor(door)) return;
        }

        if (!overview) {
            float frame = Math.min(delta,0.1f);
            float anticipation = 1f-(float)Math.exp(-5f*frame);
            float maxAhead = Math.min(3.5f,camera.viewportWidth*0.18f);
            float desiredX = MathUtils.clamp(player.getVelocityX()*0.13f,-maxAhead,maxAhead);
            float desiredY = MathUtils.clamp(player.getVelocityY()*0.08f,-1f,2f);
            cameraAheadX = MathUtils.lerp(cameraAheadX,desiredX,anticipation);
            cameraAheadY = MathUtils.lerp(cameraAheadY,desiredY,anticipation);
            float follow = 1f-(float)Math.exp(-8f*frame);
            camera.position.x = MathUtils.lerp(camera.position.x,player.x+PlayerBody.WIDTH/2f+cameraAheadX,follow);
            camera.position.y = MathUtils.lerp(camera.position.y,player.y+3.5f+cameraAheadY,follow);
            float speed = (float)Math.hypot(player.getVelocityX(),player.getVelocityY());
            float desiredZoom = 1f+0.10f*MathUtils.clamp((speed-16f)/16f,0,1);
            motionZoom = MathUtils.lerp(motionZoom,desiredZoom,1f-(float)Math.exp(-3f*frame));
        }
        camera.zoom = overview ? Math.max((worldWidth+8)/camera.viewportWidth,(worldHeight+8)/camera.viewportHeight)
            : inspectionZoom*motionZoom;        float halfW = camera.viewportWidth * camera.zoom / 2;
        float halfH = camera.viewportHeight * camera.zoom / 2;
        camera.position.x = halfW * 2 >= worldWidth + 6 ? worldWidth / 2 : MathUtils.clamp(camera.position.x, -3 + halfW, worldWidth + 3 - halfW);
        camera.position.y = halfH * 2 >= worldHeight + 4 ? worldHeight / 2 - 1 : MathUtils.clamp(camera.position.y, -2 + halfH, worldHeight + 2 - halfH);
        camera.update();
        ScreenUtils.clear(0.025f, 0.04f, 0.07f, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        if(levelScene!=null) levelScene.draw(batch,pixel,map,camera,animationTime,flags,player.x,player.y);
        else {
        float bgScale = Math.max(halfW * 2 / backgroundTexture.getWidth(), halfH * 2 / backgroundTexture.getHeight()) * 1.12f;
        float bgW = backgroundTexture.getWidth() * bgScale, bgH = backgroundTexture.getHeight() * bgScale;
        if(map.getBoolean("artwork",false)) batch.draw(backgroundTexture,0,0,worldWidth,worldHeight);
        else batch.draw(backgroundTexture, camera.position.x - bgW / 2, camera.position.y - bgH / 2, bgW, bgH);
        for (JsonValue piece = map.get("pieces").child; piece != null; piece = piece.next) {
            if(map.getBoolean("artwork",false)) continue;
            TextureRegion image = piece.getString("asset").equals("wall") ? wall : platform;
            batch.draw(image, piece.getFloat("x"), piece.getFloat("y"), piece.getFloat("width"), piece.getFloat("height"));
        }
        }
        drawCombatTargets();
        foes.begin(batch,pixel);
        if(foreman!=null) foes.drawWave(foreman,animationTime);
        foes.drawFrameJump(frameJump,animationTime);
        float centerX = player.x + PlayerBody.WIDTH / 2, centerY = player.y + PlayerBody.HEIGHT / 2;
        playerSprite.prepare(playerAnimation,player,aimX,aimY);
        float handX=playerSprite.getHandX(),handY=playerSprite.getHandY();
        if(combat.isPulling()) {
            batch.setColor(1,0.7f,0.15f,0.9f);
            drawLine(handX,handY,combat.getPulledEnemy().x,combat.getPulledEnemy().y,0.06f);
        }
        if (shotTime > 0) {
            batch.setColor(1f, 0.55f, 0.15f, shotTime / 0.22f);
            drawLine(handX, handY, shotEndX, shotEndY, 0.045f);
        }
        if (player.isPropelling()) {
            batch.setColor(1f,0.72f,0.12f,0.85f);
            drawLine(handX,handY,player.getAnchorX(),player.getAnchorY(),0.06f);
            batch.draw(pixel,player.getAnchorX()-0.12f,player.getAnchorY()-0.12f,0.24f,0.24f);
        } else if (player.isHooked()) {
            batch.setColor(0.1f, 0.9f, 1f, 1);
            float ropeX = handX, ropeY = handY;
            for (int i = 0; i < player.getRopePointCount(); i++) {
                float nextX = player.getRopePointX(i), nextY = player.getRopePointY(i);
                drawLine(ropeX, ropeY, nextX, nextY, 0.055f);
                ropeX = nextX; ropeY = nextY;
            }
            batch.draw(pixel, player.getAnchorX() - 0.12f, player.getAnchorY() - 0.12f, 0.24f, 0.24f);
        } else {
            float aimLength = (float) Math.hypot(aimX, aimY);
            PlayerBody.HookTarget target = player.findHookTarget(aimX, aimY);
            float targetX = centerX + aimX / aimLength * 5f;
            float targetY = centerY + aimY / aimLength * 5f;
            if (target != null) {
                targetX = target.probeX; targetY = target.probeY;
                if (target.assisted) batch.setColor(0.25f, 1f, 0.45f, 0.7f);
                else batch.setColor(0.1f, 0.9f, 1f, 0.65f);
                drawLine(targetX, targetY, target.x, target.y, 0.035f);
                batch.draw(pixel, target.x - 0.10f, target.y - 0.10f, 0.20f, 0.20f);
            } else batch.setColor(1f, 0.55f, 0.15f, 0.45f);
            drawCircle(targetX, targetY, PlayerBody.HOOK_ASSIST_RADIUS);
            drawLine(targetX - 0.12f, targetY, targetX + 0.12f, targetY, 0.035f);
            drawLine(targetX, targetY - 0.12f, targetX, targetY + 0.12f, 0.035f);
        }
        batch.setColor(Color.WHITE);
        if(life.isInvulnerable()) batch.setColor(1,0.45f,0.45f,0.6f+0.4f*Math.abs(MathUtils.sin(animationTime*22)));
        if(!life.alive()) batch.setColor(0.45f,0.45f,0.45f,0.8f);
        playerSprite.draw(batch);
        drawAttackEffects(centerX,centerY);
        if(levelScene!=null && !overview) levelScene.foreground(batch,pixel,camera);
        batch.setColor(Color.WHITE);
        batch.end();
        hud.getViewport().apply();
        hud.act(hudDelta);
        hud.draw();
    }

    private void loadEnemyProfile(EnemyAI.Profile profile,JsonValue data) {
        profile.speed=data.getFloat("speed");profile.detectionRange=data.getFloat("detectionRange");
        profile.attackRange=data.getFloat("attackRange");profile.damage=data.getFloat("damage");
        profile.windup=data.getFloat("windup");profile.recovery=data.getFloat("recovery");
    }
    private String attackName() {
        switch(combat.attack) {
            case SWEEP:return "BARRIDO"; case UP:return "SUPERIOR";
            case DOWN:return "INFERIOR"; case THRUST:return "ESTOCADA";
            case HOOK_STRIKE:return "GOLPE AEREO"; case WHIRLWIND:return "TORBELLINO";
            case AURA:return "AURA"; default:return "ATAQUE";
        }
    }
    private void drawCombatTargets() {
        enemySprites.fleeDuration=enemyAI.fleeDuration;
        for(CombatSystem.Enemy e:combat.enemies) enemySprites.draw(batch,e,animationTime);
        foes.begin(batch,pixel);
        if(foreman!=null) foes.drawForeman(foreman,animationTime);
        for(CombatSystem.Enemy e:combat.enemies) if(e.alive() && e.type!=CombatSystem.EnemyType.FOREMAN) {
            batch.setColor(e.flash>0?Color.WHITE:e.isStunned()?Color.valueOf("F4B95D"):e.type==CombatSystem.EnemyType.FLYING?flyingColor:e.type==CombatSystem.EnemyType.MELEE?meleeColor:Color.valueOf("CE568B"));
            if(e.isStunned()) {
                drawArc(e.x,e.y,e.radius+0.18f,0,MathUtils.PI2*e.getStunRemaining()/Math.max(0.001f,combat.stunDuration));
            }
            if(e.state==CombatSystem.EnemyState.WINDUP) {
                batch.setColor(1,0.35f,0.1f,0.9f);
                drawCircle(e.x,e.y,e.radius+0.3f);
            }
            if(e.type==CombatSystem.EnemyType.ROBOT || e.type==CombatSystem.EnemyType.SCORP || e.type==CombatSystem.EnemyType.THUG) {
                // Animated sprites are drawn separately.
            } else if(e.type==CombatSystem.EnemyType.FLYING) {
                drawCircle(e.x,e.y,e.radius);
                float flap=MathUtils.sin(animationTime*12)*e.radius*.6f;
                drawLine(e.x-e.radius*1.8f,e.y+flap,e.x-e.radius*.4f,e.y,0.12f);
                drawLine(e.x+e.radius*.4f,e.y,e.x+e.radius*1.8f,e.y+flap,0.12f);
                batch.draw(pixel,e.x-e.radius*.4f,e.y-e.radius*.4f,e.radius*.8f,e.radius*.8f);
            } else {
                batch.draw(pixel,e.x-e.radius*.7f,e.y-e.radius*.6f,e.radius*1.4f,e.radius*1.4f);
                drawLine(e.x-e.radius*.4f,e.y-e.radius*.5f,e.x-e.radius*.5f,e.y-e.radius,0.15f);
                drawLine(e.x+e.radius*.4f,e.y-e.radius*.5f,e.x+e.radius*.5f,e.y-e.radius,0.15f);
                drawLine(e.x,e.y,e.x+e.facing*e.radius*1.4f,e.y,0.15f);
            }
            batch.setColor(0.12f,0.04f,0.07f,1);
            batch.draw(pixel,e.x-e.radius,e.y+e.radius+0.15f,e.radius*2,0.1f);
            batch.setColor(0.4f,1,0.5f,1);
            batch.draw(pixel,e.x-e.radius,e.y+e.radius+0.15f,e.radius*2*e.health/e.maxHealth,0.1f);
        }
    }
    /** Efectos procedurales reutilizables: sus medidas y colores viven en assets/combat. */
    private void drawAttackEffects(float cx,float cy) {
        if(combat.auraRemaining>0) {
            batch.setColor(auraColor.r,auraColor.g,auraColor.b,0.16f);
            for(int i=0;i<4;i++) drawCircle(cx,cy,0.65f+i*0.10f);
        }
        // La hoja y los barridos están en los sprites; el aura persiste al moverse.
        batch.setColor(Color.WHITE);
    }
    private void drawArc(float cx,float cy,float radius,float start,float span) {
        for(int i=0;i<20;i++) {
            float a=start+span*i/20,b=start+span*(i+1)/20;
            drawLine(cx+MathUtils.cos(a)*radius,cy+MathUtils.sin(a)*radius,
                cx+MathUtils.cos(b)*radius,cy+MathUtils.sin(b)*radius,0.065f);
        }
    }

    private String hookDirectionName(float dx, float dy) {
        if (dy > 0) return dx < 0 ? "ARR-IZQ" : dx > 0 ? "ARR-DER" : "ARRIBA";
        if (dy < 0) return dx < 0 ? "ABA-IZQ" : dx > 0 ? "ABA-DER" : "ABAJO";
        return dx < 0 ? "IZQUIERDA" : "DERECHA";
    }

    private void drawCircle(float cx, float cy, float radius) {
        int segments = 40;
        for (int i = 0; i < segments; i++) {
            double a = i * Math.PI * 2 / segments, b = (i + 1) * Math.PI * 2 / segments;
            drawLine(cx + (float) Math.cos(a) * radius, cy + (float) Math.sin(a) * radius,
                cx + (float) Math.cos(b) * radius, cy + (float) Math.sin(b) * radius, 0.035f);
        }
    }

    private void drawLine(float x1, float y1, float x2, float y2, float thickness) {
        float dx = x2 - x1, dy = y2 - y1;
        batch.draw(pixel, x1, y1 - thickness / 2, 0, thickness / 2,
            (float) Math.hypot(dx, dy), thickness, 1, 1,
            (float) Math.toDegrees(Math.atan2(dy, dx)), 0, 0, 1, 1, false, false);
    }

    private void returnToMenu() {
        game.setScreen(menu);
        dispose();
    }

    @Override public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        camera.viewportHeight = 24;
        camera.viewportWidth = 24f * width / height;
        if(gameHud!=null)gameHud.resize(width,height);
    }
    @Override public void pause() {
        if(gameHud!=null) { gameHud.closePanels();gameHud.togglePause(); }
    }
    @Override public void hide() {
        if(gameHud!=null)gameHud.cancelTouches();
        Gdx.input.setCatchKey(Input.Keys.BACK,false);
        if (Gdx.input.getInputProcessor() == input) Gdx.input.setInputProcessor(null);
    }
    @Override public void dispose() {
        enemySprites.dispose();
        foes.dispose();
        if(levelScene!=null){levelScene.dispose();levelScene=null;}
        hide();
        if (hud != null) { hud.dispose(); hud = null; }
        if (batch != null) { batch.dispose(); batch = null; }
        if (font != null) { font.dispose(); font = null; }
        if (platformTexture != null) { platformTexture.dispose(); platformTexture = null; }
        if (wallTexture != null) { wallTexture.dispose(); wallTexture = null; }
        if (backgroundTexture != null) { backgroundTexture.dispose(); backgroundTexture = null; }
        if(playerSprite!=null){playerSprite.dispose();playerSprite=null;}
        if (pixel != null) { pixel.dispose(); pixel = null; }
    }
}
