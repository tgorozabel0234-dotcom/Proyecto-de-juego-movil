package io.github.some_example_name;

/** Geometria comprobable sin OpenGL; coordenadas de HUD, no pixeles del dispositivo. */
public final class HudLayout {
    public static final class Box {
        public final float x,y,w,h;
        public Box(float x,float y,float w,float h){this.x=x;this.y=y;this.w=w;this.h=h;}
        public boolean overlaps(Box b){return x<b.x+b.w&&x+w>b.x&&y<b.y+b.h&&y+h>b.y;}
    }
    public final Box stick;
    public final Box[] actions=new Box[TouchControls.Action.values().length];
    public HudLayout(float width,float height,float left,float right,float bottom,float margin,float stickSize) {
        float edge=width-right-margin,base=bottom+margin;
        stick=new Box(left+margin,base,stickSize,stickSize);
        actions[TouchControls.Action.JUMP.ordinal()]=circle(edge-120,base+38,31);
        actions[TouchControls.Action.DASH.ordinal()]=circle(edge-37,base+28,28);
        actions[TouchControls.Action.ATTACK.ordinal()]=circle(edge-36,base+105,36);
        boolean portrait=height>width;
        actions[TouchControls.Action.PROPULSION.ordinal()]=portrait?circle(edge-120,base+122,28):circle(edge-198,base+42,28);
        actions[TouchControls.Action.PENDULUM.ordinal()]=portrait?circle(edge-36,base+199,28):circle(edge-139,base+124,28);
    }
    private Box circle(float x,float y,float radius){return new Box(x-radius,y-radius,radius*2,radius*2);}
}
