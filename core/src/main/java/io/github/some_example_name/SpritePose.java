package io.github.some_example_name;

/** Coordenadas de imagen (Y abajo) a mundo (Y arriba), verificable sin ventana. */
public final class SpritePose {
    public float x,y,originX,originY,ppu=96,facing=1,rotation;
    /** El apoyo visual se conserva al pasar entre suelo y aire, sin mover el collider. */
    public void placeAtFeet(RedesignClips.Frame frame,float feetX,float feetY,float pixelsPerUnit,float face,boolean airborne){
        ppu=pixelsPerUnit;facing=face;rotation=0;
        originX=airborne?frame.hx:frame.px;originY=airborne?frame.hy:frame.floorY;
        x=feetX+(originX-frame.px)*facing/ppu;
        y=feetY+(frame.floorY-originY)/ppu;
    }
    public float pointX(float ix,float iy){float a=(ix-originX)*facing/ppu,b=(originY-iy)/ppu,r=(float)Math.toRadians(rotation);return x+a*(float)Math.cos(r)-b*(float)Math.sin(r);}
    public float pointY(float ix,float iy){float a=(ix-originX)*facing/ppu,b=(originY-iy)/ppu,r=(float)Math.toRadians(rotation);return y+a*(float)Math.sin(r)+b*(float)Math.cos(r);}
    public void radial(RedesignClips.Frame f,float ax,float ay,float vx,float vy,boolean stableBody){
        float rx=x-ax,ry=y-ay;
        float tangent=-ry*vx+rx*vy;if(Math.abs(tangent)>.1f)facing=Math.signum(tangent);
        float bx=stableBody?0:(f.hx-f.handX)*facing,by=stableBody?-1:f.handY-f.hy;
        rotation=(float)Math.toDegrees(Math.atan2(ry,rx)-Math.atan2(by,bx));
    }
    public void aim(RedesignClips.Frame f,float dx,float dy){
        if(Math.abs(dx)>.01f)facing=Math.signum(dx);
        float bx=(f.handX-f.hx)*facing,by=f.hy-f.handY;
        rotation=(float)Math.toDegrees(Math.atan2(dy,dx)-Math.atan2(by,bx));
    }
}
