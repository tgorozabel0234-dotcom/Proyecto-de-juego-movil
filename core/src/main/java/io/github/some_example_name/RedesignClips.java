package io.github.some_example_name;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
import java.util.*;

/** Datos reutilizables; no crea texturas ni necesita OpenGL. */
public final class RedesignClips {
    public static final String CONFIG="characters/redesign/pixel_white_v2_shirt/animations/integration_v1/runtime.json";
    public static final class Frame {
        public String file;public float duration,px,py,hx,hy,handX,handY,floorY;
    }
    public static final class Clip {
        public String id;public float ppu;public boolean loop;public Frame[] frames;
        public int[] miss,hit;public JsonValue arm;
        public int index(float time,boolean impact){
            int[] seq=impact?hit:miss;
            float length=0;for(int i:seq)length+=frames[i].duration;
            if(loop&&length>0)time=time%length;
            for(int i:seq){float d=frames[i].duration;if(d<=0||time<d)return i;time-=d;}
            return seq[seq.length-1];
        }
    }
    private final Map<String,Clip> clips=new LinkedHashMap<>();
    private final float renderScale;
    public float getRenderScale(){return renderScale;}
    public Collection<Clip> all(){return clips.values();}
    public Clip get(String id){Clip c=clips.get(id);if(c==null)throw new IllegalArgumentException("Animacion desconocida: "+id);return c;}
    public RedesignClips(FileHandle file){
        JsonValue root=new JsonReader().parse(file);
        renderScale=root.getFloat("renderScale",1);
        if(renderScale<=0)throw new IllegalArgumentException("Escala visual inválida");
        for(JsonValue v=root.get("clips").child;v!=null;v=v.next){
            Clip c=new Clip();c.id=v.getString("id");c.ppu=v.getFloat("pixelsPerUnit");c.loop=v.getBoolean("loop");c.arm=v.get("aimArm");
            JsonValue entries=v.get("frames");c.frames=new Frame[entries.size];int i=0;
            for(JsonValue e=entries.child;e!=null;e=e.next){Frame f=new Frame();f.file=e.getString("file");f.duration=e.getFloat("duration");
                f.px=e.get("pivot").getFloat("x");f.py=e.get("pivot").getFloat("y");f.hx=e.get("hip").getFloat("x");f.hy=e.get("hip").getFloat("y");f.handX=e.get("hand").getFloat("x");f.handY=e.get("hand").getFloat("y");f.floorY=e.getFloat("floorY",f.py);c.frames[i++]=f;}
            c.miss=new int[c.frames.length];for(i=0;i<c.miss.length;i++)c.miss[i]=i;c.hit=c.miss;
            JsonValue sequences=v.get("sequences");if(sequences!=null){JsonValue miss=sequences.get("miss"),hit=sequences.get("hit");if(miss!=null)c.miss=miss.asIntArray();if(hit!=null)c.hit=hit.asIntArray();}
            clips.put(c.id,c);
        }
    }
}
