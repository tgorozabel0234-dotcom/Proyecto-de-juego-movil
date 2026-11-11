package io.github.some_example_name;

import java.util.List;

/**
 * Salto Frame nivel I (guion del nivel 1). No agrega botones: un dash que alcanza a un
 * enemigo vivo hace reaparecer al jugador a su espalda, mirando hacia el. Sin graficos:
 * TestLevelScreen lee {@link #getSlowRemaining()} para el bullet-time y el destello.
 *
 * Reglas: una sola vez por dash; solo si el destino esta libre y hay linea de combate;
 * no aplica dano ni aturdimiento (el golpe sigue siendo del jugador); da una breve
 * intangibilidad. Los valores se cargan desde assets/combat/prototype/frame_jump.json.
 */
public final class FrameJump {
    public boolean enabled;
    /** Distancia extra, mas alla del radio, a la que el dash "alcanza" al enemigo. */
    public float contactMargin = .45f;
    /** Separacion entre la espalda del enemigo y el jugador al reaparecer. */
    public float exitGap = .3f;
    public float intangibility = .35f;
    /** Bullet-time: duracion en segundos reales y escala de tiempo aplicada. */
    public float slowDuration = .5f, slowScale = .35f;

    private boolean usedThisDash;
    private float slowRemaining;
    private float fromX, fromY, toX, toY, trailRemaining;
    private int count;

    public void reset() { usedThisDash = false; slowRemaining = trailRemaining = 0; }
    public float getSlowRemaining() { return slowRemaining; }
    /** Escala de tiempo de simulacion actual (1 sin bullet-time). */
    public float timeScale() { return slowRemaining > 0 ? slowScale : 1; }
    public float getTrailRemaining() { return trailRemaining; }
    public float getFromX() { return fromX; }
    public float getFromY() { return fromY; }
    public float getToX() { return toX; }
    public float getToY() { return toY; }
    public int getCount() { return count; }

    /** Avanza los efectos en tiempo real (no se ralentizan a si mismos). */
    public void updateRealTime(float realDt) {
        slowRemaining = Math.max(0, slowRemaining - realDt);
        trailRemaining = Math.max(0, trailRemaining - realDt);
    }

    /** Se invoca por subpaso, despues de la fisica del jugador. Devuelve el enemigo superado. */
    public CombatSystem.Enemy update(PlayerBody p, PlayerVitals life, List<CombatSystem.Enemy> enemies) {
        if (!p.isDashing()) { usedThisDash = false; return null; }
        if (!enabled || usedThisDash || !life.alive()) return null;
        float dir = p.getVelocityX() != 0 ? Math.signum(p.getVelocityX()) : p.getFacing();
        float cx = p.x + PlayerBody.WIDTH / 2, cy = p.y + PlayerBody.HEIGHT / 2;
        CombatSystem.Enemy best = null;
        float nearest = Float.MAX_VALUE;
        for (CombatSystem.Enemy e : enemies) {
            if (!e.alive()) continue;
            float ahead = (e.x - cx) * dir;
            float reach = e.radius + PlayerBody.WIDTH / 2 + contactMargin;
            if (ahead <= 0 || ahead > reach) continue;
            if (Math.abs(e.y - cy) > e.radius + PlayerBody.HEIGHT / 2 + .2f) continue;
            if (!p.hasCombatLine(e.x, e.y)) continue;
            if (ahead < nearest) { nearest = ahead; best = e; }
        }
        if (best == null) return null;
        float destCenter = best.x + dir * (best.radius + PlayerBody.WIDTH / 2 + exitGap);
        float nx = destCenter - PlayerBody.WIDTH / 2, ny = p.y;
        // Un destino bloqueado (pared, borde con techo) cancela el salto: el dash sigue normal.
        if (!p.isAreaFree(nx, ny, PlayerBody.WIDTH, PlayerBody.HEIGHT)) return null;
        usedThisDash = true;
        fromX = cx; fromY = cy; toX = destCenter; toY = cy;
        p.frameShift(nx, ny, -dir);
        life.grantInvulnerability(intangibility);
        slowRemaining = slowDuration;
        trailRemaining = slowDuration;
        count++;
        return best;
    }
}
