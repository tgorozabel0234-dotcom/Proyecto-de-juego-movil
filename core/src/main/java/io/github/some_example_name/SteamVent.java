package io.github.some_example_name;

import com.badlogic.gdx.utils.JsonValue;

/** La luz de aviso, la columna visible y el contacto usan el mismo reloj de simulación. */
final class SteamVent {
    static float clock(JsonValue vent,float time) {
        return (time+vent.getFloat("phase"))%vent.getFloat("period");
    }
    static void contact(JsonValue vents,float time,PlayerBody body,PlayerVitals life) {
        if (vents==null || !life.alive() || life.isInvulnerable() || body.isDashing()) return;
        for(JsonValue v=vents.child;v!=null;v=v.next) {
            float x=v.getFloat("x"),y=v.getFloat("y");
            if(clock(v,time)<v.getFloat("blast") && body.x+PlayerBody.WIDTH>x && body.x<x+v.getFloat("width")
                && body.y+PlayerBody.HEIGHT>y && body.y<y+v.getFloat("height")) {
                life.receiveDamage(1);
                return;
            }
        }
    }
}
