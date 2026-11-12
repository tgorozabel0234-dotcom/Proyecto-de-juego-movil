package io.github.some_example_name;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.io.File;
import java.nio.file.Files;

/**
 * Recorrido de las ocho salas de campaña con la física real (PlayerBody), según
 * assets/levels/traversal_rules.json. Comprueba:
 * - Salir caminando por cualquier borde de una superficie nunca cae al vacío.
 * - No hay bordes pequeños entre superficies contiguas del camino (no se sube escalones).
 * - Entradas, llegadas de portales y puertas apoyan sobre una superficie libre.
 * - Los muros laterales superan el alcance del gancho (no se sale del mapa).
 */
public final class LevelFlowCheck {
    static final float DT = 1f / 240;
    static File assets() { File a = new File("assets"); return a.isDirectory() ? a : new File("../assets"); }
    static JsonValue json(String path) {
        try { return new JsonReader().parse(new String(Files.readAllBytes(new File(assets(), path).toPath()), "UTF-8")); }
        catch (Exception e) { throw new AssertionError("No se puede leer " + path, e); }
    }
    static void check(boolean ok, String msg) { if (!ok) throw new AssertionError(msg); }

    static PlayerBody body(JsonValue map) {
        PlayerBody p = new PlayerBody();
        for (JsonValue q = map.get("pieces").child; q != null; q = q.next)
            p.addSolid(q.getFloat("x"), q.getFloat("y"), q.getFloat("width"), q.getFloat("height"), "pipe".equals(q.getString("role", "")));
        return p;
    }

    public static void main(String[] args) {
        JsonValue rules = json("levels/traversal_rules.json");
        float deathY = rules.get("walkOff").getFloat("deathY"), seconds = rules.get("walkOff").getFloat("seconds");
        float lipTol = rules.get("surfaces").getFloat("lipTolerance"), lipMax = rules.get("surfaces").getFloat("lipMax");
        float wallTop = rules.getFloat("boundaryWallMinTop");
        int edges = 0;
        for (JsonValue r = rules.get("rooms").child; r != null; r = r.next) {
            String room = r.asString();
            JsonValue map = json(room);
            PlayerBody probe = body(map);
            float width = map.getFloat("width");
            // 1. Bordes: caminar fuera de cada extremo de cada superficie apoyable.
            for (JsonValue q = map.get("pieces").child; q != null; q = q.next) {
                String role = q.getString("role", "");
                float x = q.getFloat("x"), w = q.getFloat("width"), top = q.getFloat("y") + q.getFloat("height");
                if (role.equals("anchor") || role.equals("pillar")) continue;
                if (role.equals("wall")) {
                    if (x <= 0 || x >= width - .01f) check(top >= wallTop, room + ": muro lateral bajo (" + top + "), el gancho permite salir del mapa");
                    continue;
                }
                if ("jumpGaps".equals(map.getString("traversal", ""))) continue;
                for (int dir : new int[]{-1, 1}) {
                    float sx = dir < 0 ? x + .05f : x + w - PlayerBody.WIDTH - .05f;
                    if (w < PlayerBody.WIDTH + .1f || !probe.isAreaFree(sx, top + .02f, PlayerBody.WIDTH, PlayerBody.HEIGHT)) continue;
                    PlayerBody p = body(map);
                    p.reset(sx, top + .01f);
                    float minY = p.y;
                    for (float t = 0; t < seconds; t += DT) { p.update(DT, dir, false, false, false, false); minY = Math.min(minY, p.y); }
                    check(minY > deathY, room + ": salir caminando por el borde " + (dir < 0 ? "izquierdo" : "derecho")
                        + " de x" + x + "-" + (x + w) + " (alto " + top + ") cae al vacio");
                    edges++;
                }
            }
            if ("jumpGaps".equals(map.getString("traversal", ""))) checkJumps(map,room);
            // 2. Bordes pequeños entre superficies contiguas (las redes de recuperación se exceptúan).
            for (JsonValue a = map.get("pieces").child; a != null; a = a.next)
                for (JsonValue b = a.next; b != null; b = b.next) {
                    if (skip(a) || skip(b) || a.getBoolean("suspended", false) || b.getBoolean("suspended", false)) continue;
                    float ta = a.getFloat("y") + a.getFloat("height"), tb = b.getFloat("y") + b.getFloat("height");
                    float d = Math.abs(ta - tb);
                    float touch = Math.min(a.getFloat("x") + a.getFloat("width"), b.getFloat("x") + b.getFloat("width")) - Math.max(a.getFloat("x"), b.getFloat("x"));
                    check(!(d > lipTol && d < lipMax && touch >= -.06f), room + ": borde de " + d + " entre x" + a.getFloat("x") + " y x" + b.getFloat("x") + " atasca al caminar");
                }
            // 3. Entrada, puertas y llegadas.
            stands(probe, map.get("spawn"), room + " entrada");
            door(probe, map.get("exit"), room + " salida");
            if (map.has("portals")) for (JsonValue d = map.get("portals").child; d != null; d = d.next) {
                door(probe, d, room + " portal");
                if (d.has("arrival") && d.getString("next", "").startsWith("levels/"))
                    stands(body(json(d.getString("next"))), d.get("arrival"), d.getString("next") + " llegada desde " + room);
            }
            JsonValue exit = map.get("exit");
            if (exit.has("arrival")) stands(body(json(exit.getString("next"))), exit.get("arrival"), exit.getString("next") + " llegada desde " + room);
        }
        System.out.println("PASS: 8 salas, " + edges + " bordes seguros y cruces de salto verificados, sin bordes que atasquen, entradas y llegadas apoyadas");
    }

    static void checkJumps(JsonValue map,String room) {
        java.util.List<JsonValue> floors=new java.util.ArrayList<>();
        for(JsonValue q=map.get("pieces").child;q!=null;q=q.next)
            if(q.getBoolean("foundation",false)) floors.add(q);
        floors.sort(java.util.Comparator.comparingDouble(q->q.getFloat("x")));
        for(int i=1;i<floors.size();i++) {
            JsonValue from=floors.get(i-1),to=floors.get(i);
            boolean landed=false;
            for(int attempt=0;attempt<6;attempt++) {
                float back=new float[]{.2f,1f,2f}[attempt%3];
                PlayerBody p=body(map);
                p.reset(from.getFloat("x")+from.getFloat("width")-PlayerBody.WIDTH-back,
                    from.getFloat("y")+from.getFloat("height"));
                for(int step=0;step<8;step++)p.update(DT,0,false,false,false,true);
                for(int step=0;step<480;step++) {
                    p.update(DT,1,step==0,step==0 && attempt>=3,attempt>=3,true);
                    float top=to.getFloat("y")+to.getFloat("height");
                    if(p.x>=to.getFloat("x") && p.x+PlayerBody.WIDTH<=to.getFloat("x")+to.getFloat("width")
                        && Math.abs(p.y-top)<.03f) {landed=true;break;}
                }
                if(landed)break;
            }
            check(landed,room+": salto entre "+from.getFloat("x")+" y "+to.getFloat("x")+" inaccesible");
        }
    }

    static boolean skip(JsonValue q) { String r = q.getString("role", ""); return r.equals("anchor") || r.equals("wall") || r.equals("pillar"); }

    static void stands(PlayerBody p, JsonValue at, String what) {
        float x = at.getFloat("x"), y = at.getFloat("y");
        check(p.isAreaFree(x, y + .02f, PlayerBody.WIDTH, PlayerBody.HEIGHT), what + ": punto ocupado por un solido");
        check(!p.isAreaFree(x, y - .3f, PlayerBody.WIDTH, .3f), what + ": no hay suelo bajo el punto");
    }
    static void door(PlayerBody p, JsonValue d, String what) {
        float x = d.getFloat("x"), y = d.getFloat("y"), w = d.getFloat("width");
        check(!p.isAreaFree(x, y - .1f, w, .1f), what + ": la puerta no apoya en una superficie");
        check(p.isAreaFree(x + w / 2 - PlayerBody.WIDTH / 2, y + .02f, PlayerBody.WIDTH, PlayerBody.HEIGHT), what + ": puerta tapada");
    }
}
