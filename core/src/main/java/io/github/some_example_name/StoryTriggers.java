package io.github.some_example_name;

import com.badlogic.gdx.utils.JsonValue;
import java.util.Set;

/**
 * Disparadores de escenas definidos en el campo "story" de cada mapa.
 * Cada escena se ve una vez por partida: al dispararse se agrega la bandera
 * "scene:<id>", que viaja con las demas banderas en el guardado manual (SaveStore v3).
 *
 * Formato: {"scene": "id", "on": "start" | "zone" | "boss", "x","y","width","height",
 *           "grant": "bandera opcional al iniciar la escena"}.
 */
public final class StoryTriggers {
    private StoryTriggers() {}

    public static String flagFor(String scene) { return "scene:" + scene; }

    /** Devuelve el disparador pendiente para el evento, o null. No modifica las banderas. */
    public static JsonValue find(JsonValue story, Set<String> flags, String event, float px, float py) {
        if (story == null) return null;
        for (JsonValue t = story.child; t != null; t = t.next) {
            String scene = t.getString("scene", "");
            if (scene.isEmpty() || flags.contains(flagFor(scene))) continue;
            String on = t.getString("on", "zone");
            if (!on.equals(event)) continue;
            if (on.equals("zone")) {
                float x = t.getFloat("x"), y = t.getFloat("y");
                if (px < x || px > x + t.getFloat("width") || py < y || py > y + t.getFloat("height")) continue;
            }
            return t;
        }
        return null;
    }

    /** Marca la escena como vista y concede su bandera opcional. */
    public static void consume(JsonValue trigger, Set<String> flags) {
        flags.add(flagFor(trigger.getString("scene")));
        if (trigger.has("grant")) flags.add(trigger.getString("grant"));
    }
}
