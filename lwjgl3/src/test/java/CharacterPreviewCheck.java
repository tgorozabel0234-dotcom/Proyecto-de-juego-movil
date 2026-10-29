import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.*;

/** Galeria real de todas las acciones dibujada con el mismo renderer que el juego. */
public class CharacterPreviewCheck {
    static boolean failed;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280,920);config.setInitialVisible(false);config.disableAudio(true);config.setForegroundFPS(30);config.setIdleFPS(30);
        new Lwjgl3Application(new ApplicationAdapter(){
            PlayerSprite sprite;SpriteBatch batch;BitmapFont font;OrthographicCamera camera;int frame,phase;
            final float[] phases={0,.33f,.66f,.99f};
            @Override public void create(){sprite=new PlayerSprite();batch=new SpriteBatch();font=new BitmapFont();camera=new OrthographicCamera();camera.setToOrtho(false,1280,920);}
            @Override public void render(){
                try{
                    ScreenUtils.clear(.025f,.04f,.065f,1);batch.setProjectionMatrix(camera.combined);batch.begin();
                    PlayerAnimation.State[] states=PlayerAnimation.State.values();
                    for(int i=0;i<states.length;i++){
                        int col=i%5,row=i/5;float x=col*256+128,y=920-(row+1)*230+12;
                        if(states[i]==PlayerAnimation.State.WHIRLWIND)y+=24;
                        batch.setColor(Color.WHITE);sprite.drawSample(batch,states[i],phases[phase],x,y,155,col%2==0?1:-1);
                        font.setColor(.2f,.95f,.9f,1);font.draw(batch,states[i].name()+(col%2==0?"  RIGHT":"  LEFT"),col*256+12,920-row*230-10);
                    }
                    batch.end();
                    if(++frame>=5){
                        frame=0;Pixmap image=Pixmap.createFromFrameBuffer(0,0,1280,920);
                        com.badlogic.gdx.files.FileHandle path=Gdx.files.local("../lwjgl3/build/reports/character/poses-"+phase+".png");path.parent().mkdirs();
                        PixmapIO.writePNG(path,image,-1,true);image.dispose();System.out.println("Character preview: "+path.file().getAbsolutePath());
                        if(++phase>=phases.length)Gdx.app.exit();
                    }
                }catch(Throwable e){failed=true;e.printStackTrace();Gdx.app.exit();}
            }
            @Override public void dispose(){sprite.dispose();batch.dispose();font.dispose();}
        },config);
        if(failed)throw new AssertionError("Fallo al revisar las animaciones");
    }
}
