package io.github.some_example_name;

import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reproduce las escenas del guion narrativo (assets/story). Logica pura: el texto aparece
 * por caracteres en tiempo real; avanzar completa la linea y despues pasa a la siguiente.
 * Mientras esta activa, TestLevelScreen detiene la simulacion (como la pausa).
 */
public final class DialoguePlayer {
    public static final class Line {
        public final String speaker, text, style;
        public Line(String speaker, String text, String style) {
            this.speaker = speaker == null ? "" : speaker;
            this.text = text == null ? "" : text;
            this.style = style == null || style.isEmpty() ? "dialogue" : style;
        }
        /** narration: voz en off; action: acotacion; glitch: flashback; datapad: pantalla. */
        public boolean is(String s) { return style.equals(s); }
    }

    public float charactersPerSecond = 48;
    private List<Line> lines = Collections.emptyList();
    private String sceneId = "";
    private int index;
    private float revealed;

    /** Lee {"scenes": {"id": [{"speaker","text","style"}...]}}. */
    public static Map<String, List<Line>> parse(JsonValue root) {
        Map<String, List<Line>> scenes = new HashMap<>();
        JsonValue all = root == null ? null : root.get("scenes");
        if (all == null) return scenes;
        for (JsonValue scene = all.child; scene != null; scene = scene.next) {
            List<Line> list = new ArrayList<>();
            for (JsonValue l = scene.child; l != null; l = l.next)
                list.add(new Line(l.getString("speaker", ""), l.getString("text", ""), l.getString("style", "")));
            scenes.put(scene.name, list);
        }
        return scenes;
    }

    public void start(String id, List<Line> scene) {
        if (scene == null || scene.isEmpty()) return;
        sceneId = id; lines = scene; index = 0; revealed = 0;
    }
    public boolean isActive() { return index < lines.size(); }
    public String getSceneId() { return sceneId; }
    public Line current() { return isActive() ? lines.get(index) : null; }
    public int getIndex() { return index; }
    public int getLineCount() { return lines.size(); }
    public boolean isLineComplete() { return !isActive() || revealed >= current().text.length(); }
    public String visibleText() {
        if (!isActive()) return "";
        String t = current().text;
        return t.substring(0, Math.min(t.length(), (int) revealed));
    }
    public void update(float realDt) {
        if (isActive() && !isLineComplete()) revealed = Math.min(current().text.length(), revealed + charactersPerSecond * Math.max(0, realDt));
    }
    /** Primer toque completa la linea; el siguiente avanza. Devuelve true si la escena termino. */
    public boolean advance() {
        if (!isActive()) return true;
        if (!isLineComplete()) { revealed = current().text.length(); return false; }
        index++; revealed = 0;
        return !isActive();
    }
    /** Salta la escena completa. */
    public void skip() { index = lines.size(); }
}
