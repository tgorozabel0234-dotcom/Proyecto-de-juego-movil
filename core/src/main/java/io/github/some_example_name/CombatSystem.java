package io.github.some_example_name;

import java.util.ArrayList;
import java.util.List;

/** Combate provisional sin dependencias graficas; K se resuelve aqui, no en el gancho. */
public final class CombatSystem {
    public enum EnemyType { DUMMY, MELEE, FLYING }
    public enum EnemyState { IDLE, CHASE, WINDUP, RECOVER, STUNNED, DEAD }
    public enum Attack { NONE, SWEEP, UP, THRUST, DOWN, HOOK_STRIKE, WHIRLWIND, AURA }
    public static final class Enemy {
        public final float spawnX, spawnY, radius, maxHealth;
        public float x, y, health, flash;
        public final EnemyType type;
        public EnemyState state=EnemyState.IDLE;
        public float windupRemaining, recovery, fallSpeed, facing=1;
        public final boolean stunImmune;
        private float stunRemaining;
        private int lastHit = -1;
        public Enemy(float x, float y, float radius, float health) {
            this(x,y,radius,health,false);
        }
        public Enemy(float x, float y, float radius, float health, boolean stunImmune) {
            this(x,y,radius,health,stunImmune,EnemyType.DUMMY);
        }
        public Enemy(float x,float y,float radius,float health,boolean stunImmune,EnemyType type) {
            this.type=type;
            this.stunImmune=stunImmune;
            this.spawnX=x; this.spawnY=y; this.radius=radius; this.maxHealth=health;
            reset();
        }
        void reset() { x=spawnX; y=spawnY; health=maxHealth; flash=stunRemaining=windupRemaining=recovery=fallSpeed=0; facing=1; state=EnemyState.IDLE; lastHit=-1; }
        public boolean isStunned() { return alive() && stunRemaining>0; }
        public float getStunRemaining() { return stunRemaining; }
        public boolean canAct() { return alive() && !isStunned(); }
        /** Punto de entrada para el movimiento de la futura IA. */
        public boolean tryMove(float dx,float dy) {
            if(!canAct()) return false;
            x+=dx; y+=dy; return true;
        }
        public void receiveHit(float damage,float stunDuration) {
            if(!alive()) return;
            health=Math.max(0,health-Math.max(0,damage)); flash=0.18f;
            if(!stunImmune||!alive()) windupRemaining=0;
            stunRemaining=alive()&&!stunImmune?Math.max(0,stunDuration):0;
        }
        private void update(float dt) {
            flash=Math.max(0,flash-dt); stunRemaining=Math.max(0,stunRemaining-dt);
        }
        public boolean alive() { return health>0; }
    }
    public final List<Enemy> enemies = new ArrayList<>();
    public Attack attack=Attack.NONE;
    public float elapsed, auraRemaining, attackFacing=1;
    public float duration=0.32f, reach=1.65f, baseDamage=10, auraMultiplier=1.5f;
    /** Medidas del área de ataque, independientes del collider de movimiento. */
    public float attackOriginHeight=PlayerBody.HEIGHT/2, downReach=-1, whirlwindContactRadius=.75f;
    private float cooldown, heldTime, thrustImpulse;
    public float stunDuration=0.75f;
    public float reboundMultiplier=0.85f;
    private boolean deciding, wasHeld, hitResolved;
    private int serial;
    public float bounceSpeed=20, hookRiseSpeed=18, thrustMinimum=18, thrustMaximum=32;
    private Enemy pulled;
    public void reset() {
        attack=Attack.NONE; elapsed=auraRemaining=cooldown=heldTime=thrustImpulse=0;
        deciding=wasHeld=hitResolved=false; pulled=null; serial=0;
        for (Enemy e:enemies) e.reset();
    }
    public boolean isPulling() { return pulled!=null; }
    /** Eventos de solo lectura para el renderer; no disparan impulsos ni daño. */
    public int getAttackSerial() { return serial; }
    public boolean hasResolvedImpact() { return hitResolved; }
    public Enemy getPulledEnemy() { return pulled; }
    public float progress() { return Math.min(1,elapsed/duration); }
    public boolean damaging() { return attack!=Attack.NONE && attack!=Attack.AURA && attack!=Attack.WHIRLWIND
        && elapsed>=0.04f && elapsed<=duration*0.85f; }
    private void start(Attack next, PlayerBody p) {
        attack=next; elapsed=0; serial++; hitResolved=false; attackFacing=p.getFacing();
        cooldown=duration+0.08f;
        if (next==Attack.AURA) auraRemaining=4;
        if (next==Attack.THRUST) { pulled=null; thrustImpulse=p.prepareRisingThrust(thrustMinimum,thrustMaximum); }
    }
    /** Prioriza al enemigo dentro del corredor del gancho y antes de la superficie. */
    public boolean tryPull(PlayerBody p,float ax,float ay) {
        if(p.isGrounded()) return false;
        float norm=(float)Math.hypot(ax,ay); if(norm<0.01f) return false;
        ax/=norm; ay/=norm;
        PlayerBody.HookTarget surface=p.findHookTarget(ax,ay);
        float max=surface==null?PlayerBody.HOOK_RANGE:(float)Math.hypot(surface.x-p.x-PlayerBody.WIDTH/2,surface.y-p.y-PlayerBody.HEIGHT/2);
        Enemy best=null; float nearest=max;
        for(Enemy e:enemies) if(e.alive()) {
            float dx=e.x-p.x-PlayerBody.WIDTH/2, dy=e.y-p.y-PlayerBody.HEIGHT/2;
            float projection=dx*ax+dy*ay;
            float distance=(float)Math.hypot(dx,dy);
            if(distance<=PlayerBody.HOOK_RANGE && projection>0.5f && projection<nearest
                && Math.abs(dx*ay-dy*ax)<=e.radius+0.6f && p.hasCombatLine(e.x,e.y)) {
                best=e; nearest=projection;
            }
        }
        if(best==null) return false;
        if(!p.launchPropulsionTo(best.x,best.y)) return false;
        pulled=best;
        return true;
    }
    /** Se invoca por subpaso junto con la fisica para detectar golpes a alta velocidad. */
    public void update(float dt, PlayerBody p, boolean pressed, boolean held, boolean up, boolean down) {
        cooldown=Math.max(0,cooldown-dt); auraRemaining=Math.max(0,auraRemaining-dt);
        for(Enemy e:enemies) e.update(dt);
        if(pressed || (held&&!wasHeld)) {
            if(cooldown<=0 && attack==Attack.NONE && !deciding) {
                if(p.isHooked()) { deciding=true; heldTime=0; }
                else if(down && p.isGrounded()) start(Attack.AURA,p);
                else if(down) start(Attack.DOWN,p);
                else if(up) start(Attack.UP,p);
                else if(p.canStartGroundThrust() && (p.isDashing()||p.hasPendingDash()||p.isBoosted())) start(Attack.THRUST,p);
                else start(Attack.SWEEP,p);
            }
        }
        if(deciding) {
            heldTime+=dt;
            if(!held) { deciding=false; start(Attack.HOOK_STRIKE,p); }
            else if(heldTime>=0.18f) { deciding=false; start(p.isHooked()?Attack.WHIRLWIND:Attack.SWEEP,p); }
        }
        wasHeld=held;
        if(attack!=Attack.NONE) {
            elapsed+=dt;
            if(attack==Attack.WHIRLWIND) {
                if(!held||!p.isHooked()) { attack=Attack.NONE; cooldown=0.12f; }
            } else if(elapsed>=duration) attack=Attack.NONE;
        }
        if(pulled!=null) {
            if(p.isPropelling()) p.retargetPropulsion(pulled.x,pulled.y);
            float dx=pulled.x-p.x-PlayerBody.WIDTH/2, dy=pulled.y-p.y-PlayerBody.HEIGHT/2;
            float distance=(float)Math.hypot(dx,dy);
            if(!pulled.alive() || !p.hasCombatLine(pulled.x,pulled.y)) {
                p.cancelCombatTravel(); pulled=null;
            } else if(distance<=pulled.radius+0.7f) {
                p.combatImpulse(0,hookRiseSpeed*reboundMultiplier,true);
                p.refreshAttackAirJump(); pulled=null;
            } else if(!p.isPropelling()) pulled=null; // Cancelado por salto, dash o colision.
        }
        if(!damaging() && attack!=Attack.WHIRLWIND) return;
        for(Enemy e:enemies) {
            if(!e.alive()||e.lastHit==serial) continue;
            float dx=e.x-p.x-PlayerBody.WIDTH/2,dy=e.y-p.y-attackOriginHeight;
            float distance=(float)Math.hypot(dx,dy);
            boolean contact;
            if(attack==Attack.WHIRLWIND) contact=distance<=e.radius+whirlwindContactRadius;
            else if(attack==Attack.DOWN) {float verticalReach=downReach>0?downReach:reach;contact=dy<0 && -dy<verticalReach+e.radius && Math.abs(dx)<verticalReach*0.8f+e.radius;}
            else if(attack==Attack.UP) contact=dy>0 && dy<reach+e.radius && Math.abs(dx)<reach*0.8f+e.radius;
            else contact=dx*attackFacing>=-e.radius && distance<reach+e.radius;
            if(!contact||!p.hasCombatLine(e.x,e.y)) continue;
            e.lastHit=serial;
            // El torbellino solo tiene animacion; el contacto suelta sin alterar el momentum.
            if(attack==Attack.WHIRLWIND) { p.releaseHook(); attack=Attack.NONE; cooldown=0.12f; break; }
            e.receiveHit(baseDamage*(auraRemaining>0?auraMultiplier:1),stunDuration);
            if(!hitResolved && (attack==Attack.DOWN||attack==Attack.HOOK_STRIKE||attack==Attack.THRUST)) {
                pulled=null;
                if(attack==Attack.THRUST) p.risingThrustImpact(thrustImpulse*reboundMultiplier);
                else {
                    p.combatImpulse(0,bounceSpeed*reboundMultiplier,true);
                    if(attack==Attack.DOWN) p.refreshAttackAirJump();
                }
                hitResolved=true;
            }
        }
    }
}
