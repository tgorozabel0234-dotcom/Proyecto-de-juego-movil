import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import io.github.some_example_name.MenuScreen;

/** Render real del menú principal, selector y disposición vertical. */
public class MenuPreviewCheck {
    static boolean failed;
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg=new Lwjgl3ApplicationConfiguration();
        cfg.setWindowedMode(1280,720);cfg.setInitialVisible(false);cfg.disableAudio(true);
        new Lwjgl3Application(new Game(){
            MenuScreen menu;int phase,ticks;
            public void create(){menu=new MenuScreen(this);setScreen(menu);}
            void selector(boolean open)throws Exception{java.lang.reflect.Method m=MenuScreen.class.getDeclaredMethod("setLevelsOpen",boolean.class);m.setAccessible(true);m.invoke(menu,open);}
            void capture(String name){Pixmap p=Pixmap.createFromFrameBuffer(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());com.badlogic.gdx.files.FileHandle f=Gdx.files.local("../lwjgl3/build/reports/menu/"+name+".png");f.parent().mkdirs();PixmapIO.writePNG(f,p,-1,true);p.dispose();System.out.println("Rendered "+name);}
            public void render(){try{menu.render(1f/30);if(++ticks<25)return;ticks=0;
                if(phase==0){capture("main-landscape");selector(true);phase++;}
                else if(phase==1){capture("levels-landscape");selector(false);Gdx.graphics.setWindowedMode(540,960);phase++;}
                else{capture("main-portrait");Gdx.app.exit();}
            }catch(Throwable e){failed=true;e.printStackTrace();Gdx.app.exit();}}
            public void dispose(){menu.dispose();}
        },cfg);
        if(failed)throw new AssertionError("Menu preview failed");
    }
}
