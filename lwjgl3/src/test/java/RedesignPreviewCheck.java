import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.*;

/** Exporta el renderer real: clips, cuerda invertida y apuntado terrestre. */
public class RedesignPreviewCheck {
    static boolean failed;
    static void field(PlayerBody p,String name,float value)throws Exception{java.lang.reflect.Field f=PlayerBody.class.getDeclaredField(name);f.setAccessible(true);f.setFloat(p,value);}
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();config.setWindowedMode(1280,800);config.setInitialVisible(false);config.disableAudio(true);config.setForegroundFPS(30);config.setIdleFPS(30);
        new Lwjgl3Application(new ApplicationAdapter(){
            RedesignSprite sprite;SpriteBatch batch;BitmapFont font;OrthographicCamera camera;Texture pixel;int tick,page;
            @Override public void create(){sprite=new RedesignSprite();batch=new SpriteBatch();font=new BitmapFont();camera=new OrthographicCamera();Pixmap pix=new Pixmap(1,1,Pixmap.Format.RGBA8888);pix.setColor(Color.WHITE);pix.fill();pixel=new Texture(pix);pix.dispose();}
            void line(float x,float y,float ex,float ey){float d=Vector2.dst(x,y,ex,ey);batch.draw(pixel,x,y-.025f,0,.025f,d,.05f,1,1,MathUtils.atan2(ey-y,ex-x)*MathUtils.radiansToDegrees,0,0,1,1,false,false);}
            void gallery(float time){
                camera.setToOrtho(false,1280,800);batch.setProjectionMatrix(camera.combined);batch.begin();int index=0;
                for(RedesignClips.Clip clip:sprite.getClips().all()){
                    int col=index%5,row=index/5;RedesignClips.Frame frame=clip.frames[clip.index(time,true)];Pixmap image=new Pixmap(Gdx.files.internal(frame.file));int minX=image.getWidth(),minY=image.getHeight(),maxX=0,maxY=0;
                    for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)if((image.getPixel(x,y)&255)>128){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}image.dispose();
                    float scale=.55f,facing=col%2==0?1:-1,cx=col*256+128,cy=800-row*160-90;
                    float x=cx-((minX+maxX)/2f-frame.px)*scale*facing,y=cy+((minY+maxY)/2f-frame.py)*scale;
                    batch.setColor(Color.WHITE);sprite.drawSample(batch,clip.id,time,true,x,y,scale,facing,0);font.setColor(.3f,.95f,1,1);font.draw(batch,clip.id+" / "+clip.frames.length+" poses",col*256+10,800-row*160-12);index++;
                }batch.end();
            }
            void orientation(boolean whirlwind)throws Exception{
                camera.setToOrtho(false,32,20);batch.setProjectionMatrix(camera.combined);batch.begin();
                for(int i=0;i<8;i++){
                    float deg=-180+i*45,rad=deg*MathUtils.degreesToRadians,ax=(i%4)*8+4,ay=15-(i/4)*10;
                    PlayerBody p=new PlayerBody();p.addSolid(-100,10,200,1);p.reset(0,5);if(!p.launchPendulum(0,1))throw new AssertionError("Anclaje de fixture");
                    field(p,"anchorX",ax);field(p,"anchorY",ay);p.x=ax+MathUtils.sin(rad)*2-PlayerBody.WIDTH/2;p.y=ay-MathUtils.cos(rad)*2-PlayerBody.HEIGHT/2;
                    field(p,"velocityX",MathUtils.cos(rad)*5);field(p,"velocityY",MathUtils.sin(rad)*5);
                    CombatSystem combat=new CombatSystem();if(whirlwind)combat.attack=CombatSystem.Attack.WHIRLWIND;
                    RedesignAnimation a=new RedesignAnimation();a.reset(10);a.update(.01f,p,combat,new PlayerVitals(),0);a.update(.1f,p,combat,new PlayerVitals(),0);sprite.prepare(a,p,0,1);
                    float hx=sprite.getHandX(),hy=sprite.getHandY();if(!Float.isFinite(hx)||!Float.isFinite(hy))throw new AssertionError("Union invalida");
                    batch.setColor(.2f,.95f,1,1);line(hx,hy,ax,ay);batch.setColor(1,.65f,.1f,1);batch.draw(pixel,ax-.08f,ay-.08f,.16f,.16f);batch.setColor(Color.WHITE);sprite.draw(batch);
                }batch.end();
            }
            void aim(){
                camera.setToOrtho(false,32,20);batch.setProjectionMatrix(camera.combined);batch.begin();
                for(int i=0;i<8;i++){
                    float angle=i*45*MathUtils.degreesToRadians;PlayerBody p=new PlayerBody();p.addSolid(-100,-2,200,2);p.reset(0,0);p.update(.01f,0,false);p.x=(i%4)*8+3;p.y=14-(i/4)*10;
                    RedesignAnimation a=new RedesignAnimation();CombatSystem c=new CombatSystem();PlayerVitals life=new PlayerVitals();a.update(.01f,p,c,life,.22f);a.update(.1f,p,c,life,.22f);
                    sprite.prepare(a,p,MathUtils.cos(angle),MathUtils.sin(angle));float hx=sprite.getHandX(),hy=sprite.getHandY();batch.setColor(.2f,.95f,1,1);line(hx,hy,hx+MathUtils.cos(angle)*2,hy+MathUtils.sin(angle)*2);batch.setColor(Color.WHITE);sprite.draw(batch);
                }batch.end();
            }
            @Override public void render(){try{
                Gdx.gl.glViewport(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());ScreenUtils.clear(.025f,.045f,.07f,1);if(page<3)gallery(new float[]{0,.18f,3}[page]);else if(page<5)orientation(page==4);else aim();
                if(++tick>=2){tick=0;Pixmap image=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());com.badlogic.gdx.files.FileHandle path=Gdx.files.local("../lwjgl3/build/reports/redesign/"+new String[]{"poses-start","poses-active","poses-final","pendulum-angles","whirlwind-angles","ground-aim"}[page]+".png");path.parent().mkdirs();PixmapIO.writePNG(path,image,-1,true);image.dispose();System.out.println(path.file().getAbsolutePath());if(++page==6)Gdx.app.exit();}
            }catch(Throwable e){failed=true;e.printStackTrace();Gdx.app.exit();}}
            @Override public void dispose(){sprite.dispose();batch.dispose();font.dispose();pixel.dispose();}
        },config);
        if(failed)throw new AssertionError("Fallo del renderer nuevo");
    }
}
