package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Sprites del Capataz Brutal (assets/enemies/Capataz, hoja entregada por el usuario).
 * Solo dibuja: elige el cuadro segun el estado de ForemanBoss y nunca lo modifica.
 * Los cuadros comparten lienzo con los pies en el borde inferior, asi que se anclan
 * en (x, pies) sin pivotes por cuadro. Miran a la derecha y se espejan con facing.
 */
public final class ForemanSprites {
    private static final String ROOT = "enemies/Capataz/";
    private final Map<String, Texture> frames = new HashMap<>();
    private float pixelsPerUnit = 36, idleTime = .18f, walkTime = .08f, swingTime = .15f, deathTime = .35f;
    private boolean loaded, available;
    private float defeatSeenAt = -1;

    private void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        if (!Gdx.files.internal(ROOT + "animations.json").exists()) return;
        JsonValue meta = new JsonReader().parse(Gdx.files.internal(ROOT + "animations.json"));
        pixelsPerUnit = meta.getFloat("pixelsPerUnit", pixelsPerUnit);
        JsonValue clips = meta.get("clips");
        idleTime = clips.get("idle").getFloat("frameTime", idleTime);
        walkTime = clips.get("walk").getFloat("frameTime", walkTime);
        swingTime = clips.get("swing").getFloat("frameTime", swingTime);
        deathTime = clips.get("death").getFloat("frameTime", deathTime);
        for (JsonValue clip = clips.child; clip != null; clip = clip.next)
            for (JsonValue f = clip.get("frames").child; f != null; f = f.next) {
                String name = f.asString();
                if (frames.containsKey(name) || !Gdx.files.internal(ROOT + "frames/" + name + ".png").exists()) continue;
                Texture t = new Texture(Gdx.files.internal(ROOT + "frames/" + name + ".png"));
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                frames.put(name, t);
            }
        available = frames.containsKey("idle_00");
    }

    /** true si los sprites estan disponibles; si no, StoryFoesRenderer usa la figura procedural. */
    public boolean draw(SpriteBatch batch, ForemanBoss boss, float time) {
        ensureLoaded();
        if (!available) return false;
        String name = frameFor(boss, time);
        Texture t = frames.get(name);
        if (t == null) t = frames.get("idle_00");
        CombatSystem.Enemy e = boss.body;
        float w = t.getWidth() / pixelsPerUnit, h = t.getHeight() / pixelsPerUnit;
        float feet = e.y - e.radius;
        // El destello de dano ya viene en el cuadro HIT; el bloqueo se indica con chispas aparte.
        batch.setColor(Color.WHITE);
        batch.draw(t, e.x - w / 2, feet, w, h, 0, 0, t.getWidth(), t.getHeight(), e.facing < 0, false);
        return true;
    }

    private String frameFor(ForemanBoss boss, float time) {
        CombatSystem.Enemy e = boss.body;
        ForemanBoss.State s = boss.getState();
        if (s != ForemanBoss.State.DEFEATED) defeatSeenAt = -1;
        switch (s) {
            case DEFEATED:
                if (defeatSeenAt < 0) defeatSeenAt = time;
                return time - defeatSeenAt < deathTime ? "death_00" : "death_01";
            default: break;
        }
        if (e.flash > 0) return "hit_00";
        switch (s) {
            case CHARGE: return "walk_0" + (int) (time / walkTime) % 4;
            case TELEGRAPH:
                // Justo tras golpear el suelo se mantiene el impacto mientras sale la onda.
                if (boss.isWaveActive() && boss.getWaveRadius() < boss.waveSpeed * .25f) return "slam_02";
                return "windup_00";
            case SLAM: return boss.getTimer() > boss.slamWindup * .5f ? "slam_00" : "slam_01";
            case EXPOSED: return boss.exposedTime - boss.getTimer() < swingTime ? "swing_00" : "swing_01";
            default: return "idle_0" + (int) (time / idleTime) % 3;
        }
    }

    public void dispose() {
        for (Texture t : frames.values()) t.dispose();
        frames.clear();
        loaded = available = false;
    }
}
