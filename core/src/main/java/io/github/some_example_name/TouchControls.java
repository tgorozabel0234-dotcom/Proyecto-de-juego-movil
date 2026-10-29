package io.github.some_example_name;

import java.util.Arrays;

/** Estado multitactil sin dependencias graficas. Los eventos breves sobreviven hasta endFrame. */
public final class TouchControls {
    public enum Action { JUMP, DASH, ATTACK, PROPULSION, PENDULUM }
    private final int[] owners=new int[Action.values().length];
    private final boolean[] pressed=new boolean[Action.values().length];
    private int stickOwner=-1;
    private float x,y,shotX,shotY;
    private boolean sprint, swing, seenSwing, shotAim;
    public float deadzone=.18f;
    public TouchControls() { reset(); }
    public boolean press(Action action,int pointer) {
        if(pointer<0||owns(pointer)||owners[action.ordinal()]>=0)return false;
        owners[action.ordinal()]=pointer;
        if(action==Action.PENDULUM) {
            swing=!swing;seenSwing=false;
            pressed[action.ordinal()]=swing;
        } else pressed[action.ordinal()]=true;
        if(action==Action.DASH)sprint=Math.abs(moveX())>.01f;
        if(action==Action.PROPULSION||action==Action.PENDULUM) {
            shotX=x;shotY=y;shotAim=true;
        }
        return true;
    }
    public void release(int pointer) {
        if(pointer==stickOwner) {stickOwner=-1;x=y=0;sprint=false;}
        for(int i=0;i<owners.length;i++)if(owners[i]==pointer)owners[i]=-1;
    }
    private boolean owns(int pointer) {
        if(stickOwner==pointer)return true;
        for(int owner:owners)if(owner==pointer)return true;
        return false;
    }
    public boolean beginStick(int pointer,float dx,float dy) {
        if(pointer<0||stickOwner>=0||owns(pointer))return false;
        stickOwner=pointer;dragStick(pointer,dx,dy);return true;
    }
    public void dragStick(int pointer,float dx,float dy) {
        if(pointer!=stickOwner)return;
        float norm=(float)Math.hypot(dx,dy);float scale=Math.max(1,norm);
        x=dx/scale;y=dy/scale;
        if(norm<deadzone) {x=y=0;sprint=false;}
        if(aimX()==0)sprint=false;
    }
    private int quantize(boolean horizontal,float vx,float vy) {
        if(Math.hypot(vx,vy)<deadzone)return 0;
        int sector=(int)Math.round(Math.atan2(vy,vx)/(Math.PI/4));
        return (int)Math.round(horizontal?Math.cos(sector*Math.PI/4):Math.sin(sector*Math.PI/4));
    }
    public int aimX() { return quantize(true,shotAim&&x==0&&y==0?shotX:x,shotAim&&x==0&&y==0?shotY:y); }
    public int aimY() { return quantize(false,shotAim&&x==0&&y==0?shotX:x,shotAim&&x==0&&y==0?shotY:y); }
    public float moveX() { return quantize(true,x,y)==0?0:x; }
    public float stickX() {return x;}
    public float stickY() {return y;}
    public boolean pressed(Action action) {return pressed[action.ordinal()];}
    public boolean held(Action action) {
        if(action==Action.PENDULUM)return swing;
        if(action==Action.DASH)return owners[action.ordinal()]>=0||sprint;
        return owners[action.ordinal()]>=0;
    }
    public void syncSwing(boolean hooked,boolean firedPendulum) {
        if(hooked)seenSwing=true;
        if((seenSwing&&!hooked)||(firedPendulum&&!hooked)) {swing=false;seenSwing=false;}
    }
    public void endFrame() {Arrays.fill(pressed,false);shotAim=false;}
    public void reset() {
        Arrays.fill(owners,-1);Arrays.fill(pressed,false);stickOwner=-1;
        x=y=shotX=shotY=0;sprint=swing=seenSwing=shotAim=false;
    }
}
