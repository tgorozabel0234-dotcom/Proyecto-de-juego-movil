package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Disposable;

/** Solo presentación: los rectángulos y posiciones proceden del nivel. */
public final class ReadableLevelScene implements Disposable {
    private final JsonValue config;
    private final Texture city,metal,door,anchor;
    public ReadableLevelScene(){
        config=new JsonReader().parse(Gdx.files.internal("environments/cyberpunk_v1/render.json"));
        city=texture("city");metal=texture("metal");door=texture("door");anchor=texture("anchor");
    }
    private Texture texture(String key){Texture t=new Texture(Gdx.files.internal(config.getString(key)));t.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);return t;}
    private static void rect(SpriteBatch b,Texture p,float x,float y,float w,float h,String c){b.setColor(Color.valueOf(c));b.draw(p,x,y,w,h);}
    private static void line(SpriteBatch b,Texture p,float x,float y,float xx,float yy,float t,String c){b.setColor(Color.valueOf(c));b.draw(p,x,y,0,t/2,(float)Math.hypot(xx-x,yy-y),t,1,1,(float)Math.toDegrees(Math.atan2(yy-y,xx-x)),0,0,1,1,false,false);}
    private boolean visible(float x,float y,float w,float h,OrthographicCamera c){float hw=c.viewportWidth*c.zoom/2,hh=c.viewportHeight*c.zoom/2;return x+w>=c.position.x-hw-2&&x<=c.position.x+hw+2&&y+h>=c.position.y-hh-2&&y<=c.position.y+hh+2;}
    private void background(SpriteBatch b,Texture p,OrthographicCamera c){
        float tw=config.getFloat("backgroundTileWidth"),th=tw*city.getHeight()/city.getWidth();
        float follow=1-config.getFloat("backgroundParallax"),ox=c.position.x*follow,oy=c.position.y*follow;
        float hw=c.viewportWidth*c.zoom/2,hh=c.viewportHeight*c.zoom/2;
        int left=(int)Math.floor((c.position.x-hw-ox)/tw),right=(int)Math.ceil((c.position.x+hw-ox)/tw);
        int bottom=(int)Math.floor((c.position.y-hh-oy)/th),top=(int)Math.ceil((c.position.y+hh-oy)/th);
        float brightness=config.getFloat("backgroundBrightness");b.setColor(brightness,brightness,brightness,1);
        for(int x=left;x<right;x++)for(int y=bottom;y<top;y++) b.draw(city,ox+x*tw,oy+y*th,tw,th,0,0,city.getWidth(),city.getHeight(),(x&1)!=0,(y&1)!=0);
        b.setColor(Color.WHITE);
    }
    private void panel(SpriteBatch b,float x,float y,float w,float h,boolean wall){
        float tw=wall?2:config.getFloat("metalTileWidth"),th=config.getFloat("metalTileHeight");
        b.setColor(wall?.68f:1,wall?.74f:1,wall?.85f:1,1);
        // Cada módulo se recorta dentro del sólido: nunca se extiende sobre un hueco.
        for(float xx=0;xx<w;xx+=tw)for(float yy=0;yy<h;yy+=th){
            float dw=Math.min(tw,w-xx),dh=Math.min(th,h-yy);
            b.draw(metal,x+xx,y+h-yy-dh,dw,dh,0,0,Math.max(1,(int)(metal.getWidth()*dw/tw)),Math.max(1,(int)(metal.getHeight()*dh/th)),false,false);
        }
        b.setColor(Color.WHITE);
    }
    public void draw(SpriteBatch b,Texture p,JsonValue map,OrthographicCamera camera,float time){
        background(b,p,camera);
        float height=map.getFloat("height");
        // Soportes en sombra y cables decorativos bajo las superficies.
        for(JsonValue q=map.get("pieces").child;q!=null;q=q.next){
            if(!q.getString("role", "").equals("platform"))continue;
            float x=q.getFloat("x"),y=q.getFloat("y"),w=q.getFloat("width");
            if(!visible(x,y-2,w,3,camera))continue;
            for(float sx=x+1.2f;sx<x+w-1;sx+=7){
                line(b,p,sx,y,sx+.65f,y-1.2f,.14f,"152331");line(b,p,sx+.65f,y-1.2f,sx+1.6f,y,.12f,"263947");
                for(int i=0;i<12;i++){float a=i/12f,c=(i+1)/12f;line(b,p,sx+a*2.6f,y-.7f*(float)Math.sin(a*Math.PI),sx+c*2.6f,y-.7f*(float)Math.sin(c*Math.PI),.035f,"182934");}
            }
        }
        for(JsonValue q=map.get("pieces").child;q!=null;q=q.next){
            float x=q.getFloat("x"),y=q.getFloat("y"),w=q.getFloat("width"),h=q.getFloat("height");
            String role=q.getString("role", "platform");
            if(role.equals("anchor")){
                float cx=x+w/2,cy=y+h/2;
                if(!visible(cx-1,cy-1,2,height-cy+2,camera))continue;
                line(b,p,cx,cy+.8f,cx,height,.065f,"253643");line(b,p,cx+.045f,cy+.8f,cx+.045f,height,.022f,"506271");
                float aw=config.getFloat("anchorWidth");b.setColor(Color.WHITE);
                b.draw(anchor,cx-aw/2,cy-aw*(1-config.getFloat("anchorPivotY")),aw,aw);
            }else{
                if(!visible(x,y,w,h,camera))continue;
                panel(b,x,y,w,h,role.equals("wall"));
                rect(b,p,x,y+h-.065f,w,.065f,"BAEAF4");
                if(!role.equals("wall")){
                    // Reflejos de neón contenidos en el suelo, sin nuevas superficies visuales.
                    for(float lx=x+1;lx<x+w-1;lx+=5){rect(b,p,lx,y+h-.19f,Math.min(1.6f,x+w-lx),.055f,"35CFE6");}
                    rect(b,p,x,y+h-.35f,.12f,.28f,"FFC586");rect(b,p,x+w-.12f,y+h-.35f,.12f,.28f,"FFC586");
                }
            }
        }
        JsonValue e=map.get("exit");float dw=config.getFloat("doorWidth"),dh=config.getFloat("doorHeight");
        float dx=e.getFloat("x")+e.getFloat("width")/2-dw/2,dy=e.getFloat("y")-dh*(1-config.getFloat("doorFootY"));
        b.setColor(Color.WHITE);b.draw(door,dx,dy,dw,dh);
        // Marcas discretas de peligro por debajo de las superficies jugables.
        for(float x=0;x<map.getFloat("width");x+=2)if(visible(x,-2,1,1,camera))line(b,p,x,-2,x+.7f,-1.5f,.07f,"713149");
        b.setColor(Color.WHITE);
    }
    @Override public void dispose(){city.dispose();metal.dispose();door.dispose();anchor.dispose();}
}
