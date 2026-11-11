package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Figura procedural provisional del Capataz Brutal (nivel 1) y efectos del Salto Frame.
 * Los Matones usan los sprites del Robot (EnemySprites). Solo dibuja: no cambia posiciones, dano ni estados.
 * Se reemplazara por sprites cuando exista arte propio; ver assets/story/README.md.
 */
public final class StoryFoesRenderer {
    private static final Color VISOR = Color.valueOf("FF2B4A");
    private static final Color ELECTRIC = Color.valueOf("9FF3FF"), STEEL = Color.valueOf("56606F");
    private static final Color STEEL_DARK = Color.valueOf("323943"), HAZARD = Color.valueOf("F2A93B");
    private static final Color BATTERY = Color.valueOf("15F4E3"), STUN = Color.valueOf("F4B95D");
    private final Color tmp = new Color();
    private SpriteBatch batch;
    private Texture pixel;

    private final ForemanSprites foremanSprites = new ForemanSprites();

    public void begin(SpriteBatch batch, Texture pixel) { this.batch = batch; this.pixel = pixel; }
    public void dispose() { foremanSprites.dispose(); }

    // ---------------------------------------------------------------- Capataz
    public void drawForeman(ForemanBoss boss, float time) {
        if (foremanSprites.draw(batch, boss, time)) { drawForemanEffects(boss, time); return; }
        drawForemanProcedural(boss, time);
    }

    /** Efectos sobre los sprites: bateria trasera, anuncio, bloqueo, barra y datapad. */
    private void drawForemanEffects(ForemanBoss boss, float time) {
        CombatSystem.Enemy e = boss.body;
        float f = e.facing, feet = e.y - e.radius;
        ForemanBoss.State s = boss.getState();
        if (s == ForemanBoss.State.DEFEATED) {
            // Datapad junto al Capataz derribado (escena 1.3).
            batch.setColor(.15f, .9f, .55f, .7f + .3f * MathUtils.sin(time * 4));
            rect(e.x + f * 2.2f - .25f, feet + .05f, .5f, .32f);
            batch.setColor(Color.WHITE);
            return;
        }
        if (e.vulnerable) {
            // La mochila-bateria esta en la espalda del sprite: brilla mientras es vulnerable.
            float bx = e.x - f * 1.05f, by = feet + 2.55f;
            tmp.set(BATTERY).a = .45f + .3f * MathUtils.sin(time * 14);
            batch.setColor(tmp);
            circle(bx, by, .55f + .08f * MathUtils.sin(time * 8));
            circle(bx, by, .35f);
        }
        if (s == ForemanBoss.State.TELEGRAPH || s == ForemanBoss.State.SLAM) {
            batch.setColor(1, .3f, .1f, .5f + .5f * MathUtils.sin(time * 25));
            circle(e.x, feet + 2f, 2.2f);
        }
        if (e.blockFlash > 0) {
            batch.setColor(1, .9f, .4f, e.blockFlash / .2f);
            float sx = e.x + f * 1.3f, sy = feet + 2f;
            for (int i = 0; i < 5; i++) line(sx, sy, sx + f * MathUtils.random(.2f, .7f), sy + MathUtils.random(-.6f, .6f), .06f);
        }
        drawBossBar(boss);
        batch.setColor(Color.WHITE);
    }

    /** Respaldo si faltan los PNG de assets/enemies/Capataz. */
    private void drawForemanProcedural(ForemanBoss boss, float time) {
        CombatSystem.Enemy e = boss.body;
        if (!e.alive() && boss.getState() == ForemanBoss.State.DEFEATED) { drawWreck(e, time); return; }
        float f = e.facing, feet = e.y - e.radius;
        ForemanBoss.State s = boss.getState();
        float lean = s == ForemanBoss.State.CHARGE ? .35f : s == ForemanBoss.State.EXPOSED ? -.15f : 0;
        float stomp = s == ForemanBoss.State.CHARGE ? Math.abs(MathUtils.sin(time * 18)) * .1f : 0;
        float alpha = 1;
        // Piernas
        set(STEEL_DARK, e, alpha);
        rect(e.x - .75f, feet, .5f, 1.3f + stomp);
        rect(e.x + .25f, feet, .5f, 1.3f - stomp);
        // Torso con franjas de advertencia
        float tx = e.x - 1.05f + f * lean, ty = feet + 1.25f;
        set(STEEL, e, alpha);
        rect(tx, ty, 2.1f, 1.7f);
        set(HAZARD, e, alpha);
        for (int i = 0; i < 4; i++) rect(tx + .15f + i * .5f, ty + .15f, .22f, .22f);
        set(STEEL_DARK, e, alpha);
        rect(tx - .15f, ty + 1.5f, 2.4f, .35f);
        // Bateria trasera: el punto debil
        float bx = e.x - f * 1.25f + f * lean - .3f, by = ty + .35f;
        boolean open = e.vulnerable;
        float pulse = open ? .65f + .35f * MathUtils.sin(time * 12) : .25f;
        set(STEEL_DARK, e, alpha);
        rect(bx - .08f, by - .08f, .76f, 1.16f);
        tmp.set(e.blockFlash > 0 ? Color.WHITE : BATTERY).a = pulse;
        batch.setColor(e.flash > 0 ? Color.WHITE : tmp);
        float fill = e.health / Math.max(1, e.maxHealth);
        rect(bx, by, .6f, 1f * fill);
        if (open) {
            tmp.set(BATTERY).a = .35f + .2f * MathUtils.sin(time * 20);
            batch.setColor(tmp);
            circle(bx + .3f, by + .5f, .75f + .1f * MathUtils.sin(time * 8));
        }
        // Cabeza con visor rojo
        set(STEEL, e, alpha);
        rect(e.x - .35f + f * (lean + .3f), ty + 1.8f, .7f, .55f);
        tmp.set(VISOR).a = .8f + .2f * MathUtils.sin(time * 7);
        batch.setColor(tmp);
        rect(e.x - .3f + f * (lean + .38f), ty + 2.0f, .6f, .14f);
        // Brazo con porra: arriba al golpear el suelo, al frente en la carga.
        float angle = s == ForemanBoss.State.SLAM ? 80 : s == ForemanBoss.State.CHARGE ? 0 : s == ForemanBoss.State.TELEGRAPH ? 30 : -35;
        float rad = angle * MathUtils.degRad;
        float sx = e.x + f * (1.0f + lean), sy = ty + 1.4f;
        float hx = sx + f * MathUtils.cos(rad) * .9f, hy = sy + MathUtils.sin(rad) * .9f;
        set(STEEL_DARK, e, alpha);
        line(sx, sy, hx, hy, .38f);
        float cx = hx + f * MathUtils.cos(rad) * 1.6f, cy = hy + MathUtils.sin(rad) * 1.6f;
        set(STEEL, e, alpha);
        line(hx, hy, cx, cy, .22f);
        tmp.set(ELECTRIC).a = .6f + .4f * MathUtils.random();
        batch.setColor(tmp);
        line(hx + (cx - hx) * .4f, hy + (cy - hy) * .4f, cx, cy, .08f);
        // Anuncio: anillo rojo; carga: estelas.
        if (s == ForemanBoss.State.TELEGRAPH || s == ForemanBoss.State.SLAM) {
            batch.setColor(1, .3f, .1f, .5f + .5f * MathUtils.sin(time * 25));
            circle(e.x, e.y, e.radius + .5f);
        } else if (s == ForemanBoss.State.CHARGE) {
            batch.setColor(1, 1, 1, .35f);
            for (int i = 0; i < 4; i++) line(e.x - f * (1.4f + i * .4f), feet + .5f + i * .55f, e.x - f * (2.6f + i * .4f), feet + .5f + i * .55f, .05f);
        }
        if (e.blockFlash > 0) {
            batch.setColor(1, .9f, .4f, e.blockFlash / .2f);
            circle(e.x + f * 1.1f, e.y + .4f, .5f);
        }
        drawBossBar(boss);
        batch.setColor(Color.WHITE);
    }

    private void drawBossBar(ForemanBoss boss) {
        CombatSystem.Enemy e = boss.body;
        if (!boss.isEngaged()) return;
        float w = 3.6f, x = e.x - w / 2, y = e.y - e.radius + 5.2f;
        batch.setColor(.12f, .04f, .07f, .9f);
        rect(x - .05f, y - .05f, w + .1f, .3f);
        batch.setColor(BATTERY);
        rect(x, y, w * e.health / e.maxHealth, .2f);
        batch.setColor(0, 0, 0, 1);
        for (int i = 1; i < boss.cycles; i++) rect(x + w * i / boss.cycles - .03f, y, .06f, .2f);
    }

    private void drawWreck(CombatSystem.Enemy e, float time) {
        float feet = e.y - e.radius;
        batch.setColor(STEEL_DARK);
        rect(e.x - 1.2f, feet, 2.4f, .6f);
        batch.setColor(STEEL);
        rect(e.x - .9f, feet + .5f, 1.6f, .5f);
        if (MathUtils.sin(time * 6) > .6f) {
            batch.setColor(ELECTRIC);
            line(e.x, feet + 1f, e.x + MathUtils.random(-.6f, .6f), feet + 1.6f, .04f);
        }
        // Datapad junto al Capataz derribado (escena 1.3).
        batch.setColor(.15f, .9f, .55f, .7f + .3f * MathUtils.sin(time * 4));
        rect(e.x + 1.5f, feet + .05f, .5f, .32f);
        batch.setColor(Color.WHITE);
    }

    /** Onda electrica que recorre el suelo en ambos sentidos. */
    public void drawWave(ForemanBoss boss, float time) {
        if (!boss.isWaveActive()) return;
        float r = boss.getWaveRadius(), y = boss.getWaveFloorY(), h = boss.waveHeight;
        float fade = 1 - r / Math.max(.01f, boss.waveRange);
        for (int side = -1; side <= 1; side += 2) {
            float x = boss.getWaveOriginX() + side * r;
            batch.setColor(ELECTRIC.r, ELECTRIC.g, ELECTRIC.b, .9f * fade);
            float px = x, py = y;
            for (int i = 1; i <= 4; i++) {
                float nx = x - side * i * .18f, ny = y + (i % 2 == 0 ? .1f : h * (1 - i * .15f)) + MathUtils.random(-.06f, .06f);
                line(px, py, nx, ny, .07f);
                px = nx; py = ny;
            }
            batch.setColor(.3f, .6f, 1, .35f * fade);
            rect(Math.min(x, boss.getWaveOriginX()), y, Math.abs(x - boss.getWaveOriginX()), .06f);
        }
        batch.setColor(Color.WHITE);
    }

    /** Estela de estatica azul y codigo binario entre el origen y el destino del Salto Frame. */
    public void drawFrameJump(FrameJump frame, float time) {
        float k = frame.getTrailRemaining() / Math.max(.01f, frame.slowDuration);
        if (k <= 0) return;
        float fx = frame.getFromX(), fy = frame.getFromY(), tx = frame.getToX(), ty = frame.getToY();
        batch.setColor(.25f, .55f, 1, .55f * k);
        for (int i = 0; i < 6; i++) {
            float o = (i - 2.5f) * .22f;
            line(fx, fy + o, tx, ty + o, .03f + .03f * MathUtils.random());
        }
        // "Bits" de estatica en el origen y el destino
        for (int i = 0; i < 14; i++) {
            boolean origin = i % 2 == 0;
            float bx = (origin ? fx : tx) + MathUtils.random(-.6f, .6f), by = (origin ? fy : ty) + MathUtils.random(-1f, 1.2f);
            batch.setColor(origin ? .4f : .85f, origin ? .7f : .95f, 1, k);
            rect(bx, by, MathUtils.randomBoolean() ? .08f : .16f, .08f);
        }
        batch.setColor(Color.WHITE);
    }

    // ---------------------------------------------------------------- utilidades
    private void set(Color base, CombatSystem.Enemy e, float alpha) {
        if (e.flash > 0) batch.setColor(1, 1, 1, alpha);
        else if (e.isStunned()) batch.setColor(STUN.r * .6f + base.r * .4f, STUN.g * .6f + base.g * .4f, STUN.b * .6f + base.b * .4f, alpha);
        else batch.setColor(base.r, base.g, base.b, alpha);
    }
    private void rect(float x, float y, float w, float h) { batch.draw(pixel, x, y, w, h); }
    private void line(float x1, float y1, float x2, float y2, float t) {
        float dx = x2 - x1, dy = y2 - y1;
        batch.draw(pixel, x1, y1 - t / 2, 0, t / 2, (float) Math.hypot(dx, dy), t, 1, 1,
            (float) Math.toDegrees(Math.atan2(dy, dx)), 0, 0, 1, 1, false, false);
    }
    private void circle(float cx, float cy, float r) {
        for (int i = 0; i < 28; i++) {
            float a = i * MathUtils.PI2 / 28, b = (i + 1) * MathUtils.PI2 / 28;
            line(cx + MathUtils.cos(a) * r, cy + MathUtils.sin(a) * r, cx + MathUtils.cos(b) * r, cy + MathUtils.sin(b) * r, .04f);
        }
    }
}
