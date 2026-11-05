package io.github.some_example_name.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.PlayerBody;
import java.util.List;

/** Enemigo Scorpius-C1 (Asset: Enemies/Scorp).
 * Acecha en plataformas elevadas, realiza ataques de tenaza y disparos de energía. */
public class ScorpiusEnemy extends Enemy {
    public enum State { PATROL, PINCER_ATTACK, FIRE_ATTACK, DYING }

    private TextureAtlas atlas;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> fireAnim;
    private Animation<TextureRegion> pincerAnim;
    private Animation<TextureRegion> deathAnim;
    private final Array<Texture> fallbackTextures = new Array<>();

    private final float minX, maxX;
    private final float patrolSpeed = 2.2f;
    private State currentState = State.PATROL;
    private float attackCooldown = 0f;

    public ScorpiusEnemy(float x, float y, float width, float height, float minX, float maxX) {
        super(x, y, width, height);
        this.minX = minX;
        this.maxX = maxX;
        this.facing = 1f;
        this.health = 90f;
        this.maxHealth = 90f;
        loadAnimations();
    }

    private void loadAnimations() {
        try {
            atlas = new TextureAtlas(Gdx.files.internal("Enemies/Scorp/scorpius_c1.atlas"));
            Array<TextureAtlas.AtlasRegion> walkRegions = atlas.findRegions("walk");
            Array<TextureAtlas.AtlasRegion> fireRegions = atlas.findRegions("fire");
            Array<TextureAtlas.AtlasRegion> pincerRegions = atlas.findRegions("pincer");
            Array<TextureAtlas.AtlasRegion> deathRegions = atlas.findRegions("damage_death");

            walkAnim = new Animation<>(0.10f, walkRegions.size > 0 ? (Array<TextureRegion>)(Object)walkRegions : loadFallbackFrames("walk", 5), Animation.PlayMode.LOOP);
            fireAnim = new Animation<>(0.08f, fireRegions.size > 0 ? (Array<TextureRegion>)(Object)fireRegions : loadFallbackFrames("fire", 5), Animation.PlayMode.NORMAL);
            pincerAnim = new Animation<>(0.08f, pincerRegions.size > 0 ? (Array<TextureRegion>)(Object)pincerRegions : loadFallbackFrames("pincer", 3), Animation.PlayMode.NORMAL);
            deathAnim = new Animation<>(0.12f, deathRegions.size > 0 ? (Array<TextureRegion>)(Object)deathRegions : loadFallbackFrames("damage_death", 6), Animation.PlayMode.NORMAL);
        } catch (Exception e) {
            Gdx.app.log("ScorpiusEnemy", "Atlas loading failed, falling back to individual frames: " + e.getMessage());
            walkAnim = new Animation<>(0.10f, loadFallbackFrames("walk", 5), Animation.PlayMode.LOOP);
            fireAnim = new Animation<>(0.08f, loadFallbackFrames("fire", 5), Animation.PlayMode.NORMAL);
            pincerAnim = new Animation<>(0.08f, loadFallbackFrames("pincer", 3), Animation.PlayMode.NORMAL);
            deathAnim = new Animation<>(0.12f, loadFallbackFrames("damage_death", 6), Animation.PlayMode.NORMAL);
        }
    }

    private Array<TextureRegion> loadFallbackFrames(String prefix, int count) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = 0; i < count; i++) {
            String path = String.format("Enemies/Scorp/frames/%s_%02d.png", prefix, i);
            if (Gdx.files.internal(path).exists()) {
                Texture tex = new Texture(Gdx.files.internal(path));
                fallbackTextures.add(tex);
                regions.add(new TextureRegion(tex));
            }
        }
        return regions;
    }

    @Override
    public void update(float delta, PlayerBody player, List<float[]> solids) {
        if (!active) return;
        stateTime += delta;
        attackCooldown = Math.max(0, attackCooldown - delta);

        if (isDying) {
            if (deathAnim.isAnimationFinished(stateTime)) {
                active = false;
            }
            return;
        }

        float dx = (player.x + PlayerBody.WIDTH / 2f) - (x + width / 2f);
        float dy = (player.y + PlayerBody.HEIGHT / 2f) - (y + height / 2f);
        float dist = (float) Math.hypot(dx, dy);

        // Finalización de animaciones de ataque
        if (currentState == State.FIRE_ATTACK) {
            if (fireAnim.isAnimationFinished(stateTime)) {
                currentState = State.PATROL;
                stateTime = 0f;
            }
            return;
        } else if (currentState == State.PINCER_ATTACK) {
            if (pincerAnim.isAnimationFinished(stateTime)) {
                currentState = State.PATROL;
                stateTime = 0f;
            } else if (collidesWithPlayer(player) && pincerAnim.getKeyFrameIndex(stateTime) == 1) {
                player.reset(player.x - facing * 1.5f, player.y);
            }
            return;
        }

        // Lógica de detección y ataque
        if (dist < 14f && attackCooldown <= 0) {
            facing = dx > 0 ? 1f : -1f;
            stateTime = 0f;
            attackCooldown = 1.8f;
            if (dist < 3.0f) {
                currentState = State.PINCER_ATTACK;
            } else {
                currentState = State.FIRE_ATTACK;
            }
            return;
        }

        // Movimiento de patrulla
        currentState = State.PATROL;
        velocityX = facing * patrolSpeed;
        float newX = x + velocityX * delta;

        if (newX <= minX) {
            newX = minX;
            facing = 1f;
        } else if (newX + width >= maxX) {
            newX = maxX - width;
            facing = -1f;
        }

        x = newX;

        // Interacción por colisión
        if (collidesWithPlayer(player)) {
            if (player.isDashing() || player.isPropelling()) {
                takeDamage(50f);
            } else {
                facing = -facing;
                x = MathUtils.clamp(x + facing * 0.4f, minX, maxX - width);
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (!active || atlas == null) return;

        TextureRegion frame;
        if (isDying) {
            frame = deathAnim.getKeyFrame(stateTime, false);
        } else {
            switch (currentState) {
                case FIRE_ATTACK:
                    frame = fireAnim.getKeyFrame(stateTime, false);
                    break;
                case PINCER_ATTACK:
                    frame = pincerAnim.getKeyFrame(stateTime, false);
                    break;
                case PATROL:
                default:
                    frame = walkAnim.getKeyFrame(stateTime, true);
                    break;
            }
        }

        if (frame == null) return;

        boolean flipX = facing < 0;
        batch.draw(
            frame.getTexture(),
            x, y,
            width / 2f, height / 2f,
            width, height,
            1f, 1f, 0f,
            frame.getRegionX(), frame.getRegionY(),
            frame.getRegionWidth(), frame.getRegionHeight(),
            flipX, false
        );
    }

    @Override
    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
            atlas = null;
        }
        for (int i = 0; i < fallbackTextures.size; i++) {
            fallbackTextures.get(i).dispose();
        }
        fallbackTextures.clear();
    }
}
