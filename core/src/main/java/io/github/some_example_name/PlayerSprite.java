package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Hojas RGBA y regiones explicitas: no supone que la imagen generada tenga celdas exactas. */
public final class PlayerSprite implements Disposable {
    public static final String ROOT="players/cyber_runner_v1/";
    private static final class Frame {
        Piece[] pieces;float width,height,pivotX;
    }
    private static final class Piece {TextureRegion right,left;float x,y,width,height;}
    private final Texture[] textures;
    private final float renderScale;
    private final Frame[][] frames;
    private final Color color=new Color();
    public PlayerSprite(){
        JsonValue data=new JsonReader().parse(Gdx.files.internal(ROOT+"animations.json"));
        renderScale=data.getFloat("renderScale",1.5f);
        JsonValue sheets=data.get("sheets");textures=new Texture[sheets.size];frames=new Frame[sheets.size][];
        int s=0;
        for(JsonValue sheet=sheets.child;sheet!=null;sheet=sheet.next,s++){
            Texture texture=new Texture(Gdx.files.internal(ROOT+sheet.getString("file")));
            texture.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);textures[s]=texture;
            JsonValue regions=sheet.get("frames");frames[s]=new Frame[regions.size];int i=0;
            for(JsonValue region=regions.child;region!=null;region=region.next,i++){
                int x=region.getInt("x"),y=region.getInt("y"),w=region.getInt("width"),h=region.getInt("height");
                if(x<0||y<0||x+w>texture.getWidth()||y+h>texture.getHeight())throw new IllegalArgumentException("Region fuera de hoja "+s+":"+i);
                float ppu=region.getFloat("pixelsPerUnit");Frame frame=new Frame();
                frame.pieces=pieces(texture,x,y,w,h,ppu,region.get("exclude"));
                frame.width=w/ppu;frame.height=h/ppu;frame.pivotX=region.getFloat("pivotX")/ppu;frames[s][i]=frame;
            }
        }
    }
    private Piece[] pieces(Texture texture,int x,int y,int width,int height,float ppu,JsonValue exclusions){
        java.util.List<int[]> boxes=new java.util.ArrayList<>();boxes.add(new int[]{0,0,width,height});
        for(JsonValue e=exclusions==null?null:exclusions.child;e!=null;e=e.next){
            java.util.List<int[]> next=new java.util.ArrayList<>();
            for(int[] b:boxes){
                int ix=Math.max(b[0],e.getInt("x")),iy=Math.max(b[1],e.getInt("y"));
                int ex=Math.min(b[0]+b[2],e.getInt("x")+e.getInt("width")),ey=Math.min(b[1]+b[3],e.getInt("y")+e.getInt("height"));
                if(ix>=ex||iy>=ey){next.add(b);continue;}
                box(next,b[0],b[1],b[2],iy-b[1]);box(next,b[0],ey,b[2],b[1]+b[3]-ey);
                box(next,b[0],iy,ix-b[0],ey-iy);box(next,ex,iy,b[0]+b[2]-ex,ey-iy);
            }
            boxes=next;
        }
        Piece[] result=new Piece[boxes.size()];int i=0;
        for(int[] b:boxes){
            Piece part=new Piece();part.right=new TextureRegion(texture,x+b[0],y+b[1],b[2],b[3]);part.left=new TextureRegion(part.right);part.left.flip(true,false);
            part.x=b[0]/ppu;part.y=(height-b[1]-b[3])/ppu;part.width=b[2]/ppu;part.height=b[3]/ppu;result[i++]=part;
        }
        return result;
    }
    private void box(java.util.List<int[]> boxes,int x,int y,int w,int h){if(w>0&&h>0)boxes.add(new int[]{x,y,w,h});}
    public void draw(Batch batch,PlayerAnimation animation,PlayerBody p){
        color.set(batch.getColor());
        PlayerAnimation.State state=animation.getState();
        boolean grounded=p.isGrounded()||state==PlayerAnimation.State.LAND;
        float facing=animation.getFacing();
        float rotation=state==PlayerAnimation.State.WHIRLWIND?-facing*animation.getTime()*900:0;
        if(state==PlayerAnimation.State.DASH){
            for(int i=3;i>=1;i--){
                batch.setColor(.15f,.9f,1,color.a*.12f*(4-i));
                drawFrame(batch,animation.getSheet(),animation.getFrame(),p.x+PlayerBody.WIDTH/2-facing*.16f*i*renderScale,p.y,renderScale,facing,grounded,rotation);
            }
        }
        batch.setColor(color);
        drawFrame(batch,animation.getSheet(),animation.getFrame(),p.x+PlayerBody.WIDTH/2,p.y,renderScale,facing,grounded,rotation);
        batch.setColor(color);
    }
    /** Vista de poses para revisar assets; no toca el selector ni el estado del jugador. */
    public void drawSample(Batch b,PlayerAnimation.State state,float phase,float x,float y,float scale,float facing){
        float time=phase*.6f;
        drawFrame(b,PlayerAnimation.sheet(state),PlayerAnimation.frame(state,time,phase),x,y,scale,facing,state!=PlayerAnimation.State.WHIRLWIND,
            state==PlayerAnimation.State.WHIRLWIND?-facing*phase*360:0);
    }
    private void drawFrame(Batch b,int sheet,int index,float x,float y,float scale,float facing,boolean grounded,float rotation){
        Frame f=frames[sheet][index];float w=f.width*scale,h=f.height*scale;
        float ox=(facing<0?f.width-f.pivotX:f.pivotX)*scale,oy=grounded?0:h/2;
        float baseY=grounded?y:y+PlayerBody.HEIGHT/2*scale;
        for(Piece part:f.pieces){
            float dx=facing<0?w-(part.x+part.width)*scale:part.x*scale,dy=part.y*scale;
            b.draw(facing<0?part.left:part.right,x-ox+dx,baseY-oy+dy,ox-dx,oy-dy,part.width*scale,part.height*scale,1,1,rotation);
        }
    }
    @Override public void dispose(){for(Texture t:textures)if(t!=null)t.dispose();}
}
