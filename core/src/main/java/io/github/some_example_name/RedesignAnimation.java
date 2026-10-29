package io.github.some_example_name;

/** Solo estado visual: no modifica movimiento, controles, colisiones ni combate. */
public final class RedesignAnimation {
    private String state="neutral";
    private float time,idle,lastHealth=10,lastShot,fireRemaining,hurtRemaining,releaseRemaining,standRemaining,lastVy;
    private int lastSerial;
    private boolean wasHooked,impact;
    private float facing=1;
    public String getState(){return state;}
    public float getTime(){return time;}
    public float getFacing(){return facing;}
    public boolean hasImpact(){return impact;}
    public void reset(float health){state="neutral";time=idle=lastShot=fireRemaining=hurtRemaining=releaseRemaining=standRemaining=lastVy=0;lastHealth=health;lastSerial=0;wasHooked=impact=false;facing=1;}
    public void update(float dt,PlayerBody p,CombatSystem c,PlayerVitals life,float shotTime){
        dt=Math.max(0,Math.min(.25f,dt));
        fireRemaining=Math.max(0,fireRemaining-dt);hurtRemaining=Math.max(0,hurtRemaining-dt);
        releaseRemaining=Math.max(0,releaseRemaining-dt);standRemaining=Math.max(0,standRemaining-dt);
        if(life.getHealth()<lastHealth)hurtRemaining=.24f;
        if(shotTime>lastShot+.01f)fireRemaining=.16f;
        if(wasHooked&&!p.isHooked()&&!p.isPropelling()&&c.attack==CombatSystem.Attack.NONE)releaseRemaining=.22f;
        boolean active=c.attack!=CombatSystem.Attack.NONE;
        boolean moving=Math.abs(p.getVelocityX())>.25f;
        boolean quiet=p.isGrounded()&&!moving&&!active&&!p.isHooked()&&!p.isPropelling()&&!p.isDashing()&&fireRemaining<=0;
        idle=quiet?idle+dt:0;
        if(state.equals("rest")&&time>1.9f&&moving&&p.isGrounded()&&!active&&!p.isDashing())standRemaining=.25f;
        String next;
        if(!life.alive())next="death";
        else if(hurtRemaining>0)next="hurt";
        else if(active){switch(c.attack){
            case UP:next="attack_up";break;case DOWN:next="attack_down";break;
            case THRUST:next="thrust";break;case AURA:next="aura";break;
            case WHIRLWIND:next="whirlwind";break;case HOOK_STRIKE:next="hook_strike";break;
            default:next="attack_side";
        }}else if(p.isPropelling())next="propulsion";
        else if(p.isHooked())next="pendulum";
        else if(p.isDashing())next="dash";
        else if(fireRemaining>0)next="hook_fire";
        else if(releaseRemaining>0&&!p.isGrounded())next="release";
        else if(!p.isGrounded())next=p.getVelocityY()>.2f?"jump":"fall";
        else if(standRemaining>0)next="stand_up";
        else if(moving)next=p.isBoosted()||Math.abs(p.getVelocityX())>9?"run":"walk";
        else next=idle>=3?"rest":"neutral";
        boolean freshAttack=active&&c.getAttackSerial()!=lastSerial;
        if(!next.equals(state)||freshAttack||(next.equals("jump")&&p.getVelocityY()>lastVy+3))time=0;else time+=dt;
        if(active&&c.hasResolvedImpact()&&!impact)time=Math.max(time,.16f);
        state=next;impact=active&&c.hasResolvedImpact();
        facing=active?c.attackFacing:p.getFacing();
        lastHealth=life.getHealth();lastShot=shotTime;lastSerial=c.getAttackSerial();wasHooked=p.isHooked();lastVy=p.getVelocityY();
    }
}
