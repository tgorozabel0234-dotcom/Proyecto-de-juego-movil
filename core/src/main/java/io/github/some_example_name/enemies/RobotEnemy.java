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

/** Enemigo Robot Voltaic Slinker (Asset: Enemies/Robot).
 * Patrulla, persigue al jugador al detectarlo y realiza ataques cuerpo a cuerpo. */
public class RobotEnemy extends Enemy {
    public enum State { IDLE, PATROL, CHASE, ATTACK, DYING }

    private TextureAtlas atlas;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> hitAnim;
    private Animation<TextureRegion> deathAnim;
    private final Array<Texture> fallbackTextures = new Array<>();

    private final float minX, maxX;
    private final float patrolSpeed = 3.0f;
    private final float chaseSpeed = 5.5f;
    private State currentState = State.PATROL;
    private float attackCooldown = 0f;

    public RobotEnemy(float x, float y, float width, float height, float minX, float maxX) {
        super(x, y, width, height);
        this.minX = minX;
        this.maxX = maxX;
        this.facing = 1f;
        this.health = 100f;
        this.maxHealth = 100f;
        loadAnimations();
    }

    private void loadAnimations() {
        try {
            atlas = new TextureAtlas(Gdx.files.internal("Enemies/Robot/voltaic_slinker.atlas"));
            Array<TextureAtlas.AtlasRegion> idleRegions = atlas.findRegions("idle");
            Array<TextureAtlas.AtlasRegion> walkRegions = atlas.findRegions("walk");
            Array<TextureAtlas.AtlasRegion> attackRegions = atlas.findRegions("static_attack");
            Array<TextureAtlas.AtlasRegion> hitRegions = atlas.findRegions("get_hit");
            Array<TextureAtlas.AtlasRegion> deathRegions = atlas.findRegions("death");

            idleAnim = new Animation<>(0.13f, idleRegions.size > 0 ? (Array<TextureRegion>)(Object)idleRegions : loadFallbackFrames("idle", 6), Animation.PlayMode.LOOP);
            walkAnim = new Animation<>(0.10f, walkRegions.size > 0 ? (Array<TextureRegion>)(Object)walkRegions : loadFallbackFrames("walk", 8), Animation.PlayMode.LOOP);
            attackAnim = new Animation<>(0.09f, attackRegions.size > 0 ? (Array<TextureRegion>)(Object)attackRegions : loadFallbackFrames("static_attack", 6), Animation.PlayMode.NORMAL);
            hitAnim = new Animation<>(0.15f, hitRegions.size > 0 ? (Array<TextureRegion>)(Object)hitRegions : loadFallbackFrames("get_hit", 2), Animation.PlayMode.NORMAL);
            deathAnim = new Animation<>(0.15f, deathRegions.size > 0 ? (Array<TextureRegion>)(Object)deathRegions : loadFallbackFrames("death", 6), Animation.PlayMode.NORMAL);
        } catch (Exception e) {
            Gdx.app.log("RobotEnemy", "Atlas loading failed, falling back to individual frames: " + e.getMessage());
            idleAnim = new Animation<>(0.13f, loadFallbackFrames("idle", 6), Animation.PlayMode.LOOP);
            walkAnim = new Animation<>(0.10f, loadFallbackFrames("walk", 8), Animation.PlayMode.LOOP);
            attackAnim = new Animation<>(0.09f, loadFallbackFrames("static_attack", 6), Animation.PlayMode.NORMAL);
            hitAnim = new Animation<>(0.15f, loadFallbackFrames("get_hit", 2), Animation.PlayMode.NORMAL);
            deathAnim = new Animation<>(0.15f, loadFallbackFrames("death", 6), Animation.PlayMode.NORMAL);
        }
    }

    private Array<TextureRegion> loadFallbackFrames(String prefix, int count) {
        Array<TextureRegion> regions = new Array<>();
        for (int i = 0; i < count; i++) {
            String path = String.format("Enemies/Robot/frames/%s_%02d.png", prefix, i);
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

        // Estado de Ataque
        if (currentState == State.ATTACK) {
            if (attackAnim.isAnimationFinished(stateTime)) {
                currentState = State.PATROL;
                stateTime = 0f;
            } else if (collidesWithPlayer(player) && attackAnim.getKeyFrameIndex(stateTime) == 3) {
                // Impacto en el fotograma activo
                player.reset(player.x - facing * 1.2f, player.y);
            }
            return;
        }

        // Detección del jugador (rango 12 unidades)
        if (dist < 12f && Math.abs(dy) < 3.5f) {
            facing = dx > 0 ? 1f : -1f;
            if (dist < 1.8f && attackCooldown <= 0) {
                currentState = State.ATTACK;
                stateTime = 0f;
                attackCooldown = 1.2f;
                return;
            } else {
                currentState = State.CHASE;
            }
        } else {
            currentState = State.PATROL;
        }

        float speed = (currentState == State.CHASE) ? chaseSpeed : patrolSpeed;
        velocityX = facing * speed;
        float newX = x + velocityX * delta;

        // Limite de patrulla
        if (newX <= minX) {
            newX = minX;
            facing = 1f;
        } else if (newX + width >= maxX) {
            newX = maxX - width;
            facing = -1f;
        }

        // Colisión lateral con sólidos
        for (float[] s : solids) {
            float sx = s[0], sy = s[1], sw = s[2], sh = s[3];
            if (y < sy + sh && y + height > sy) {
                if (velocityX > 0 && newX + width > sx && x + width <= sx) {
                    newX = sx - width;
                    facing = -1f;
                } else if (velocityX < 0 && newX < sx + sw && x >= sx + sw) {
                    newX = sx + sw;
                    facing = 1f;
                }
            }
        }

        x = newX;

        // Interacción por colisión directa
        if (collidesWithPlayer(player)) {
            if (player.isDashing() || player.isPropelling()) {
                takeDamage(40f);
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
                case ATTACK:
                    frame = attackAnim.getKeyFrame(stateTime, false);
                    break;
                case CHASE:
                case PATROL:
                    frame = walkAnim.getKeyFrame(stateTime, true);
                    break;
                case IDLE:
                default:
                    frame = idleAnim.getKeyFrame(stateTime, true);
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
