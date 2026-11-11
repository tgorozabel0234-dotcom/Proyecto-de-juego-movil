package io.github.some_example_name;

/**
 * Mini jefe del nivel 1, "El Capataz Brutal" (guion tecnico, nivel 1, seccion 3).
 * Logica pura sin graficos ni libGDX: el renderer solo lee estado y temporizadores.
 *
 * Patron:
 * - Fase 1, carga frontal: anuncia, corre en linea recta hasta el borde o una pared y
 *   queda expuesto. El frente esta blindado (Enemy.backArmor): solo la bateria trasera
 *   recibe dano. Un Salto Frame a traves de la carga deja al jugador a su espalda.
 * - Fase 2 (desde el segundo ciclo): golpea el suelo antes de cargar; una onda electrica
 *   recorre el suelo en ambos sentidos y se esquiva saltando.
 * - Victoria: destruir la bateria en 3 ciclos de vulnerabilidad. La vida se divide en
 *   tercios; el dano de un ciclo no puede pasar del tercio actual.
 *
 * Es stunImmune segun docs/gameplay/enemy-stun.md; recibe dano pero no se aturde.
 */
public final class ForemanBoss {
    public enum State { DORMANT, TELEGRAPH, CHARGE, EXPOSED, RECOVER, SLAM, DEFEATED }

    /** Valores por defecto; TestLevelScreen los reemplaza con types.json ("foreman"). */
    public float aggroRange = 15, telegraph = .75f, chargeSpeed = 12, chargeDistance = 16;
    public float exposedTime = 2.2f, recoverTime = .6f, chargeDamage = 2;
    public float slamWindup = .8f, waveSpeed = 11, waveRange = 12, waveHeight = .7f, waveDamage = 1;
    public int cycles = 3;

    public final CombatSystem.Enemy body;
    private State state = State.DORMANT;
    private float timer, travelled, waveRadius, waveOriginX, waveFloorY, fallSpeed;
    private boolean waveActive, waveHit, chargeHit;
    private int cycle;
    private boolean justDefeated;

    public ForemanBoss(CombatSystem.Enemy body) {
        this.body = body;
        body.backArmor = true;
        body.vulnerable = false;
    }

    public State getState() { return state; }
    public float getTimer() { return timer; }
    public int getCycle() { return cycle; }
    /** Fase del guion: 1 = carga frontal, 2 = onda de choque + carga. */
    public int getPhase() { return cycle == 0 ? 1 : 2; }
    public boolean isWaveActive() { return waveActive; }
    public float getWaveRadius() { return waveRadius; }
    public float getWaveOriginX() { return waveOriginX; }
    public float getWaveFloorY() { return waveFloorY; }
    public boolean isEngaged() { return state != State.DORMANT && state != State.DEFEATED; }
    /** Devuelve true una sola vez, en el paso en que el jefe cae. */
    public boolean consumeDefeat() { boolean d = justDefeated; justDefeated = false; return d; }

    /** Vida minima permitida en el ciclo actual (tercios de la vida maxima). */
    public float cycleFloor() {
        return Math.max(0, body.maxHealth * (cycles - cycle - 1) / (float) cycles);
    }

    /** Coloca el ciclo segun la vida (al cargar una partida guardada a mitad del combate). */
    public void reset() {
        state = body.alive() ? State.DORMANT : State.DEFEATED;
        timer = travelled = waveRadius = fallSpeed = 0;
        waveActive = waveHit = chargeHit = justDefeated = false;
        cycle = 0;
        while (cycle < cycles - 1 && body.health <= cycleFloor() + .001f) cycle++;
        body.vulnerable = false;
        body.state = body.alive() ? CombatSystem.EnemyState.IDLE : CombatSystem.EnemyState.DEAD;
    }

    public void update(float dt, PlayerBody p, PlayerVitals life) {
        if (state == State.DEFEATED) return;
        // El dano de un ciclo no atraviesa el tercio actual: obliga a 3 ventanas.
        // Se limita antes de comprobar la muerte para que un golpe enorme no salte ciclos.
        float floor = cycleFloor();
        if (body.health < floor) body.health = floor;
        if (!body.alive()) { defeat(); return; }
        if (cycle < cycles - 1 && body.health <= floor + .001f && body.vulnerable) endCycle();
        updateWave(dt, p, life);
        float px = p.x + PlayerBody.WIDTH / 2, py = p.y + PlayerBody.HEIGHT / 2;
        float dx = px - body.x;
        timer = Math.max(0, timer - dt);
        switch (state) {
            case DORMANT:
                if (life.alive() && Math.hypot(dx, py - body.y) <= aggroRange && p.hasCombatLine(body.x, body.y)) {
                    face(dx); begin(getPhase() == 2 ? State.SLAM : State.TELEGRAPH, getPhase() == 2 ? slamWindup : telegraph);
                }
                break;
            case SLAM:
                if (timer == 0) {
                    waveActive = true; waveHit = false; waveRadius = 0;
                    waveOriginX = body.x; waveFloorY = body.y - body.radius;
                    face(dx); begin(State.TELEGRAPH, telegraph);
                }
                break;
            case TELEGRAPH:
                if (timer > telegraph * .5f) face(dx); // fija la direccion en la mitad final
                if (timer == 0) { travelled = 0; chargeHit = false; body.vulnerable = true; state = State.CHARGE; }
                break;
            case CHARGE: {
                float step = body.facing * chargeSpeed * dt;
                boolean blocked = !groundAhead(p, step) || !moveX(p, step);
                travelled += Math.abs(step);
                if (!chargeHit && life.alive() && Math.abs(dx) < body.radius + PlayerBody.WIDTH / 2
                    && Math.abs(py - body.y) < body.radius + PlayerBody.HEIGHT / 2 && life.receiveDamage(chargeDamage)) {
                    chargeHit = true;
                    if (!life.alive()) p.cancelCombatTravel();
                }
                if (blocked || travelled >= chargeDistance) begin(State.EXPOSED, exposedTime);
                break;
            }
            case EXPOSED:
                body.vulnerable = true;
                if (timer == 0) { body.vulnerable = false; begin(State.RECOVER, recoverTime); }
                break;
            case RECOVER:
                body.vulnerable = false;
                if (timer == 0) {
                    face(dx);
                    if (!life.alive()) state = State.DORMANT;
                    else begin(getPhase() == 2 ? State.SLAM : State.TELEGRAPH, getPhase() == 2 ? slamWindup : telegraph);
                }
                break;
            default: break;
        }
        body.state = state == State.TELEGRAPH || state == State.SLAM ? CombatSystem.EnemyState.WINDUP
            : state == State.CHARGE ? CombatSystem.EnemyState.CHASE
            : state == State.EXPOSED ? CombatSystem.EnemyState.RECOVER : CombatSystem.EnemyState.IDLE;
        fallSpeed = Math.max(-30, fallSpeed - 26 * dt);
        if (!moveY(p, fallSpeed * dt)) fallSpeed = 0;
        if (body.y < -10) body.health = 0;
    }

    private void endCycle() {
        cycle++;
        body.vulnerable = false;
        begin(State.RECOVER, recoverTime);
    }

    private void defeat() {
        state = State.DEFEATED; body.vulnerable = false; waveActive = false;
        body.state = CombatSystem.EnemyState.DEAD; justDefeated = true;
    }

    private void begin(State next, float seconds) { state = next; timer = seconds; }
    private void face(float dx) { if (dx != 0) body.facing = dx < 0 ? -1 : 1; }

    private void updateWave(float dt, PlayerBody p, PlayerVitals life) {
        if (!waveActive) return;
        waveRadius += waveSpeed * dt;
        if (waveRadius >= waveRange) { waveActive = false; return; }
        float px = p.x + PlayerBody.WIDTH / 2;
        // Daño solo a la altura del suelo: saltar por encima de la onda la esquiva.
        boolean low = p.y <= waveFloorY + waveHeight && p.y + PlayerBody.HEIGHT >= waveFloorY - .1f;
        float front = Math.abs(px - waveOriginX);
        if (!waveHit && low && Math.abs(front - waveRadius) < .45f && life.receiveDamage(waveDamage)) {
            waveHit = true;
            if (!life.alive()) p.cancelCombatTravel();
        }
    }

    private boolean groundAhead(PlayerBody p, float step) {
        boolean supported = !p.isAreaFree(body.x - body.radius, body.y - body.radius - .04f, body.radius * 2, .04f);
        if (!supported) return true; // en el aire no se frena por bordes
        float foot = body.x + Math.signum(step) * (body.radius + Math.abs(step) + .05f);
        return !p.isAreaFree(foot - .025f, body.y - body.radius - .12f, .05f, .12f);
    }

    /** Movimiento con colision; no usa tryMove porque el jefe es stunImmune y nunca se bloquea. */
    private boolean moveX(PlayerBody p, float dx) { return axis(p, dx, 0); }
    private boolean moveY(PlayerBody p, float dy) { return axis(p, 0, dy); }
    private boolean axis(PlayerBody p, float dx, float dy) {
        if (dx == 0 && dy == 0) return true;
        float r = body.radius;
        if (p.isAreaFree(body.x + dx - r, body.y + dy - r, r * 2, r * 2)) { body.x += dx; body.y += dy; return true; }
        float lo = 0, hi = 1;
        for (int i = 0; i < 14; i++) {
            float mid = (lo + hi) / 2;
            if (p.isAreaFree(body.x + dx * mid - r, body.y + dy * mid - r, r * 2, r * 2)) lo = mid; else hi = mid;
        }
        body.x += dx * lo; body.y += dy * lo;
        return false;
    }
}
