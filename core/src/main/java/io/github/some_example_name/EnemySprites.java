package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Map;

/** Renderiza los PNG sueltos del pack; la IA y el combate viven en sus sistemas. */
public final class EnemySprites {
    private final Map<String, Array<TextureRegion>> clips = new HashMap<>();
    private final Array<Texture> ownedTextures = new Array<>();
    private final IdentityHashMap<CombatSystem.Enemy, Pose> poses = new IdentityHashMap<>();
    private static final class Pose { String clip = ""; float started; }

    private void load(String key, String directory, String prefix, int count) {
        Array<TextureRegion> frames = new Array<>();
        for (int i=0; i<count; i++) {
            String path = directory + "/frames/" + prefix + String.format("_%02d.png", i);
            if (!Gdx.files.internal(path).exists()) continue;
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            ownedTextures.add(texture);
            frames.add(new TextureRegion(texture));
        }
        if (frames.size > 0) clips.put(key, frames);
    }
    private void ensureLoaded() {
        if (!clips.isEmpty()) return;
        load("robot_idle","Enemies/Robot","idle",6);
        load("robot_walk","Enemies/Robot","walk",8);
        load("robot_attack","Enemies/Robot","static_attack",6);
        load("robot_hit","Enemies/Robot","get_hit",2);
        load("robot_death","Enemies/Robot","death",6);
        load("scorp_walk","Enemies/Scorp","walk",5);
        load("scorp_attack","Enemies/Scorp","pincer",3);
        load("scorp_death","Enemies/Scorp","damage_death",6);
    }
    public void draw(SpriteBatch batch, CombatSystem.Enemy e, float time) {
        boolean robot = e.type == CombatSystem.EnemyType.ROBOT;
        if (!robot && e.type != CombatSystem.EnemyType.SCORP) return;
        ensureLoaded();
        String clip = robot ? (!e.alive()?"robot_death":e.isStunned()?"robot_hit":e.state==CombatSystem.EnemyState.WINDUP?"robot_attack":e.state==CombatSystem.EnemyState.CHASE?"robot_walk":"robot_idle")
            : (!e.alive()||e.isStunned()?"scorp_death":e.state==CombatSystem.EnemyState.WINDUP?"scorp_attack":"scorp_walk");
        Array<TextureRegion> frames = clips.get(clip);
        if (frames == null || frames.size == 0) return;
        Pose pose=poses.computeIfAbsent(e,k->new Pose());
        if(!pose.clip.equals(clip)){pose.clip=clip;pose.started=time;}
        float elapsed=Math.max(0,time-pose.started);
        if(!e.alive()&&elapsed>.85f)return;
        int index=e.isStunned()?0:!e.alive()?(int)(elapsed/.14f):(int)(elapsed/.12f);
        if(e.state==CombatSystem.EnemyState.WINDUP) index=(int)(elapsed/.12f);
        TextureRegion frame=frames.get(Math.min(frames.size-1,index%frames.size));
        float width=robot?4.2f:3.5f,height=width*frame.getRegionHeight()/frame.getRegionWidth();
        batch.setColor(e.isStunned()?new Color(1,.75f,.4f,1):Color.WHITE);
        batch.draw(frame.getTexture(),e.x-width/2,e.y-e.radius,width,height,frame.getRegionX(),frame.getRegionY(),frame.getRegionWidth(),frame.getRegionHeight(),e.facing<0,false);
        batch.setColor(Color.WHITE);
    }
    public void dispose(){for(Texture texture:ownedTextures)texture.dispose();ownedTextures.clear();clips.clear();poses.clear();}
}

