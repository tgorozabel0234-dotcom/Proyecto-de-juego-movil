import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.*;
import java.lang.reflect.Field;

/** Render real de pruebas; usa ventana oculta y exporta capturas, sin tocar preferencias. */
public class HudPreviewCheck {
    static boolean failed;
    public static void main(String[] args) {
        System.setProperty("game.touchHud","true");
        Lwjgl3ApplicationConfiguration c=new Lwjgl3ApplicationConfiguration();
        c.setTitle("HUD verification");c.setWindowedMode(960,540);c.setInitialVisible(false);
        c.setForegroundFPS(30);c.setIdleFPS(30);c.disableAudio(true);
        new Lwjgl3Application(new Game(){
            int frame,phase;TestLevelScreen level;GameHud hud;TouchControls touch;PlayerBody body;PlayerVitals life;
            @Override public void create(){
                level=new TestLevelScreen(this,null);setScreen(level);
                try {
                    Field f=TestLevelScreen.class.getDeclaredField("gameHud");f.setAccessible(true);hud=(GameHud)f.get(level);
                    f=TestLevelScreen.class.getDeclaredField("touch");f.setAccessible(true);touch=(TouchControls)f.get(level);
                    f=TestLevelScreen.class.getDeclaredField("player");f.setAccessible(true);body=(PlayerBody)f.get(level);
                    f=TestLevelScreen.class.getDeclaredField("life");f.setAccessible(true);life=(PlayerVitals)f.get(level);
                    level.render(1f/60);
                } catch(Exception e){throw new RuntimeException(e);}
            }
            void tapAt(float x,float y,int pointer,boolean down){
                Vector2 v=hud.stage.getViewport().project(new Vector2(x,y));
                if(down)hud.stage.touchDown(Math.round(v.x),Gdx.graphics.getHeight()-Math.round(v.y),pointer,0);
                else hud.stage.touchUp(Math.round(v.x),Gdx.graphics.getHeight()-Math.round(v.y),pointer,0);
            }
            void save(String name){
                Pixmap pix=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());
                com.badlogic.gdx.files.FileHandle file=Gdx.files.local("../lwjgl3/build/reports/hud/"+name+".png");
                file.parent().mkdirs();PixmapIO.writePNG(file,pix,-1,true);pix.dispose();
                System.out.println("HUD preview: "+file.file().getAbsolutePath());
            }
            void click(String name){
                com.badlogic.gdx.scenes.scene2d.Actor actor=hud.stage.getRoot().findActor(name);
                if(actor==null)throw new AssertionError("No existe boton "+name);
                Vector2 point=actor.localToStageCoordinates(new Vector2(actor.getWidth()/2,actor.getHeight()/2));
                tapAt(point.x,point.y,4,true);tapAt(point.x,point.y,4,false);
            }
            @Override public void render(){
                try {
                    level.render(0);
                    if(++frame<8)return;
                    frame=0;
                    if(phase==0){
                        save("landscape");
                        hud.toggleTouch();level.render(0);save("desktop-life-only");
                        if(hud.stage.getRoot().findActor("pause").isVisible())throw new AssertionError("En modo teclado solo debe verse la vida");
                        hud.toggleTouch();level.render(0);
                        HudLayout layout=new HudLayout(hud.stage.getWidth(),hud.stage.getHeight(),0,0,0,18,144);
                        tapAt(layout.stick.x+layout.stick.w*.75f,layout.stick.y+layout.stick.h/2,0,true);
                        HudLayout.Box b=layout.actions[TouchControls.Action.JUMP.ordinal()];tapAt(b.x+b.w/2,b.y+b.h/2,1,true);
                        b=layout.actions[TouchControls.Action.ATTACK.ordinal()];tapAt(b.x+b.w/2,b.y+b.h/2,2,true);
                        if(!touch.held(TouchControls.Action.JUMP)||!touch.held(TouchControls.Action.ATTACK)||touch.moveX()<=0)
                            throw new AssertionError("Scene2D no recibe tres dedos simultaneos");
                        float beforeX=body.x,beforeY=body.y;level.render(1f/60);
                        if(body.x<=beforeX||body.y<=beforeY)throw new AssertionError("Los controles dibujados no mueven y saltan con el jugador");
                        java.lang.reflect.Method restart=TestLevelScreen.class.getDeclaredMethod("restartLevel");restart.setAccessible(true);restart.invoke(level);
                        hud.cancelTouches();click("pause");
                        if(!hud.isPaused())throw new AssertionError("Boton pausa no responde");
                        beforeX=body.x;beforeY=body.y;level.render(.1f);
                        if(body.x!=beforeX||body.y!=beforeY)throw new AssertionError("Pausa no detiene simulacion");
                    } else if(phase==1){save("pause");click("CONTINUAR");
                        if(hud.isPaused())throw new AssertionError("Boton continuar no responde");
                        Gdx.graphics.setWindowedMode(450,800);}
                    else if(phase==2){save("portrait");hud.toggleHelp();}
                    else if(phase==3){save("help-portrait");hud.closePanels();Gdx.graphics.setWindowedMode(1280,540);}
                    else if(phase==4){
                        save("ultrawide");
                        body.update(.25f,1,false,true,true,true);life.receiveDamage(3);
                        level.render(0);hud.stage.act(.55f);level.render(0);save("cyber-damage-dash");
                        life.update(.81f);life.receiveDamage(6);level.render(0);hud.stage.act(.55f);level.render(0);save("cyber-low-health");
                        Gdx.app.exit();
                    }
                    phase++;
                }catch(Throwable e){failed=true;e.printStackTrace();Gdx.app.exit();}
            }
            @Override public void dispose(){level.dispose();}
        },c);
        if(failed)throw new AssertionError("Fallo en verificacion visual del HUD");
    }
}
