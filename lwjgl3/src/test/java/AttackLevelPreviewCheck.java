import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import io.github.some_example_name.*;
import java.lang.reflect.Field;

/** Capturas dentro del nivel real, no solo de hojas aisladas. */
public final class AttackLevelPreviewCheck {
    static boolean failed;
    static Object get(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return f.get(o);}
    public static void main(String[] args){
        System.setProperty("game.touchHud","false");Lwjgl3ApplicationConfiguration cfg=new Lwjgl3ApplicationConfiguration();cfg.setWindowedMode(960,540);cfg.setInitialVisible(false);cfg.disableAudio(true);cfg.setForegroundFPS(30);cfg.setIdleFPS(30);
        new Lwjgl3Application(new Game(){
            TestLevelScreen level;PlayerBody p;CombatSystem c;PlayerVitals life;RedesignAnimation animation;int ticks,phase;float spawnX,spawnY;
            final String[] names={"neutral-2x","sweep-prepare","sweep-active","sweep-follow","up-active","thrust-active","down-air","neutral-after"};
            @Override public void create(){try{level=new TestLevelScreen(this,null);setScreen(level);p=(PlayerBody)get(level,"player");c=(CombatSystem)get(level,"combat");life=(PlayerVitals)get(level,"life");animation=(RedesignAnimation)get(level,"playerAnimation");c.enemies.clear();}catch(Exception e){throw new RuntimeException(e);}}
            @Override public void render(){try{
                if(ticks++<15){level.render(1f/30);spawnX=p.x;spawnY=p.y;return;}
                if(!p.isGrounded()&&phase==0){level.render(1f/30);spawnX=p.x;spawnY=p.y;return;}
                p.reset(spawnX,spawnY+(phase==6?4:0));p.update(.02f,0,false);c.reset();life.reset();animation.reset(10);
                float t=0;switch(phase){case 1:c.attack=CombatSystem.Attack.SWEEP;t=.01f;break;case 2:c.attack=CombatSystem.Attack.SWEEP;t=.12f;break;case 3:c.attack=CombatSystem.Attack.SWEEP;t=.25f;break;case 4:c.attack=CombatSystem.Attack.UP;t=.12f;break;case 5:c.attack=CombatSystem.Attack.THRUST;t=.12f;break;case 6:c.attack=CombatSystem.Attack.DOWN;t=.12f;break;default:c.attack=CombatSystem.Attack.NONE;}
                c.elapsed=t;animation.update(0,p,c,life,0);Field f=RedesignAnimation.class.getDeclaredField("time");f.setAccessible(true);f.setFloat(animation,t);level.render(0);
                Pixmap image=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());com.badlogic.gdx.files.FileHandle file=Gdx.files.local("../lwjgl3/build/reports/character-polish/"+names[phase]+".png");file.parent().mkdirs();PixmapIO.writePNG(file,image,-1,true);image.dispose();System.out.println(file.file().getAbsolutePath());
                if(++phase==names.length)Gdx.app.exit();
            }catch(Throwable e){failed=true;e.printStackTrace();Gdx.app.exit();}}
            @Override public void dispose(){level.dispose();}
        },cfg);if(failed)throw new AssertionError("Revisión de ataques fallida");
    }
}
