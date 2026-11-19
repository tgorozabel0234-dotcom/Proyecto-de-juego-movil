package io.github.some_example_name;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Nivel 2 segun el guion (fase 1): escenas 2.1 y 2.2, Salto Frame Doble y barreras electromagneticas.
 * Ejecutar con gradlew :core:level2StoryCheck (forma parte de :core:check). Sin ventana.
 */
public final class Level2StoryCheck {
    static final float DT = 1f / 240;
    static final float LIFT = 2.5f;
    static File assets() { File a = new File("assets"); return a.isDirectory() ? a : new File("../assets"); }
    static JsonValue json(String path) {
        try { return new JsonReader().parse(new String(java.nio.file.Files.readAllBytes(new File(assets(), path).toPath()), "UTF-8")); }
        catch (Exception e) { throw new AssertionError("No se puede leer " + path, e); }
    }
    static void check(boolean ok, String msg) { if (!ok) throw new AssertionError(msg); }

    public static void main(String[] args) {
        barrierBlocksWithoutDoubleFrame();
        dashPhasesThroughBarrier();
        neverStuckInsideBarrier();
        secondAirDash();
        realBarrierRoom();
        scenesAndTriggers();
        System.out.println("PASS: Salto Frame Doble (segundo dash en el aire), barreras, sala real, escenas 2.1 y 2.2 y sus disparadores");
    }

    /** Suelo plano y una barrera de 0.6 en x=3, tan alta como los muros del mapa. */
    static PlayerBody gate(boolean doubleFrame, float barrierWidth) {
        PlayerBody p = new PlayerBody();
        p.addSolid(-100, -2, 200, 2);
        p.addBarrier(3, 0, barrierWidth, 160);
        p.setDoubleFrame(doubleFrame, LIFT);
        p.reset(0, 0);
        p.setDoubleFrame(doubleFrame, LIFT);
        p.update(DT, 0, false);
        return p;
    }
    static void run(PlayerBody p, float seconds, float dir, boolean dashEachSecond) {
        for (float t = 0; t < seconds; t += DT) {
            boolean dash = dashEachSecond && (int) (t / DT) % 240 == 0;
            p.update(DT, dir, false, dash, false, true);
        }
    }

    static void barrierBlocksWithoutDoubleFrame() {
        PlayerBody p = gate(false, .6f);
        run(p, 2, 1, false);
        check(p.x + PlayerBody.WIDTH <= 3.001f, "sin el Salto Frame Doble la barrera debe detener al jugador (x=" + p.x + ")");
        p = gate(false, .6f);
        run(p, 3, 1, true);
        check(p.x + PlayerBody.WIDTH <= 3.001f, "un dash normal no atraviesa la barrera (x=" + p.x + ")");
        check(!p.isPhasing(), "sin desbloqueo no hay paso de fase");
    }

    static void dashPhasesThroughBarrier() {
        PlayerBody p = gate(true, .6f);
        p.update(DT, 1, false, true, false, true);
        check(p.isDashing() && p.isPhasing(), "con el Salto Frame Doble el dash entra en fase");
        for (int i = 0; i < 240 && p.isDashing(); i++) p.update(DT, 1, false, false, false, true);
        check(p.x >= 3.6f, "el dash debe atravesar la barrera (x=" + p.x + ")");
        check(p.isAreaFree(p.x, p.y, PlayerBody.WIDTH, PlayerBody.HEIGHT), "al salir de la barrera queda en un espacio libre");
        // Al terminar el dash la barrera vuelve a ser solida: no se regresa caminando.
        run(p, 2, -1, false);
        check(p.x >= 3.599f, "de vuelta, la barrera bloquea otra vez sin dash (x=" + p.x + ")");
        // Tambien en el aire: salto + dash.
        p = gate(true, .6f);
        p.update(DT, 1, true, false, false, true);
        for (int i = 0; i < 30; i++) p.update(DT, 1, false, false, false, true);
        p.update(DT, 1, false, true, false, true);
        for (int i = 0; i < 480 && (p.isDashing() || !p.isGrounded()); i++) p.update(DT, 1, false, false, false, true);
        check(p.x >= 3.6f && p.isGrounded(), "el dash en el aire tambien atraviesa la barrera (x=" + p.x + ")");
    }

    static void neverStuckInsideBarrier() {
        // Barrera mas ancha que el recorrido del dash (unos 4): el dash continua hasta salir.
        PlayerBody p = gate(true, 6f);
        p.update(DT, 1, false, true, false, true);
        for (int i = 0; i < 480 && p.isDashing(); i++) p.update(DT, 0, false, false, false, true);
        check(p.x >= 9f - .001f, "un dash que acaba dentro de una barrera sigue hasta salir (x=" + p.x + ")");
        // Un Salto Frame que deja al jugador dentro de una barrera lo expulsa hacia delante.
        p = gate(false, .6f);
        p.frameShift(3.1f, 0, 1);
        p.update(DT, 0, false, false, false, true);
        check(p.isAreaFree(p.x, p.y, PlayerBody.WIDTH, PlayerBody.HEIGHT) && p.x >= 3.6f - .001f,
            "dentro de una barrera el jugador sale por el lado del avance (x=" + p.x + ")");
    }

    /** Devuelve cuantos dash comenzaron en un mismo salto al pedir tres seguidos. */
    static int airDashes(PlayerBody p) {
        p.update(DT, 1, true, false, false, true);
        for (int i = 0; i < 24; i++) p.update(DT, 1, false, false, false, true);
        int started = 0;
        for (int attempt = 0; attempt < 3; attempt++) {
            if (p.isGrounded()) break;
            p.update(DT, 1, false, true, false, true);
            if (p.isDashing()) started++;
            if (attempt == 1 && p.isDashing()) check(p.getVelocityY() >= LIFT - .3f, "el segundo dash en el aire da un pequeno impulso (vy=" + p.getVelocityY() + ")");
            for (int i = 0; i < 240 && p.isDashing(); i++) p.update(DT, 1, false, false, false, true);
        }
        for (int i = 0; i < 480 && !p.isGrounded(); i++) p.update(DT, 0, false, false, false, true);
        return started;
    }

    static void secondAirDash() {
        PlayerBody normal = new PlayerBody(); normal.addSolid(-100, -2, 300, 2); normal.reset(0, 0); normal.update(DT, 0, false);
        check(airDashes(normal) == 1, "sin el Salto Frame Doble solo hay un dash por recarga");
        PlayerBody p = new PlayerBody(); p.addSolid(-100, -2, 300, 2); p.reset(0, 0); p.setDoubleFrame(true, LIFT); p.update(DT, 0, false);
        check(airDashes(p) == 2, "con el Salto Frame Doble hay un segundo dash en el aire, y solo uno");
        for (int i = 0; i < 240; i++) p.update(DT, 0, false, false, false, true);
        check(airDashes(p) == 2, "el segundo dash en el aire se recupera al tocar el suelo");
        // En el suelo no existe: el dash sigue esperando su recarga.
        p.update(DT, 1, false, true, false, true);
        for (int i = 0; i < 240 && p.isDashing(); i++) p.update(DT, 1, false, false, false, true);
        p.update(DT, 1, false, true, false, true);
        check(!p.isDashing(), "en el suelo el segundo dash no ignora la recarga");
    }

    static PlayerBody body(JsonValue map) {
        PlayerBody p = new PlayerBody();
        for (JsonValue q = map.get("pieces").child; q != null; q = q.next) {
            float x = q.getFloat("x"), y = q.getFloat("y"), w = q.getFloat("width"), h = q.getFloat("height");
            String role = q.getString("role", "");
            if (role.equals("barrier")) p.addBarrier(x, y, w, h);
            else p.addSolid(x, y, w, h, role.equals("pipe"));
        }
        return p;
    }
    static boolean atDoor(PlayerBody p, JsonValue d) {
        float x = d.getFloat("x"), y = d.getFloat("y");
        return p.x + PlayerBody.WIDTH > x && p.x < x + d.getFloat("width") && p.y + PlayerBody.HEIGHT > y && p.y < y + d.getFloat("height");
    }

    /** Retorno de servicio: la barrera cierra la salida hasta tener el Salto Frame Doble. */
    static void realBarrierRoom() {
        String room = "levels/section_2_room_4.json";
        JsonValue map = json(room), barrier = null;
        for (JsonValue q = map.get("pieces").child; q != null; q = q.next) if ("barrier".equals(q.getString("role", ""))) barrier = q;
        check(barrier != null, room + ": falta la barrera electromagnetica");
        float bx = barrier.getFloat("x"), floor = barrier.getFloat("y");
        float top = barrier.getFloat("y") + barrier.getFloat("height");
        check(top >= json("levels/traversal_rules.json").getFloat("boundaryWallMinTop"), room + ": la barrera debe superar el alcance del gancho");
        JsonValue exit = map.get("exit");
        check(exit.getFloat("x") > bx, room + ": la barrera debe estar antes de la salida");
        for (boolean unlocked : new boolean[]{false, true}) {
            boolean reached = false;
            for (int attempt = 0; attempt < 4 && !reached; attempt++) {
                PlayerBody p = body(map);
                p.reset(bx - 2.5f, floor);
                p.setDoubleFrame(unlocked, LIFT);
                p.update(DT, 0, false);
                for (int step = 0; step < 240 * 6 && !reached; step++) {
                    boolean jump = attempt >= 2 && step % 240 == 0, dash = attempt % 2 == 1 ? step % 240 == 10 : step % 240 == 0;
                    p.update(DT, 1, jump, dash, false, true);
                    if (atDoor(p, exit)) reached = true;
                    check(p.y > -10, room + ": caida al vacio intentando cruzar la barrera");
                }
            }
            check(reached == unlocked, room + (unlocked ? ": con el Salto Frame Doble se llega a la salida"
                : ": sin el Salto Frame Doble no se puede pasar la barrera"));
        }
        JsonValue story = map.get("story");
        if (story != null) for (JsonValue t = story.child; t != null; t = t.next)
            check(!"frame2".equals(t.getString("grant", "")), room + ": la sala de la barrera no debe ser la que lo desbloquea");
    }

    static void scenesAndTriggers() {
        JsonValue config = json("combat/prototype/frame_jump.json");
        check("frame2".equals(config.getString("doubleUnlockFlag", "")), "frame_jump.json: falta doubleUnlockFlag");
        check(Math.abs(config.getFloat("airDashLift", -1) - LIFT) < .001f, "frame_jump.json: airDashLift distinto del probado");
        Map<String, List<DialoguePlayer.Line>> scenes = DialoguePlayer.parse(json("story/nivel_2.json"));
        List<DialoguePlayer.Line> intro = scenes.get("intro2"), tx = scenes.get("transmision");
        check(intro != null && tx != null, "nivel_2.json: faltan las escenas intro2 y transmision");
        check(text(intro).contains("aceite quemado y ozono") && text(intro).contains("arterias de Central Corp"), "escena 2.1 incompleta");
        check(text(tx).contains("núcleo del Nivel 5") && text(tx).contains("elimínenlo sin piedad")
            && text(tx).contains("nivel por nivel hasta alcanzarte"), "escena 2.2 incompleta");
        check(tx.get(tx.size() - 1).is("tutorial") && tx.get(tx.size() - 1).text.contains("SALTO FRAME DOBLE"), "la escena 2.2 termina con el tutorial");

        // 2.1: al entrar al Colector del distrito por la puerta del nivel 1.
        JsonValue s2 = json("levels/section_2.json");
        check("story/nivel_2.json".equals(s2.getString("storyFile", "")), "section_2: storyFile");
        Set<String> flags = new HashSet<>();
        JsonValue t = StoryTriggers.find(s2.get("story"), flags, "enter", 0, 0, false);
        check(t != null && "intro2".equals(t.getString("scene")), "section_2: la escena 2.1 se dispara al entrar");
        StoryTriggers.consume(t, flags);
        check(StoryTriggers.find(s2.get("story"), flags, "enter", 0, 0, false) == null, "la escena 2.1 se ve una vez");
        check(json("levels/section_1_room_4.json").get("exit").getString("next").equals("levels/section_2.json"), "el nivel 1 debe llevar al Colector");

        // 2.2: en la plataforma del terminal, antes de la salida de la Camara de conmutacion.
        String room = "levels/section_2_room_3.json";
        JsonValue r3 = json(room);
        check("story/nivel_2.json".equals(r3.getString("storyFile", "")), room + ": storyFile");
        JsonValue z = r3.get("story").child;
        check("transmision".equals(z.getString("scene")) && "zone".equals(z.getString("on")) && z.getBoolean("grounded", false), room + ": zona de pie");
        check("frame2".equals(z.getString("grant", "")), room + ": la escena 2.2 concede el Salto Frame Doble");
        JsonValue exit = r3.get("exit");
        check(("scene:transmision").equals(exit.getString("requires", "")), room + ": la salida espera a la escena 2.2 (no se puede saltar)");
        float ex = exit.getFloat("x") + exit.getFloat("width") / 2, ey = exit.getFloat("y");
        check(ex >= z.getFloat("x") && ex <= z.getFloat("x") + z.getFloat("width"), room + ": la zona cubre la plataforma de la salida");
        PlayerBody probe = body(r3);
        float px = ex - PlayerBody.WIDTH / 2;
        check(!probe.isAreaFree(px, ey - .3f, PlayerBody.WIDTH, .3f), room + ": la salida apoya en la plataforma de la zona");
        Set<String> f2 = new HashSet<>();
        check(StoryTriggers.find(r3.get("story"), f2, "zone", ex, ey + .5f, false) == null, room + ": nunca en el aire");
        check(StoryTriggers.find(r3.get("story"), f2, "zone", ex, ey + .5f, true) == z, room + ": de pie junto al terminal");
        JsonValue h = z.get("hologram"), fp = z.get("focusPoint");
        check(h != null && fp != null && fp.size == 2, room + ": holograma y enfoque de camara");
        check(h.getInt("from") < h.getInt("until") && h.getInt("until") < tx.size(), room + ": lineas del holograma");
        check(tx.get(h.getInt("from") + 1).is("hologram") && tx.get(h.getInt("until")).text.contains("se disipa"), room + ": el holograma acompaña a Celestia y se disipa con el guion");
        check(probe.isAreaFree(h.getFloat("x") - .3f, h.getFloat("y") + .02f, .6f, 2.3f)
            && !probe.isAreaFree(h.getFloat("x") - .3f, h.getFloat("y") - .3f, .6f, .3f), room + ": el holograma se proyecta sobre la plataforma");
    }
    static String text(List<DialoguePlayer.Line> lines) {
        StringBuilder b = new StringBuilder();
        for (DialoguePlayer.Line l : lines) b.append(l.text).append('\n');
        return b.toString();
    }
}
