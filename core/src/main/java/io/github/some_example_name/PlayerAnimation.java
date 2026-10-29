package io.github.some_example_name;

/** Selector visual independiente: nunca modifica velocidad, colisiones, vida ni acciones. */
public final class PlayerAnimation {
    public enum State { IDLE, WALK, RUN, JUMP, FALL, LAND, DASH, HOOK_FIRE, PROPULSION,
        PENDULUM, RELEASE, ATTACK_SIDE, ATTACK_UP, ATTACK_DOWN, THRUST, AURA, WHIRLWIND, HURT, DEATH }
    private State state=State.IDLE;
    private float time,lastHealth=10,lastShot,lastAttack,fireRemaining,hurtRemaining,landRemaining,releaseRemaining,lastVy;
    private boolean wasGrounded=true,wasHooked,hadExtraJump;
    private float attackProgress;
    private float facing=1;
    public State getState(){return state;}
    public float getTime(){return time;}
    public int getSheet(){return sheet(state);}
    public int getFrame(){return frame(state,time,attackProgress);}
    public float getFacing(){return facing;}
    public void reset(float health){
        state=State.IDLE;time=lastShot=lastAttack=fireRemaining=hurtRemaining=landRemaining=releaseRemaining=lastVy=0;
        lastHealth=health;wasGrounded=true;wasHooked=hadExtraJump=false;attackProgress=0;facing=1;
    }
    public void update(float delta,PlayerBody p,CombatSystem combat,PlayerVitals life,float shotTime){
        float dt=Math.max(0,Math.min(delta,.25f));
        fireRemaining=Math.max(0,fireRemaining-dt);hurtRemaining=Math.max(0,hurtRemaining-dt);
        landRemaining=Math.max(0,landRemaining-dt);releaseRemaining=Math.max(0,releaseRemaining-dt);
        if(life.getHealth()<lastHealth)hurtRemaining=.18f;
        if(shotTime>lastShot+.01f)fireRemaining=.16f;
        if(!wasGrounded&&p.isGrounded()&&(lastVy<-.5f||state==State.FALL))landRemaining=.1f;
        if(wasHooked&&!p.isHooked()&&!p.isPropelling()&&combat.attack==CombatSystem.Attack.NONE)releaseRemaining=.12f;
        State next;
        if(!life.alive())next=State.DEATH;
        else if(hurtRemaining>0)next=State.HURT;
        else if(combat.attack!=CombatSystem.Attack.NONE){
            switch(combat.attack){
                case UP:next=State.ATTACK_UP;break;
                case DOWN:next=State.ATTACK_DOWN;break;
                case THRUST:next=State.THRUST;break;
                case HOOK_STRIKE:next=State.ATTACK_SIDE;break;
                case WHIRLWIND:next=State.WHIRLWIND;break;
                case AURA:next=State.AURA;break;
                default:next=State.ATTACK_SIDE;
            }
        }else if(fireRemaining>0)next=State.HOOK_FIRE;
        else if(p.isPropelling())next=State.PROPULSION;
        else if(p.isHooked())next=State.PENDULUM;
        else if(p.isDashing())next=State.DASH;
        else if(releaseRemaining>0)next=State.RELEASE;
        else if(!p.isGrounded())next=p.getVelocityY()>.5f?State.JUMP:State.FALL;
        else if(landRemaining>0)next=State.LAND;
        else if(Math.abs(p.getVelocityX())>.3f)next=p.isBoosted()||Math.abs(p.getVelocityX())>9?State.RUN:State.WALK;
        else next=State.IDLE;
        boolean extraJump=hadExtraJump&&!p.hasAttackAirJump()&&p.getVelocityY()>lastVy+3;
        boolean repeatedAttack=combat.attack!=CombatSystem.Attack.NONE&&combat.elapsed<lastAttack;
        if(next!=state||extraJump||repeatedAttack)time=0;else time+=dt;
        state=next;attackProgress=combat.progress();lastAttack=combat.elapsed;
        facing=combat.attack!=CombatSystem.Attack.NONE?combat.attackFacing
            :p.isDashing()&&Math.abs(p.getVelocityX())>.1f?Math.signum(p.getVelocityX()):p.getFacing();
        lastHealth=life.getHealth();lastShot=shotTime;wasGrounded=p.isGrounded();wasHooked=p.isHooked();
        hadExtraJump=p.hasAttackAirJump();lastVy=p.getVelocityY();
    }
    public static int sheet(State state){
        switch(state){
            case ATTACK_SIDE:case ATTACK_UP:case ATTACK_DOWN:case THRUST:case WHIRLWIND:return 1;
            case HOOK_FIRE:case PROPULSION:case PENDULUM:case RELEASE:case AURA:case HURT:case DEATH:return 2;
            default:return 0;
        }
    }
    public static int frame(State state,float time,float progress){
        time=Math.max(0,time);progress=Math.max(0,Math.min(.99999f,progress));
        switch(state){
            case WALK:return 4+(int)(time*9)%4;
            case RUN:return 8+(int)(time*14)%4;
            case JUMP:return time<.06f?12:time<.22f?13:14;
            case FALL:return 15;
            case LAND:return 12;
            case DASH:return 8+(int)(time*22)%2;
            case HOOK_FIRE:return Math.min(3,(int)(time/.04f));
            case PROPULSION:case RELEASE:return 7;
            case PENDULUM:int k=(int)(time*7)%4;return 4+(k==3?1:k);
            case ATTACK_SIDE:return (int)(progress*4);
            case ATTACK_UP:return 4+(int)(progress*4);
            case ATTACK_DOWN:return 8+(int)(progress*4);
            case THRUST:return 12+(int)(progress*4);
            case AURA:return 12+(int)(progress*4);
            case WHIRLWIND:return (int)(time*18)%4;
            case HURT:return time<.09f?8:9;
            case DEATH:return time<.28f?10:11;
            default:return (int)(time*4)%4;
        }
    }
}
