package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Disposable;

/** Solo presentación: los rectángulos y posiciones proceden del nivel. */
public final class ReadableLevelScene implements Disposable {
    private final com.badlogic.gdx.graphics.g2d.BitmapFont lettering = new com.badlogic.gdx.graphics.g2d.BitmapFont();
    private final com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
    private final JsonValue config;
    private final IndustrialAtmosphere atmosphere = new IndustrialAtmosphere();
    private final Texture city,metal,door,anchor;
    private final ObjectMap<String, Texture> scenerySheets = new ObjectMap<>();
    private final Array<Landmark> landmarks = new Array<>();

    private static final class Landmark {
        final TextureRegion region;
        final float x, y, width, height;
        final String circuit, sprite;
        final boolean machinery, hot;
        float ceiling;
        final boolean poweredOnly;

        Landmark(JsonValue placement, TextureRegion region) {
            this.region = region;
            sprite = placement.getString("sprite");
            machinery = java.util.Arrays.asList("pump","pump_bank","vessel","gauge","filters","diverter",
                "chiller","rupture","drain","transformer","switchboard","breaker","busbars","piston","header","intake").contains(sprite);
            hot = sprite.equals("vessel") || sprite.equals("pump") || sprite.equals("transformer") || sprite.equals("breaker");
            x = placement.getFloat("x");
            y = placement.getFloat("y");
            width = placement.getFloat("width");
            height = width * region.getRegionHeight() / region.getRegionWidth();
            circuit = placement.getString("power", "");
            poweredOnly = "on".equals(placement.getString("only", ""));
        }
    }

    private void loadScenery(JsonValue scenery) {
        if (scenery == null) return;
        String root = "environments/act_1/";
        JsonValue regions = new JsonReader().parse(Gdx.files.internal(root + "regions.json"));
        for (JsonValue placement = scenery.child; placement != null; placement = placement.next) {
            JsonValue region = regions.require(placement.getString("sprite"));
            String sheet = region.getString("sheet");
            Texture texture = scenerySheets.get(sheet);
            if (texture == null) {
                texture = loadTexture(root + sheet + ".png");
                scenerySheets.put(sheet, texture);
            }
            landmarks.add(new Landmark(placement, new TextureRegion(texture, region.getInt("x"),
                region.getInt("y"), region.getInt("w"), region.getInt("h"))));
        }
    }

    private void drawLandmarks(SpriteBatch batch, Texture pixel, OrthographicCamera camera, float time, java.util.Set<String> flags) {
        for (Landmark prop : landmarks) {
            if (!visible(prop.x, prop.y, prop.width, prop.height, camera)) continue;
            boolean powered = prop.circuit.isEmpty() || flags.contains(prop.circuit);
            if (!powered && prop.poweredOnly) continue;
            if(prop.machinery) atmosphere.conduit(batch,pixel,prop.x,prop.y,prop.width,prop.height,prop.ceiling);
            if (powered) batch.setColor(.77f, .84f, .91f, 1);
            else batch.setColor(.37f, .42f, .50f, 1);
            batch.draw(prop.region, prop.x, prop.y, prop.width, prop.height);
            if (prop.machinery) atmosphere.fittings(batch,pixel,prop.x,prop.y,prop.width,prop.height,time,prop.hot);
            if (prop.sprite.equals("gauge")) atmosphere.fluid(batch,pixel,prop.x,prop.y,prop.width,prop.height,time);
        }
        batch.setColor(Color.WHITE);
    }
    public ReadableLevelScene(JsonValue map){
        config=new JsonReader().parse(Gdx.files.internal("environments/cyberpunk_v1/render.json"));
        lettering.setUseIntegerPositions(false);
        loadScenery(map.get("scenery"));
        for (Landmark prop : landmarks) {
            prop.ceiling = map.getFloat("height");
            float axis = prop.x + prop.width*1.08f;
            for (JsonValue q=map.get("pieces").child;q!=null;q=q.next) {
                float y=q.getFloat("y"), x=q.getFloat("x");
                if (axis>=x && axis<=x+q.getFloat("width") && y>=prop.y+prop.height*.72f+4)
                    prop.ceiling=Math.min(prop.ceiling,y);
            }
        }
        lettering.getData().setScale(.048f);
        city=loadTexture(map.getString("background", config.getString("city")));metal=texture("metal");door=texture("door");anchor=texture("anchor");
    }
    private Texture texture(String key){return loadTexture(config.getString(key));}
    private Texture loadTexture(String path){Texture t=new Texture(Gdx.files.internal(path));t.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);return t;}
    private static void rect(SpriteBatch b,Texture p,float x,float y,float w,float h,String c){b.setColor(Color.valueOf(c));b.draw(p,x,y,w,h);}
    private static void line(SpriteBatch b,Texture p,float x,float y,float xx,float yy,float t,String c){b.setColor(Color.valueOf(c));b.draw(p,x,y,0,t/2,(float)Math.hypot(xx-x,yy-y),t,1,1,(float)Math.toDegrees(Math.atan2(yy-y,xx-x)),0,0,1,1,false,false);}
    private boolean visible(float x,float y,float w,float h,OrthographicCamera c){float hw=c.viewportWidth*c.zoom/2,hh=c.viewportHeight*c.zoom/2;return x+w>=c.position.x-hw-2&&x<=c.position.x+hw+2&&y+h>=c.position.y-hh-2&&y<=c.position.y+hh+2;}
    private void background(SpriteBatch b,Texture p,OrthographicCamera c,float time){
        float tw=config.getFloat("backgroundTileWidth"),th=tw*city.getHeight()/city.getWidth();
        float follow=1-config.getFloat("backgroundParallax"),ox=c.position.x*follow,oy=c.position.y*follow;
        float hw=c.viewportWidth*c.zoom/2,hh=c.viewportHeight*c.zoom/2;
        int left=(int)Math.floor((c.position.x-hw-ox)/tw),right=(int)Math.ceil((c.position.x+hw-ox)/tw);
        int bottom=(int)Math.floor((c.position.y-hh-oy)/th),top=(int)Math.ceil((c.position.y+hh-oy)/th);
        float brightness=config.getFloat("backgroundBrightness");b.setColor(brightness,brightness,brightness,1);
        for(int x=left;x<right;x++)for(int y=bottom;y<top;y++) {
            b.setColor(brightness,brightness,brightness,1);
            b.draw(city,ox+x*tw,oy+y*th,tw,th,0,0,city.getWidth(),city.getHeight(),(x&1)!=0,(y&1)!=0);
            atmosphere.windows(b,p,ox+x*tw,oy+y*th,tw,th,time);
        }
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
    public void draw(SpriteBatch b,Texture p,JsonValue map,OrthographicCamera camera,float time,java.util.Set<String> flags,float playerX,float playerY){
        background(b,p,camera,time);
        atmosphere.air(b,p,camera,time,map.getFloat("height"));
        drawLandmarks(b,p,camera,time,flags);
        float height=map.getFloat("height");
        // Soportes en sombra y cables decorativos bajo las superficies.
        for(JsonValue q=map.get("pieces").child;q!=null;q=q.next){
            if(!q.getString("role", "").equals("platform"))continue;
            float x=q.getFloat("x"),y=q.getFloat("y"),w=q.getFloat("width");
            if(!visible(x,y-2,w,3,camera))continue;
            atmosphere.underDeck(b,p,x,y,w);
            if(q.getBoolean("suspended",false)) atmosphere.hangers(b,p,x,y,w,q.getFloat("height"));
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
                if(role.equals("pipe")) atmosphere.pipe(b,p,x,y,w,h);
                else panel(b,x,y,w,h,role.equals("wall"));
                rect(b,p,x,y+h-.065f,w,.065f,"BAEAF4");
                if(!role.equals("wall")){
                    atmosphere.deck(b,p,x,y+h,w,time);
                    rect(b,p,x,y+h-.35f,.12f,.28f,"FFC586");rect(b,p,x+w-.12f,y+h-.35f,.12f,.28f,"FFC586");
                }
            }
        }
        JsonValue vents=map.get("vents");
        if(vents!=null) for(JsonValue v=vents.child;v!=null;v=v.next)
            atmosphere.vent(b,p,v,time);
        JsonValue exit = map.get("exit");
        drawDoor(b, exit, exit == null || !exit.has("requires") || flags.contains(exit.getString("requires")));
        JsonValue portals = map.get("portals");
        if (portals != null) for (JsonValue e = portals.child; e != null; e = e.next)
            drawDoor(b, e, !e.has("requires") || flags.contains(e.getString("requires")));
        JsonValue signs = map.get("signs");
        if (signs != null) for (JsonValue sign = signs.child; sign != null; sign = sign.next) {
            float x = sign.getFloat("x"), y = sign.getFloat("y");
            if (!visible(x,y-1,16,2,camera)) continue;
            boolean lit = flags.contains(sign.getString("flag",""));
            String label = sign.getString(lit ? "lit" : "text");
            layout.setText(lettering,label);
            float width=layout.width+.7f;
            boolean near=Math.abs(playerX-x)<9 && Math.abs(playerY-y)<6;
            rect(b,p,x,y-.96f,width,1.27f,near?"08303EDB":"092A36A8");
            rect(b,p,x,y-.99f,width,.045f,"4AF6F0BC");
            rect(b,p,x,y+.29f,width*.3f,.055f,"80FFF0DF");
            rect(b,p,x+.13f,y-.9f,.03f,.98f,"69ECD6AD");
            float scan=(time*.68f+x)%1;
            rect(b,p,x,y-.88f+scan,width,.035f,"60EEE533");
            line(b,p,x+.35f,y-1,x+.14f,y-1.38f,.04f,"5CFFDD99");
            if(label.contains(">") || label.contains("<")) {
                float dir=label.contains("<")?-1:1;
                float tip=x+(dir<0?-.35f:width+.35f)+dir*.26f*scan;
                line(b,p,tip-dir*.26f,y-.14f,tip,y-.38f,.06f,"7CFFDF");
                line(b,p,tip,y-.38f,tip-dir*.26f,y-.62f,.06f,"7CFFDF");
            }
            b.setColor(Color.WHITE);
            lettering.setColor(lit?.61f:.49f,1,.95f,near?1:.9f);
            lettering.draw(b,label,x+.35f,y+.1f);
        }
        for(float x=0;x<map.getFloat("width");x+=2)if(visible(x,-2,1,1,camera))line(b,p,x,-2,x+.7f,-1.5f,.07f,"713149");
        b.setColor(Color.WHITE);
    }
    private void drawDoor(SpriteBatch b, JsonValue e, boolean open) {
        float dw=config.getFloat("doorWidth"),dh=config.getFloat("doorHeight");
        float dx=e.getFloat("x")+e.getFloat("width")/2-dw/2,dy=e.getFloat("y")-dh*(1-config.getFloat("doorFootY"));
        if (open) b.setColor(Color.WHITE); else b.setColor(.35f,.35f,.43f,1);
        b.draw(door,dx,dy,dw,dh);
        b.setColor(Color.WHITE);
    }
    void foreground(SpriteBatch b,Texture p,OrthographicCamera camera){atmosphere.foreground(b,p,camera);}
    @Override public void dispose(){
        atmosphere.dispose();
        for (Texture sheet : scenerySheets.values()) sheet.dispose();
        lettering.dispose();city.dispose();metal.dispose();door.dispose();anchor.dispose();
    }
}
