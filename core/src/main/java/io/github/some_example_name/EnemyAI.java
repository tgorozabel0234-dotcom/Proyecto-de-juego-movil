package io.github.some_example_name;

import java.util.List;

/** IA sencilla: perseguir, anunciar golpe, golpear y recuperarse. No busca rutas. */
public final class EnemyAI {
    public static final class Profile {
        public float speed, detectionRange, attackRange, damage, windup, recovery;
        public Profile(float speed,float detection,float range,float damage,float windup,float recovery) {
            this.speed=speed;detectionRange=detection;attackRange=range;this.damage=damage;
            this.windup=windup;this.recovery=recovery;
        }
    }
    public final Profile melee=new Profile(3,12,1.25f,1,0.35f,0.9f);
    public final Profile flying=new Profile(3.5f,16,1.35f,1,0.45f,1.1f);
    public void update(float dt,PlayerBody p,PlayerVitals life,List<CombatSystem.Enemy> enemies) {
        for(CombatSystem.Enemy e:enemies) {
            if(e.type==CombatSystem.EnemyType.DUMMY) continue;
            if(!e.alive()) { e.state=CombatSystem.EnemyState.DEAD;continue; }
            // No hay rutas alternativas que muevan o ataquen durante el stun.
            if(!e.canAct()) { e.state=CombatSystem.EnemyState.STUNNED;continue; }
            if(!life.alive()) { e.state=CombatSystem.EnemyState.IDLE;continue; }
            Profile profile=e.type==CombatSystem.EnemyType.FLYING?flying:melee;
            e.recovery=Math.max(0,e.recovery-dt);
            float dx=p.x+PlayerBody.WIDTH/2-e.x,dy=p.y+PlayerBody.HEIGHT/2-e.y;
            float distance=(float)Math.hypot(dx,dy);
            boolean visible=distance<=profile.detectionRange && p.hasCombatLine(e.x,e.y);
            boolean inRange=visible&&distance<=profile.attackRange+e.radius;
            if(e.windupRemaining>0) {
                e.state=CombatSystem.EnemyState.WINDUP;
                e.windupRemaining=Math.max(0,e.windupRemaining-dt);
                if(e.windupRemaining==0) {
                    // Se puede esquivar: no se garantiza el golpe al iniciarlo.
                    if(inRange && life.receiveDamage(profile.damage) && !life.alive()) p.cancelCombatTravel();
                    e.recovery=profile.recovery;e.state=CombatSystem.EnemyState.RECOVER;
                }
            } else if(inRange && e.recovery<=0) {
                e.facing=dx<0?-1:1;e.windupRemaining=profile.windup;e.state=CombatSystem.EnemyState.WINDUP;
            } else if(visible && !inRange) {
                e.state=CombatSystem.EnemyState.CHASE;e.facing=dx<0?-1:1;
                if(e.type==CombatSystem.EnemyType.FLYING) {
                    move(e,p,dx/Math.max(distance,.001f)*profile.speed*dt,dy/Math.max(distance,.001f)*profile.speed*dt);
                } else {
                    boolean supported=!p.isAreaFree(e.x-e.radius,e.y-e.radius-.04f,e.radius*2,.04f);
                    float step=dx<0?-profile.speed*dt:profile.speed*dt;
                    // Evita abandonar una plataforma; no salta ni atraviesa paredes.
                    float foot=e.x+(step<0?-1:1)*(e.radius+Math.abs(step)+.05f);
                    boolean groundAhead=!p.isAreaFree(foot-.025f,e.y-e.radius-.12f,.05f,.12f);
                    if(!supported||groundAhead) move(e,p,step,0);
                }
            } else e.state=e.recovery>0?CombatSystem.EnemyState.RECOVER:CombatSystem.EnemyState.IDLE;
            if(e.type==CombatSystem.EnemyType.MELEE) {
                e.fallSpeed=Math.max(-30,e.fallSpeed-26*dt);
                if(!move(e,p,0,e.fallSpeed*dt)) e.fallSpeed=0;
                if(e.y < -10) e.health=0;
            }
        }
    }
    private boolean move(CombatSystem.Enemy e,PlayerBody p,float dx,float dy) {
        if(!e.canAct()) return false;
        boolean x=axis(e,p,dx,0),y=axis(e,p,0,dy);return x&&y;
    }
    private boolean axis(CombatSystem.Enemy e,PlayerBody p,float dx,float dy) {
        if(dx==0&&dy==0) return true;
        if(p.isAreaFree(e.x+dx-e.radius,e.y+dy-e.radius,e.radius*2,e.radius*2)) return e.tryMove(dx,dy);
        float lo=0,hi=1;
        for(int i=0;i<14;i++) {
            float mid=(lo+hi)/2;
            if(p.isAreaFree(e.x+dx*mid-e.radius,e.y+dy*mid-e.radius,e.radius*2,e.radius*2))lo=mid;
            else hi=mid;
        }
        e.tryMove(dx*lo,dy*lo);return false;
    }
}
