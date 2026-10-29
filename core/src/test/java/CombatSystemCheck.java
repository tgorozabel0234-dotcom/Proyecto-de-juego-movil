import io.github.some_example_name.CombatSystem;
import io.github.some_example_name.PlayerBody;

public class CombatSystemCheck {
    private static void check(boolean condition,String message) {
        if(!condition) throw new AssertionError(message);
    }
    private static PlayerBody body(boolean floor) {
        PlayerBody p=new PlayerBody(); if(floor)p.addSolid(-100,-2,200,2);
        p.reset(0,floor?0:5);p.update(1f/60,0,false);return p;
    }
    private static void advance(CombatSystem c,PlayerBody p,float seconds,boolean held) {
        for(int i=0;i<(int)(seconds*240);i++)c.update(1f/240,p,false,held,false,false);
    }
    public static void main(String[] args) {
        for(int fps:new int[]{30,60,144,240}) {
            PlayerBody p=body(true); CombatSystem c=new CombatSystem();
            CombatSystem.Enemy e=new CombatSystem.Enemy(1,0.5f,.4f,100);c.enemies.add(e);
            for(int i=0;i<fps/2;i++) c.update(1f/fps,p,i==0,i<fps/4,false,false);
            check(e.health==90,"Un golpe por pulsacion a "+fps);
        }
        PlayerBody p=body(true); CombatSystem c=new CombatSystem();
        c.update(.01f,p,true,true,false,true);
        check(c.attack==CombatSystem.Attack.AURA&&c.auraRemaining>0,"Aura en suelo");
        advance(c,p,.5f,false);
        CombatSystem.Enemy e=new CombatSystem.Enemy(1,.5f,.4f,100); c.enemies.add(e);
        c.update(.01f,p,true,true,false,false);advance(c,p,.2f,false);
        check(e.health==85,"Aura aumenta dano");

        p=body(false);c=new CombatSystem();
        e=new CombatSystem.Enemy(.25f,4.5f,.4f,100);c.enemies.add(e);
        c.update(.01f,p,true,true,false,true);advance(c,p,.1f,false);
        check(e.health==90&&Math.abs(p.getVelocityY()-17)<.001f,"Barrido inferior rebota al impactar");
        p=body(false);c=new CombatSystem();float vy=p.getVelocityY();
        c.update(.01f,p,true,true,false,true);advance(c,p,.1f,false);
        check(p.getVelocityY()==vy,"Sin enemigo no hay rebote infinito");

        p=body(true);p.update(.01f,1,false,true,true,true);c=new CombatSystem();
        c.update(.01f,p,true,true,false,false);
        check(c.attack==CombatSystem.Attack.THRUST&&p.getVelocityY()<=0,"Estocada fallida no eleva al comenzar");
        float runningX=p.x;
        check(p.getVelocityX()>20&&p.isDashing(),"Estocada conserva el dash al comenzar");
        for(int i=0;i<24;i++) { p.update(1f/240,1,false,false,true,true);c.update(1f/240,p,false,true,false,false); }
        check(p.x>runningX+2&&p.getVelocityX()>20&&p.getVelocityY()<=0,"Estocada corre hacia delante sin elevacion al fallar");
        e=new CombatSystem.Enemy(p.x+.9f,.5f,.4f,100);c.enemies.add(e);
        c.update(1f/240,p,false,true,false,false);
        check(Math.abs(p.getVelocityY()-18.7f)<.001f,"Convierte velocidad reservada solo al impactar");
        check(p.getVelocityX()==0&&!p.isDashing(),"Solo al impactar convierte avance en ascenso");
        check(e.isStunned()&&Math.abs(e.getStunRemaining()-.75f)<.0001f,"Golpe aplica 0.75 s al enemigo");

        for(int side:new int[]{-1,1}) {
            p=body(false);p.addSolid(-30,12,60,1);check(p.launchPendulum(0,1),"Anclaje");
            c=new CombatSystem();c.update(.01f,p,true,true,false,false);
            advance(c,p,.2f,true);check(c.attack==CombatSystem.Attack.WHIRLWIND,"K mantenida");
            float vx=p.getVelocityX(), oldVy=p.getVelocityY();
            e=new CombatSystem.Enemy(p.x+.25f+side*.5f,p.y+.5f,.3f,100);c.enemies.add(e);
            c.update(.01f,p,false,true,false,false);
            check(!p.isHooked()&&e.health==100,"Torbellino suelta sin dano");
            check(vx==p.getVelocityX()&&oldVy==p.getVelocityY(),"Conserva vector momentum");

            p=body(false);p.addSolid(-30,12,60,1);p.launchPendulum(0,1);c=new CombatSystem();
            c.update(.01f,p,true,true,false,false);c.update(.01f,p,false,false,false,false);
            check(p.isHooked()&&c.attack==CombatSystem.Attack.HOOK_STRIKE,"Tap no suelta antes del impacto");
            e=new CombatSystem.Enemy(p.x+.25f+.8f,p.y+.5f,.3f,100);c.enemies.add(e);
            advance(c,p,.08f,false);
            check(!p.isHooked()&&Math.abs(p.getVelocityY()-17)<.001f&&e.health==90,"Tap impacto suelta y eleva");
        }
        p=body(false);c=new CombatSystem();
        e=new CombatSystem.Enemy(.25f,6.5f,.3f,100);c.enemies.add(e);
        c.update(.01f,p,true,true,true,false);advance(c,p,.1f,false);
        check(e.health==90,"Barrido superior");
        p=body(true);p.update(.01f,-1,false);c=new CombatSystem();
        e=new CombatSystem.Enemy(-1,.5f,.3f,100);c.enemies.add(e);
        c.update(.01f,p,true,true,false,false);advance(c,p,.1f,false);
        check(e.health==90,"Barrido izquierdo");
        p=body(false);p.addSolid(.7f,0,.2f,10);c=new CombatSystem();
        e=new CombatSystem.Enemy(1.3f,5.5f,.3f,100);c.enemies.add(e);
        c.update(.01f,p,true,true,false,false);advance(c,p,.1f,false);
        check(e.health==100,"No golpea a traves de pared");
        for(int fps:new int[]{30,60,144,240}) {
            p=body(true);c=new CombatSystem();e=new CombatSystem.Enemy(.9f,1.8f,.4f,100);c.enemies.add(e);
            for(int frame=0;frame<fps/3;frame++) {
                int steps=(int)Math.ceil(240f/fps);float dt=1f/fps/steps;
                for(int step=0;step<steps;step++) {
                    boolean first=frame==0&&step==0;
                    p.update(dt,1,false,first,true,true);
                    c.update(dt,p,first,frame<fps/4,false,false);
                }
            }
            check(e.health==90,"Estocada ascendente golpea a "+fps);
            check(p.y>3.5f,"Elevacion de estocada visible a "+fps);
        }
        p=body(false);c=new CombatSystem();e=new CombatSystem.Enemy(5,5.5f,.4f,100);c.enemies.add(e);
        check(c.tryPull(p,1,0)&&p.isPropelling(),"U lleva al jugador hacia el enemigo");
        check(!p.hasAttackAirJump(),"Disparar U no restaura salto antes de llegar");
        float startX=p.x;
        boolean checkedHookRise=false;
        for(int i=0;i<120;i++) {
            boolean traveling=c.isPulling();
            p.update(1f/240,0,false); c.update(1f/240,p,false,false,false,false);
            if(traveling&&!c.isPulling()) {
                checkedHookRise=true;
                check(Math.abs(p.getVelocityY()-15.3f)<.001f,"Gancho aplica potencia reducida exactamente al llegar");
            }
        }
        check(e.x==5 && e.y==5.5f,"El enemigo permanece inmovil");
        check(p.x>startX+2&&!c.isPulling(),"Jugador llega al objetivo");
        check(checkedHookRise&&p.getVelocityY()>0,"Llegada al enemigo impulsa arriba");
        check(p.hasAttackAirJump(),"Gancho contra enemigo restaura salto");
        float hookVy=p.getVelocityY();p.update(1f/60,0,true,false,false,true);
        check(!p.hasAttackAirJump()&&p.getVelocityY()>hookVy,"H consume el salto restaurado por U");
        advance(c,p,.1f,false);
        check(!p.hasAttackAirJump(),"Llegada no restaura continuamente el salto");
        for(int fps:new int[]{30,60,144,240}) for(int side:new int[]{-1,1}) {
            p=body(false); c=new CombatSystem();
            e=new CombatSystem.Enemy(p.x+.25f+side*10,p.y+6,.4f,100);c.enemies.add(e);
            check(c.tryPull(p,side,0.55f),"Diagonal hacia enemigo");
            boolean arrived=false;
            for(int frame=0;frame<fps;frame++) {
                int steps=(int)Math.ceil(240f/fps);float dt=1f/fps/steps;
                for(int step=0;step<steps;step++) {
                    boolean traveling=c.isPulling();
                    p.update(dt,0,false);c.update(dt,p,false,false,false,false);
                    if(traveling&&!c.isPulling()) {
                        arrived=true;check(p.getVelocityY()>=15.299f,"Ascenso en llegada");
                        check(p.hasAttackAirJump(),"Salto restaurado en ambas diagonales");
                    }
                }
            }
            check(arrived,"Llega en ambas diagonales a "+fps);
        }
        p=body(false);p.addSolid(2,0,1,10);c=new CombatSystem();
        c.enemies.add(new CombatSystem.Enemy(5,5.5f,.4f,100));
        check(!c.tryPull(p,1,0),"No atrae atravesando pared");
        check(!p.hasAttackAirJump(),"Gancho bloqueado no restaura salto");
        p=body(true);check(!c.tryPull(p,1,0),"Atraccion solo aerea");
        c.reset();check(c.attack==CombatSystem.Attack.NONE&&!c.isPulling()&&c.enemies.get(0).health==100,"Reset completo");
        for(int fps:new int[]{30,60,144,240}) {
            p=body(true); c=new CombatSystem();
            e=new CombatSystem.Enemy(10,10,.5f,100);c.enemies.add(e);
            e.receiveHit(10,.75f);
            check(!e.canAct()&&!e.tryMove(2,3),"Enemigo no puede actuar ni moverse durante aturdimiento");
            check(e.x==10&&e.y==10,"Conserva posicion");
            for(int i=0;i<(int)(fps*.7f);i++)c.update(1f/fps,p,false,false,false,false);
            check(e.isStunned(),"Sigue inmovil antes de 0.75 a "+fps);
            for(int i=0;i<(int)(fps*.1f)+1;i++)c.update(1f/fps,p,false,false,false,false);
            check(e.canAct()&&e.tryMove(1,0),"Recupera movimiento tras 0.75 a "+fps);
            e.receiveHit(10,.75f);advance(c,p,.4f,false);e.receiveHit(10,.75f);
            check(e.health==70&&Math.abs(e.getStunRemaining()-.75f)<.0001f,"Reimpacto refresca sin sumar ni invulnerabilidad");
        }
        p=body(true);c=new CombatSystem();
        e=new CombatSystem.Enemy(1,.5f,.4f,100,true);c.enemies.add(e);
        c.update(.01f,p,true,true,false,false);advance(c,p,.1f,false);
        check(e.health==90&&!e.isStunned()&&e.canAct(),"Inmune recibe dano pero no aturdimiento");
        e.receiveHit(100,.75f);check(!e.alive()&&!e.canAct()&&!e.isStunned(),"Enemigo muerto no actua");
        c.reset();check(e.alive()&&!e.isStunned()&&e.stunImmune,"Reset conserva inmunidad");
        // Mantener J y atacar sin enemigo no permite ganar altura, ni siquiera a FPS altos.
        for(int fps:new int[]{30,60,144,240,360}) {
            p=body(true);c=new CombatSystem();
            for(int frame=0;frame<fps/2;frame++) {
                int steps=(int)Math.ceil(240f/fps);float dt=1f/fps/steps;
                for(int step=0;step<steps;step++) {
                    boolean first=frame==0&&step==0;
                    p.update(dt,1,false,first,true,true);c.update(dt,p,first,frame<fps/4,false,false);
                    check(p.y<.001f&&p.getVelocityY()<=0,"Fallar no eleva a "+fps);
                }
            }
        }
        for(int fps:new int[]{30,60,144,240,360}) for(int side:new int[]{-1,1}) {
            p=body(true);c=new CombatSystem();float origin=p.x;
            boolean forward=false;
            for(int frame=0;frame<fps/4;frame++) {
                int steps=(int)Math.ceil(240f/fps);float dt=1f/fps/steps;
                for(int step=0;step<steps;step++) {
                    boolean first=frame==0&&step==0;
                    p.update(dt,side,false,first,true,true);
                    c.update(dt,p,first,true,false,false);
                    if((p.x-origin)*side>2) forward=true;
                    check(p.getVelocityY()<=0,"Estocada sin impacto no eleva");
                }
            }
            check(forward&&p.getVelocityX()*side>0,"Carrera en ambas direcciones a "+fps);
            p=body(true);c=new CombatSystem();origin=p.x;
            e=new CombatSystem.Enemy(origin+.25f+side*3,.5f,.4f,100);c.enemies.add(e);
            boolean launched=false;
            for(int frame=0;frame<fps/4;frame++) {
                int steps=(int)Math.ceil(240f/fps);float dt=1f/fps/steps;
                for(int step=0;step<steps;step++) {
                    boolean first=frame==0&&step==0;float oldHealth=e.health;
                    p.update(dt,side,false,first,true,true);c.update(dt,p,first,true,false,false);
                    if(e.health<oldHealth) {
                        launched=true;
                        check((p.x-origin)*side>0.5f,"Antes del impacto hubo carrera real");
                        check(Math.abs(p.getVelocityY()-18.7f)<.001f&&p.getVelocityX()==0,"Impacto convierte carrera en ascenso");
                    }
                }
            }
            check(launched,"Impacto en ambas direcciones a "+fps);
        }
        for(int fps:new int[]{30,60,144,240,360}) {
            p=body(false); c=new CombatSystem();
            e=new CombatSystem.Enemy(p.x+.25f,p.y-.5f,.4f,100);c.enemies.add(e);
            for(int frame=0;frame<fps/5;frame++)c.update(1f/fps,p,frame==0,true,false,true);
            check(p.hasAttackAirJump()&&Math.abs(p.getVelocityY()-17)<.001f,"Rebote normal restaura salto a "+fps);
            p.update(1f/60,0,true,false,false,true);
            check(!p.hasAttackAirJump()&&p.getVelocityY()>20,"H consume la carga y suma salto al rebote");
            c.enemies.add(new CombatSystem.Enemy(p.x+.25f,p.y-.5f,.4f,100));
            c.update(1f/240,p,false,true,false,true);
            check(!p.hasAttackAirJump(),"Varios enemigos en el mismo barrido no restauran varias veces");
            advance(c,p,.5f,false);
            e.x=p.x+.25f;e.y=p.y-.5f;
            c.update(.01f,p,true,true,false,true);advance(c,p,.1f,true);
            check(p.hasAttackAirJump(),"Otro rebote valido restaura la carga consumida");
            c.reset();c.enemies.clear();p.reset(0,5);
            c.update(.01f,p,true,true,false,true);advance(c,p,.1f,true);
            check(!p.hasAttackAirJump(),"Fallar el barrido no restaura salto");
        }
        System.out.println("PASS: combate, aura, rebote, estocada, tap/hold, momentum, atraccion y paredes");
    }
}
