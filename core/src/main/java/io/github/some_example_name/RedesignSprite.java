package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.*;
import java.util.*;

/** Sprites aprobados y nuevos, con escala física independiente del margen del lienzo. */
public final class RedesignSprite implements Disposable {
    private final RedesignClips clips=new RedesignClips(Gdx.files.internal(RedesignClips.CONFIG));
    private final Map<String,Texture> textures=new LinkedHashMap<>();
    private final SpritePose pose=new SpritePose();
    private RedesignClips.Clip clip;private RedesignClips.Frame frame;
    private float handX,handY,armAngle;private int frameIndex;private boolean armAim,clipAtFloor;
    private float tangentFacing=1;
    public RedesignSprite(){
        try{for(RedesignClips.Clip c:clips.all())for(RedesignClips.Frame f:c.frames)if(!textures.containsKey(f.file)){
            Texture t=new Texture(Gdx.files.internal(f.file));t.setFilter(Texture.TextureFilter.Nearest,Texture.TextureFilter.Nearest);textures.put(f.file,t);
        }}catch(RuntimeException e){dispose();throw e;}
    }
    public RedesignClips getClips(){return clips;}
    public float getHandX(){return handX;}public float getHandY(){return handY;}
    public void prepare(RedesignAnimation animation,PlayerBody p,float aimX,float aimY){
        clip=clips.get(animation.getState());
        float time=animation.getTime();
        if(clip.id.equals("walk"))time*=MathUtils.clamp(Math.abs(p.getVelocityX())/7,.5f,1.6f);
        if(clip.id.equals("run"))time*=MathUtils.clamp(Math.abs(p.getVelocityX())/16.1f,.65f,1.6f);
        frameIndex=clip.index(time,animation.hasImpact());frame=clip.frames[frameIndex];
        pose.placeAtFeet(frame,p.x+PlayerBody.WIDTH/2,p.y,clip.ppu/clips.getRenderScale(),animation.getFacing(),!p.isGrounded());
        if(p.isHooked()){
            pose.originX=frame.hx;pose.originY=frame.hy;pose.facing=tangentFacing;
            pose.radial(frame,p.getAnchorX(),p.getAnchorY(),p.getVelocityX(),p.getVelocityY(),!clip.id.equals("pendulum"));tangentFacing=pose.facing;
        }else if(p.isPropelling()){
            pose.originX=frame.hx;pose.originY=frame.hy;
            pose.aim(frame,p.getAnchorX()-pose.x,p.getAnchorY()-pose.y);
        }
        handX=pose.pointX(frame.handX,frame.handY);handY=pose.pointY(frame.handX,frame.handY);
        clipAtFloor=p.isGrounded()&&Math.abs(pose.rotation)<.01f;
        armAim=clip.id.equals("hook_fire")&&p.isGrounded()&&clip.arm!=null&&frameIndex==clip.arm.getInt("frame");
        if(armAim){
            if(Math.abs(aimX)>.01f)pose.facing=Math.signum(aimX);
            float sx=clip.arm.getFloat("shoulderX"),sy=clip.arm.getFloat("shoulderY");
            float wx=pose.pointX(sx,sy),wy=pose.pointY(sx,sy);
            armAngle=MathUtils.atan2(aimY,aimX)*MathUtils.radiansToDegrees-(pose.facing<0?180:0);
            float dx=(frame.handX-sx)*pose.facing/pose.ppu,dy=(sy-frame.handY)/pose.ppu;
            handX=wx+dx*MathUtils.cosDeg(armAngle)-dy*MathUtils.sinDeg(armAngle);
            handY=wy+dx*MathUtils.sinDeg(armAngle)+dy*MathUtils.cosDeg(armAngle);
        }
    }
    public void draw(Batch batch){drawPrepared(batch);}
    private void drawPrepared(Batch batch){
        Texture t=textures.get(frame.file);
        if(!armAim){drawPiece(batch,t,0,0,t.getWidth(),t.getHeight(),pose);return;}
        JsonValue a=clip.arm;int x=a.getInt("x"),y=a.getInt("y"),w=a.getInt("width"),h=a.getInt("height");
        drawPiece(batch,t,0,0,t.getWidth(),y,pose);drawPiece(batch,t,0,y+h,t.getWidth(),t.getHeight()-y-h,pose);
        drawPiece(batch,t,0,y,x,h,pose);drawPiece(batch,t,x+w,y,t.getWidth()-x-w,h,pose);
        SpritePose arm=new SpritePose();arm.ppu=pose.ppu;arm.facing=pose.facing;arm.rotation=armAngle;
        arm.originX=a.getFloat("shoulderX");arm.originY=a.getFloat("shoulderY");
        arm.x=pose.pointX(arm.originX,arm.originY);arm.y=pose.pointY(arm.originX,arm.originY);
        drawPiece(batch,t,x,y,w,h,arm);
    }
    private void drawPiece(Batch batch,Texture texture,int sx,int sy,int w,int h,SpritePose p){
        if(clipAtFloor&&!armAim)h=Math.min(h,(int)Math.ceil(frame.floorY)-sy);
        if(w<=0||h<=0)return;
        float ox=(p.originX-sx)/p.ppu,oy=(sy+h-p.originY)/p.ppu;
        batch.draw(new TextureRegion(texture,sx,sy,w,h),p.x-ox,p.y-oy,ox,oy,w/p.ppu,h/p.ppu,p.facing,1,p.rotation);
    }
    /** Galería GL: usa las mismas texturas y transformación que el nivel. */
    public void drawSample(Batch batch,String id,float time,boolean hit,float x,float y,float pixelScale,float facing,float rotation){
        clip=clips.get(id);frameIndex=clip.index(time,hit);frame=clip.frames[frameIndex];armAim=false;clipAtFloor=false;
        pose.x=x;pose.y=y;pose.originX=frame.px;pose.originY=frame.py;pose.ppu=1/pixelScale;pose.facing=facing;pose.rotation=rotation;
        handX=pose.pointX(frame.handX,frame.handY);handY=pose.pointY(frame.handX,frame.handY);drawPrepared(batch);
    }
    @Override public void dispose(){for(Texture t:textures.values())t.dispose();textures.clear();}
}
