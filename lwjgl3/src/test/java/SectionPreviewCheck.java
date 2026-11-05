import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.JsonValue;
import io.github.some_example_name.*;
import java.lang.reflect.Field;
public class SectionPreviewCheck {
 static boolean failed;
 static Object get(Object obj,String name)throws Exception{Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(obj);}
 static void set(Object obj,String name,Object val)throws Exception{Field f=obj.getClass().getDeclaredField(name);f.setAccessible(true);f.set(obj,val);}
 public static void main(String[] args){System.setProperty("game.touchHud","false");Lwjgl3ApplicationConfiguration cfg=new Lwjgl3ApplicationConfiguration();cfg.setWindowedMode(1280,800);cfg.setInitialVisible(false);cfg.disableAudio(true);
 new Lwjgl3Application(new Game(){TestLevelScreen level;int room=1,ticks;
 void load(){level=new TestLevelScreen(this,null,"levels/"+(room==1?"section_1":"section_1_room_"+room)+".json");setScreen(level);try{set(level,"overview",true);}catch(Exception e){throw new RuntimeException(e);}}
 void capture(String name){Pixmap p=Pixmap.createFromFrameBuffer(0,0,1280,800);com.badlogic.gdx.files.FileHandle f=Gdx.files.local("../lwjgl3/build/reports/section-1/room-"+room+name+".png");f.parent().mkdirs();PixmapIO.writePNG(f,p,-1,true);p.dispose();}
 void detail(String name,float x,float y)throws Exception{set(level,"overview",false);OrthographicCamera c=(OrthographicCamera)get(level,"camera");c.position.set(x,y,0);level.render(0);capture(name);}
 public void create(){load();}
 public void render(){try{level.render(1f/60);if(++ticks<4)return;capture("");JsonValue map=(JsonValue)get(level,"map");JsonValue spawn=map.get("spawn");detail("-entry",spawn.getFloat("x")+9,spawn.getFloat("y")+7);int total=0;for(JsonValue q=map.get("pieces").child;q!=null;q=q.next)if(q.getString("role", "").equals("platform"))total++;int i=0;for(JsonValue q=map.get("pieces").child;q!=null;q=q.next)if(q.getString("role", "").equals("platform")&&i++==total/2){detail("-middle",q.getFloat("x")+q.getFloat("width")/2,q.getFloat("y")+q.getFloat("height")+6);break;}JsonValue e=map.get("exit");detail("-exit",e.getFloat("x")-7,e.getFloat("y")+6);System.out.println("Rendered room "+room+" overview, entry, middle and exit");if(room==4){Gdx.app.exit();return;}level.dispose();room++;ticks=0;load();}catch(Throwable e){e.printStackTrace();failed=true;Gdx.app.exit();}}
 public void dispose(){level.dispose();}},cfg);if(failed)throw new AssertionError("Section preview failed");}
}
