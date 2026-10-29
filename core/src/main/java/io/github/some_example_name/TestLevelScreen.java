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
    private final EnemyAI enemyAI = new EnemyAI();
    private Color meleeColor, flyingColor;
    private float animationTime;
    private Color attackColor, auraColor;


    public TestLevelScreen(Game game, MenuScreen menu) {
        this(game, menu, "levels/section_1.json");
    }

    public TestLevelScreen(Game game, MenuScreen menu, String levelJsonPath) {
        this.game = game;
        this.menu = menu;
        this.levelJsonPath = levelJsonPath;
    }

    @Override public void show() {
        batch = new SpriteBatch();
        map = new JsonReader().parse(Gdx.files.internal(levelJsonPath));
        worldWidth = map.getFloat("width");
        worldHeight = map.getFloat("height");
        for (JsonValue piece = map.get("pieces").child; piece != null; piece = piece.next) {
            player.addSolid(piece.getFloat("x"), piece.getFloat("y"), piece.getFloat("width"), piece.getFloat("height"));
        }
        player.reset(map.get("spawn").getFloat("x"), map.get("spawn").getFloat("y"));
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
        loadEnemyProfile(enemyAI.melee,enemyConfig.get("melee"));
        loadEnemyProfile(enemyAI.flying,enemyConfig.get("flying"));
        meleeColor=Color.valueOf(enemyConfig.get("melee").getString("color"));
        flyingColor=Color.valueOf(enemyConfig.get("flying").getString("color"));
        JsonValue targets = new JsonReader().parse(Gdx.files.internal("levels/test_combat_targets.json"));
        combat.enemies.clear();
        for(JsonValue e=targets.get("targets").child;e!=null;e=e.next)
            combat.enemies.add(new CombatSystem.Enemy(e.getFloat("x"),e.getFloat("y"),e.getFloat("radius"),e.getFloat("health"),e.getBoolean("stunImmune",false),CombatSystem.EnemyType.valueOf(e.getString("type","DUMMY"))));
        combat.reset(); life.reset();
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
        camera.position.set(worldWidth / 2f, 7, 0);
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

    private void createHud() {
        font=new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        gameHud=new GameHud(pixel,font,touch,()->restartLevel(),()->returnToMenu(),()->overview=!overview);
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
        player.reset(map.get("spawn").getFloat("x"),map.get("spawn").getFloat("y"));
        playerAnimation.reset(life.getHealth());

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
            if(alive) combat.update(dt,player,step==0 && attackPressed,attackHeld,up&&!down,down&&!up);
            else { combat.attack=CombatSystem.Attack.NONE;player.cancelCombatTravel(); }
            enemyAI.update(dt,player,life,combat.enemies);
            playerAnimation.update(dt,player,combat,life,shotTime);
        }
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
        touch.syncSwing(player.isHooked(),firePendulum);
        touch.endFrame();
    }
    @Override public void render(float delta) {
        float hudDelta=delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)||Gdx.input.isKeyJustPressed(Input.Keys.BACK)) gameHud.togglePause();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F1)) toggleHelp();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F2)) toggleDebug();
        if(Gdx.input.isKeyJustPressed(Input.Keys.F3)) gameHud.toggleTouch();
        if(Gdx.input.isKeyJustPressed(Input.Keys.M)) overview=!overview;
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) { gameHud.closePanels();restartLevel(); }
        if(!gameHud.isPaused()) updateGame(delta);
        else { touch.endFrame();delta=0; }
        gameHud.update(player,life,combat);

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
        float bgScale = Math.max(halfW * 2 / backgroundTexture.getWidth(), halfH * 2 / backgroundTexture.getHeight()) * 1.12f;
        float bgW = backgroundTexture.getWidth() * bgScale, bgH = backgroundTexture.getHeight() * bgScale;
        batch.draw(backgroundTexture, camera.position.x - bgW / 2, camera.position.y - bgH / 2, bgW, bgH);
        for (JsonValue piece = map.get("pieces").child; piece != null; piece = piece.next) {
            TextureRegion image = piece.getString("asset").equals("wall") ? wall : platform;
            batch.draw(image, piece.getFloat("x"), piece.getFloat("y"), piece.getFloat("width"), piece.getFloat("height"));
        }
        drawCombatTargets();
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
        for(CombatSystem.Enemy e:combat.enemies) if(e.alive()) {
            batch.setColor(e.flash>0?Color.WHITE:e.isStunned()?Color.valueOf("F4B95D"):e.type==CombatSystem.EnemyType.FLYING?flyingColor:e.type==CombatSystem.EnemyType.MELEE?meleeColor:Color.valueOf("CE568B"));
            if(e.isStunned()) {
                drawArc(e.x,e.y,e.radius+0.18f,0,MathUtils.PI2*e.getStunRemaining()/Math.max(0.001f,combat.stunDuration));
            }
            if(e.state==CombatSystem.EnemyState.WINDUP) {
                batch.setColor(1,0.35f,0.1f,0.9f);
                drawCircle(e.x,e.y,e.radius+0.3f);
            }
            if(e.type==CombatSystem.EnemyType.FLYING) {
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
