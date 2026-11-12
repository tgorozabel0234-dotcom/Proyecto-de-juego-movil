package io.github.some_example_name;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nivel 1 segun el guion: Salto Frame, Matones, Capataz Brutal, escenas y datos del mapa.
 * Ejecutar con gradlew :core:storyCheck (forma parte de :core:check). Sin ventana.
 */
public final class StoryLevelCheck {
    static final float DT = 1f / 240;
    static File assets() { File a = new File("assets"); return a.isDirectory() ? a : new File("../assets"); }
    static String read(String path) {
        try { return new String(java.nio.file.Files.readAllBytes(new File(assets(), path).toPath()), "UTF-8"); }
        catch (Exception e) { throw new AssertionError("No se puede leer " + path, e); }
    }
    static void check(boolean ok, String msg) { if (!ok) throw new AssertionError(msg); }

    static PlayerBody flat() {
        PlayerBody p = new PlayerBody();
        p.addSolid(-100, -2, 200, 2);
        return p;
    }

    public static void main(String[] args) {
        frameJump();
        backArmor();
        squadFlee();
        foremanCycles();
        foremanWave();
        foremanOnRealMap();
        dialogueAndTriggers();
        levelData();
        System.out.println("PASS: salto frame, matones, capataz (3 ciclos, onda, mapa real), escenas y datos del nivel 1");
    }

    // ------------------------------------------------------------------ Salto Frame
    static int dashInto(PlayerBody p, PlayerVitals life, FrameJump frame, List<CombatSystem.Enemy> enemies) {
        p.update(DT, 1, false, true, false, true);
        frame.update(p, life, enemies);
        for (int i = 0; i < 240 && p.isDashing(); i++) { p.update(DT, 0, false, false, false, true); frame.update(p, life, enemies); }
        return frame.getCount();
    }

    static void frameJump() {
        for (boolean enabled : new boolean[]{true, false}) {
            PlayerBody p = flat(); p.reset(0, 0); p.update(DT, 0, false);
            PlayerVitals life = new PlayerVitals(); life.reset();
            CombatSystem c = new CombatSystem();
            CombatSystem.Enemy e = new CombatSystem.Enemy(3, .55f, .55f, 40, false, CombatSystem.EnemyType.THUG);
            c.enemies.add(e);
            FrameJump frame = new FrameJump(); frame.enabled = enabled;
            int count = dashInto(p, life, frame, c.enemies);
            if (enabled) {
                float center = p.x + PlayerBody.WIDTH / 2;
                check(count == 1, "El dash contra un enemigo activa el Salto Frame");
                check(center > e.x + e.radius, "Reaparece a la espalda del enemigo");
                check(p.getFacing() < 0, "Queda mirando al enemigo para golpear su espalda");
                check(!p.isDashing() && p.getVelocityX() == 0, "El Salto Frame termina el dash");
                check(life.isInvulnerable() && frame.timeScale() < 1, "Intangibilidad breve y bullet-time");
                check(e.health == e.maxHealth && !e.isStunned(), "El Salto Frame no causa dano ni stun por si solo");
                frame.updateRealTime(frame.slowDuration + .01f);
                check(frame.timeScale() == 1, "El bullet-time termina en tiempo real");
            } else check(count == 0, "Sin desbloquear no hay Salto Frame");
        }
        // Destino bloqueado por una pared: no atraviesa solidos.
        PlayerBody p = flat(); p.addSolid(3.6f, 0, 2, 5); p.reset(0, 0); p.update(DT, 0, false);
        PlayerVitals life = new PlayerVitals(); life.reset();
        CombatSystem.Enemy e = new CombatSystem.Enemy(3, .55f, .55f, 40, false, CombatSystem.EnemyType.THUG);
        FrameJump frame = new FrameJump(); frame.enabled = true;
        check(dashInto(p, life, frame, java.util.Collections.singletonList(e)) == 0 && p.x + PlayerBody.WIDTH <= 3.6001f,
            "Con pared detras del enemigo el Salto Frame se cancela");
        // Enemigo detras del jugador o muerto: no hay salto.
        p = flat(); p.reset(5, 0); p.update(DT, 0, false);
        e = new CombatSystem.Enemy(3, .55f, .55f, 40, false, CombatSystem.EnemyType.THUG);
        frame = new FrameJump(); frame.enabled = true;
        check(dashInto(p, life, frame, java.util.Collections.singletonList(e)) == 0, "Solo enemigos por delante del dash");
        p = flat(); p.reset(0, 0); p.update(DT, 0, false);
        e.receiveHit(999, .75f);
        check(dashInto(p, life, frame, java.util.Collections.singletonList(e)) == 0, "Un enemigo muerto no activa el salto");
    }

    // ------------------------------------------------------------------ Blindaje
    static float strike(float playerX, float bossFacing, boolean vulnerable) {
        PlayerBody p = flat(); p.reset(playerX, 0); p.update(DT, 0, false);
        CombatSystem c = new CombatSystem();
        CombatSystem.Enemy boss = new CombatSystem.Enemy(5, 1.1f, 1.1f, 90, true, CombatSystem.EnemyType.FOREMAN);
        boss.backArmor = true; boss.vulnerable = vulnerable; boss.facing = bossFacing;
        c.enemies.add(boss);
        float face = boss.x > playerX ? 1 : -1;
        p.update(DT, face, false); // orienta sin desplazarse apenas
        c.update(DT, p, true, true, false, false);
        for (int i = 0; i < 120; i++) c.update(DT, p, false, false, false, false);
        check(!boss.isStunned(), "El Capataz es inmune al stun");
        return boss.health;
    }

    static void backArmor() {
        check(strike(3, 1, true) < 90, "La bateria trasera recibe dano por la espalda");
        check(strike(3, -1, true) == 90, "El frente blindado no recibe dano");
        check(strike(3, 1, false) == 90, "Fuera de la ventana de vulnerabilidad no hay dano");
        check(strike(7, -1, true) < 90, "La espalda funciona hacia ambos lados");
        CombatSystem.Enemy normal = new CombatSystem.Enemy(0, 0, .5f, 50, false, CombatSystem.EnemyType.ROBOT);
        check(normal.acceptsHitFrom(-3) && normal.acceptsHitFrom(3), "Los enemigos sin blindaje no cambian");
    }

    // ------------------------------------------------------------------ Matones
    static void squadFlee() {
        PlayerBody p = flat(); p.reset(0, 0); p.update(DT, 0, false);
        PlayerVitals life = new PlayerVitals(); life.reset();
        CombatSystem c = new CombatSystem(); EnemyAI ai = new EnemyAI();
        CombatSystem.Enemy a = new CombatSystem.Enemy(3, .55f, .55f, 40, false, CombatSystem.EnemyType.THUG);
        CombatSystem.Enemy b = new CombatSystem.Enemy(5, .55f, .55f, 40, false, CombatSystem.EnemyType.THUG);
        CombatSystem.Enemy loner = new CombatSystem.Enemy(9, .55f, .55f, 40, false, CombatSystem.EnemyType.THUG);
        a.squad = b.squad = loner.squad = "matones";
        a.fleeWhenSquadFalls = b.fleeWhenSquadFalls = true;
        c.enemies.add(a); c.enemies.add(b); c.enemies.add(loner);
        for (int i = 0; i < 60; i++) { life.update(DT); ai.update(DT, p, life, c.enemies); }
        check(!b.fleeing, "Nadie huye mientras el grupo esta completo");
        a.receiveHit(999, .75f);
        ai.update(DT, p, life, c.enemies);
        check(b.fleeing && !loner.fleeing, "Al caer un Maton, el companero marcado huye");
        float startX = b.x;
        for (int i = 0; i < (int) (.5f / DT); i++) { life.update(DT); ai.update(DT, p, life, c.enemies); }
        check(b.x > startX && b.windupRemaining == 0, "Huye alejandose del jugador sin atacar");
        for (int i = 0; i < (int) (ai.fleeDuration / DT) + 10; i++) ai.update(DT, p, life, c.enemies);
        check(b.fled && !b.alive() && b.health == 0, "Desaparece y queda fuera del combate y del guardado");
        c.reset();
        check(a.alive() && b.alive() && !b.fleeing && !b.fled, "Reiniciar restaura al grupo");
        // El stun tambien congela la huida.
        CombatSystem.Enemy x = new CombatSystem.Enemy(3, .55f, .55f, 40, false, CombatSystem.EnemyType.THUG);
        x.fleeing = true; x.fleeRemaining = 5; x.receiveHit(1, .75f);
        float fx = x.x;
        c = new CombatSystem(); c.enemies.add(x);
        for (int i = 0; i < 100; i++) ai.update(DT, p, life, c.enemies);
        check(x.x == fx, "Un Maton aturdido no huye");
    }

    // ------------------------------------------------------------------ Capataz
    static ForemanBoss boss(float x, float floorTop) {
        CombatSystem.Enemy e = new CombatSystem.Enemy(x, floorTop + 1.1f, 1.1f, 90, true, CombatSystem.EnemyType.FOREMAN);
        ForemanBoss b = new ForemanBoss(e);
        b.reset();
        return b;
    }

    static void foremanCycles() {
        PlayerBody p = flat(); p.reset(0, 0); p.update(DT, 0, false);
        PlayerVitals life = new PlayerVitals(); life.reset();
        ForemanBoss b = boss(30, 0);
        b.update(DT, p, life);
        check(b.getState() == ForemanBoss.State.DORMANT, "Dormido fuera de su alcance");
        b = boss(8, 0);
        b.body.vulnerable = true;
        b.body.health = 20; // un golpe enorme dentro del primer ciclo
        b.update(DT, p, life);
        check(b.body.health == 60 && b.getCycle() == 1, "El dano de un ciclo se limita a un tercio");
        check(!b.body.vulnerable, "Al vaciar el tercio termina la ventana");
        b.body.vulnerable = true; b.body.health = 0;
        b.update(DT, p, life);
        check(b.body.health == 30 && b.getCycle() == 2 && b.body.alive(), "El segundo ciclo tambien se limita");
        b.body.vulnerable = true; b.body.health = 0;
        b.update(DT, p, life);
        check(b.getState() == ForemanBoss.State.DEFEATED && b.consumeDefeat() && !b.consumeDefeat(),
            "Cae en el tercer ciclo y anuncia la derrota una sola vez");
        // Una partida guardada a mitad del combate retoma el ciclo correcto.
        ForemanBoss saved = boss(8, 0); saved.body.health = 55; saved.reset();
        check(saved.getCycle() == 1 && saved.getPhase() == 2, "Reanuda en el ciclo y fase de su vida");
        // La carga golpea a quien la recibe de frente.
        p = flat(); p.reset(0, 0); p.update(DT, 0, false); life.reset();
        b = boss(8, 0);
        for (int i = 0; i < 240 * 3 && life.getHealth() == 10; i++) { life.update(DT); b.update(DT, p, life); }
        check(life.getHealth() == 10 - b.chargeDamage, "La carga frontal causa dano de contacto");
    }

    static void foremanWave() {
        for (boolean onLedge : new boolean[]{false, true}) {
            PlayerBody p = flat();
            if (onLedge) p.addSolid(-1, 0, 2, 1); // repisa por encima de la onda
            p.reset(0, onLedge ? 1 : 0); p.update(DT, 0, false);
            PlayerVitals life = new PlayerVitals(); life.reset();
            ForemanBoss b = boss(10, 0);
            b.body.health = 60; b.reset(); // fase 2
            boolean sawWave = false;
            // Anuncio 0.8 s + recorrido de la onda (~0.9 s); la carga llega despues de 2.2 s.
            for (int i = 0; i < (int) (2.0f / DT); i++) { life.update(DT); b.update(DT, p, life); sawWave |= b.isWaveActive(); }
            check(sawWave, "En fase 2 golpea el suelo y lanza la onda");
            if (onLedge) check(life.getHealth() == 10, "Estar por encima de la onda la esquiva");
            else check(life.getHealth() == 10 - b.waveDamage, "La onda dana a quien esta en el suelo");
        }
    }

    /** Combate completo en la geometria real del Callejon 42 con la tecnica del guion. */
    static void foremanOnRealMap() {
        JsonValue map = new JsonReader().parse(read("levels/section_1.json"));
        JsonValue types = new JsonReader().parse(read("enemies/prototype/types.json"));
        PlayerBody p = new PlayerBody();
        for (JsonValue piece = map.get("pieces").child; piece != null; piece = piece.next)
            p.addSolid(piece.getFloat("x"), piece.getFloat("y"), piece.getFloat("width"), piece.getFloat("height"), "pipe".equals(piece.getString("role", "")));
        JsonValue t = null;
        for (JsonValue e = map.get("targets").child; e != null; e = e.next) if ("FOREMAN".equals(e.getString("type"))) t = e;
        check(t != null, "El Callejon 42 tiene al Capataz");
        CombatSystem c = new CombatSystem();
        JsonValue attacks = new JsonReader().parse(read("combat/prototype/attacks.json"));
        c.duration = attacks.getFloat("duration"); c.reach = attacks.getFloat("reach");
        c.attackOriginHeight = attacks.getFloat("attackOriginHeight"); c.baseDamage = attacks.getFloat("damage");
        CombatSystem.Enemy body = new CombatSystem.Enemy(t.getFloat("x"), t.getFloat("y"), t.getFloat("radius"), t.getFloat("health"), t.getBoolean("stunImmune"), CombatSystem.EnemyType.FOREMAN);
        c.enemies.add(body);
        ForemanBoss b = new ForemanBoss(body);
        JsonValue f = types.get("foreman");
        b.aggroRange = f.getFloat("aggroRange"); b.chargeSpeed = f.getFloat("chargeSpeed"); b.chargeDistance = f.getFloat("chargeDistance");
        b.exposedTime = f.getFloat("exposedTime"); b.telegraph = f.getFloat("telegraph"); b.cycles = f.getInt("cycles");
        b.waveSpeed = f.getFloat("waveSpeed"); b.waveRange = f.getFloat("waveRange"); b.chargeDamage = f.getFloat("chargeDamage");
        b.reset();
        PlayerVitals life = new PlayerVitals(); life.reset();
        FrameJump frame = new FrameJump(); frame.enabled = true;
        p.reset(150, 6.2f); p.update(DT, 0, false);
        int frames = 0, exposedWithDamage = 0, swings = 0;
        float lastHealth = body.health, attackWait = 0;
        boolean sawWave = false, defeated = false, wasExposed = false;
        for (int i = 0; i < 240 * 120 && !defeated; i++) {
            float pc = p.x + PlayerBody.WIDTH / 2, dx = body.x - pc;
            float toward = Math.signum(dx);
            boolean dash = false, jump = false, attack = false; float dir = 0;
            ForemanBoss.State s = b.getState();
            boolean behind = (pc - body.x) * body.facing < 0;
            if (b.isWaveActive() && p.isGrounded() && Math.abs(Math.abs(pc - b.getWaveOriginX()) - b.getWaveRadius()) < 1.6f) jump = true;
            else if (s == ForemanBoss.State.CHARGE && dx * body.facing < 0 && Math.abs(dx) < 3.2f && p.canDash()) {
                dir = toward; dash = true; // Salto Frame a traves de la carga
            } else if (body.vulnerable && behind) {
                if (Math.abs(dx) > body.radius + c.reach * .8f) dir = toward;
                else if (p.getFacing() != toward) dir = toward * .01f;
                else if ((attackWait -= DT) <= 0) { attack = true; attackWait = .45f; swings++; }
            }
            life.update(DT);
            p.update(DT, dir, jump, dash, false, true);
            if (frame.update(p, life, c.enemies) != null) frames++;
            c.update(DT, p, attack, attack, false, false);
            b.update(DT, p, life);
            sawWave |= b.isWaveActive();
            if (body.health < lastHealth && !wasExposed) { exposedWithDamage++; wasExposed = true; }
            if (!body.vulnerable) wasExposed = false;
            lastHealth = body.health;
            defeated = b.consumeDefeat();
            check(life.alive(), "El jugador sobrevive aplicando la tecnica del guion");
        }
        check(defeated, "El Capataz cae con Salto Frame + golpes a la bateria (" + body.health + " de vida, " + frames + " saltos)");
        System.out.println("  capataz: saltos frame=" + frames + ", golpes=" + swings + ", ventanas con dano=" + exposedWithDamage + ", vida jugador=" + life.getHealth());
        check(frames >= 3, "Cada ciclo se gana atravesando la carga con el Salto Frame");
        check(life.getHealth() == life.maxHealth, "Con la tecnica correcta el Capataz no alcanza al jugador");
        check(exposedWithDamage >= 3, "Se necesitan al menos 3 ventanas de vulnerabilidad");
        check(sawWave, "La fase 2 aparece durante el combate real");
        check(body.x - body.radius >= 148 - .01f && body.x + body.radius <= 168 + .01f, "La carga se detiene en el borde de la plataforma");
    }

    // ------------------------------------------------------------------ Escenas
    static void dialogueAndTriggers() {
        Map<String, List<DialoguePlayer.Line>> scenes = DialoguePlayer.parse(new JsonReader().parse(read("story/nivel_1.json")));
        for (String id : new String[]{"intro", "matones", "matones_huida", "capataz", "datapad"})
            check(scenes.containsKey(id) && !scenes.get(id).isEmpty(), "Falta la escena " + id);
        check(scenes.get("datapad").stream().anyMatch(l -> l.text.contains("CELESTIA_NET")), "El datapad muestra la autorizacion del guion");
        DialoguePlayer d = new DialoguePlayer();
        d.start("intro", scenes.get("intro"));
        check(d.isActive() && d.visibleText().isEmpty(), "La escena empieza sin texto");
        d.update(.1f);
        check(d.visibleText().length() == (int) (d.charactersPerSecond * .1f), "El texto aparece por caracteres");
        check(!d.advance() && d.isLineComplete() && d.getIndex() == 0, "El primer toque completa la linea");
        check(!d.advance() && d.getIndex() == 1, "El segundo toque avanza");
        d.skip();
        check(!d.isActive() && d.current() == null, "Saltar termina la escena");

        JsonValue story = new JsonReader().parse(read("levels/section_1.json")).get("story");
        Set<String> flags = new HashSet<>();
        JsonValue start = StoryTriggers.find(story, flags, "start", 4, 3);
        check(start != null && start.getString("scene").equals("intro"), "La partida nueva abre con la escena 1.1");
        StoryTriggers.consume(start, flags);
        check(StoryTriggers.find(story, flags, "start", 4, 3) == null, "Cada escena se ve una vez");
        check(StoryTriggers.find(story, flags, "zone", 40, 5) == null, "Sin escena fuera de las zonas");
        check(StoryTriggers.find(story, flags, "zone", 69, 4, true) == null, "Acercarse sin pisar su plataforma no dispara la escena");
        check(StoryTriggers.find(story, flags, "zone", 76, 3.9f, false) == null, "En el aire (salto o gancho) no se dispara la escena");
        JsonValue matones = StoryTriggers.find(story, flags, "zone", 76, 3.9f, true);
        check(matones != null && matones.getString("scene").equals("matones"), "De pie en la plataforma de los Matones se abre la escena 1.2");
        check(StoryTriggers.find(story, flags, "zone", 150, 6.7f, false) == null
            && StoryTriggers.find(story, flags, "zone", 150, 6.7f, true) != null, "La escena del Capataz exige estar de pie en su plataforma");
        check(!flags.contains("frame"), "El Salto Frame no esta antes de la escena 1.2");
        StoryTriggers.consume(matones, flags);
        check(flags.contains("frame") && flags.contains("scene:matones"), "La escena 1.2 desbloquea el Salto Frame");
        JsonValue boss = StoryTriggers.find(story, flags, "boss", 0, 0);
        check(boss != null && boss.getString("scene").equals("datapad"), "Vencer al Capataz abre la escena 1.3");
        StoryTriggers.consume(boss, flags);
        check(flags.contains("capataz"), "La escena 1.3 abre el paso al sector industrial");
    }

    static void levelData() {
        JsonValue map = new JsonReader().parse(read("levels/section_1.json"));
        Set<String> ids = new HashSet<>();
        int thugs = 0, foremen = 0;
        PlayerBody solids = new PlayerBody();
        for (JsonValue piece = map.get("pieces").child; piece != null; piece = piece.next)
            solids.addSolid(piece.getFloat("x"), piece.getFloat("y"), piece.getFloat("width"), piece.getFloat("height"));
        for (JsonValue e = map.get("targets").child; e != null; e = e.next) {
            check(ids.add(e.getString("id")), "Id de enemigo repetido: " + e.getString("id"));
            CombatSystem.EnemyType.valueOf(e.getString("type"));
            float x = e.getFloat("x"), y = e.getFloat("y"), r = e.getFloat("radius");
            check(solids.isAreaFree(x - r, y - r, r * 2, r * 2), e.getString("id") + " no debe empezar dentro de un solido");
            check(!solids.isAreaFree(x - r, y - r - .06f, r * 2, .06f), e.getString("id") + " debe empezar apoyado");
            if (e.getString("type").equals("THUG")) { thugs++; check("matones".equals(e.getString("squad", "")), "Los Matones forman un grupo"); }
            if (e.getString("type").equals("FOREMAN")) { foremen++; check(e.getBoolean("stunImmune"), "El jefe es stunImmune (docs/gameplay/enemy-stun.md)"); }
        }
        check(thugs == 2 && foremen == 1, "Dos Matones y un Capataz en el Callejon 42");
        check("capataz".equals(map.get("exit").getString("requires", "")), "La salida espera la derrota del Capataz");
        // Zonas "grounded": su borde inferior es la superficie de la plataforma de los enemigos,
        // y los ids de "focus" existen en el mapa.
        for (JsonValue t = map.get("story").child; t != null; t = t.next) {
            if (t.getBoolean("grounded", false)) {
                float zx = t.getFloat("x"), zy = t.getFloat("y"), zw = t.getFloat("width");
                check(!solids.isAreaFree(zx + .1f, zy - .05f, zw - .2f, .05f) && solids.isAreaFree(zx + .1f, zy + .01f, zw - .2f, .5f),
                    "La zona de " + t.getString("scene") + " debe estar sobre la plataforma de sus enemigos");
            }
            if (t.has("focus")) for (JsonValue f = t.get("focus").child; f != null; f = f.next)
                check(ids.contains(f.asString()), "focus de " + t.getString("scene") + " no existe: " + f.asString());
        }
        // Sprites del Capataz (hoja del usuario): cada cuadro citado existe y comparte lienzo.
        JsonValue anim = new JsonReader().parse(read("enemies/Capataz/animations.json"));
        int w = anim.getInt("frameWidth"), h = anim.getInt("frameHeight");
        for (JsonValue clip = anim.get("clips").child; clip != null; clip = clip.next)
            for (JsonValue f = clip.get("frames").child; f != null; f = f.next) {
                File png = new File(assets(), "enemies/Capataz/frames/" + f.asString() + ".png");
                check(png.isFile(), "Falta el cuadro del Capataz " + f.asString());
                try {
                    java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(png);
                    check(img.getWidth() == w && img.getHeight() == h && img.getColorModel().hasAlpha(),
                        f.asString() + " debe ser RGBA de " + w + "x" + h);
                } catch (java.io.IOException ex) { throw new AssertionError(ex); }
            }
    }
}
