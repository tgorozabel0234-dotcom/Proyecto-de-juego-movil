package io.github.some_example_name.enemies;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.PlayerBody;
import java.util.List;

/** Clase base abstracta para todos los enemigos con animaciones en el juego. */
public abstract class Enemy {
    public float x, y;
    public float width, height;
    public float velocityX, velocityY;
    public boolean active = true;
    public float health = 100f;
    public float maxHealth = 100f;
    public float facing = 1f; // 1 = derecha, -1 = izquierda
    protected float stateTime = 0f;
    protected boolean isDying = false;

    public Enemy(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(float delta, PlayerBody player, List<float[]> solids);

    public abstract void draw(SpriteBatch batch);

    public boolean collidesWithPlayer(PlayerBody player) {
        if (!active || isDying) return false;
        return x < player.x + PlayerBody.WIDTH &&
               x + width > player.x &&
               y < player.y + PlayerBody.HEIGHT &&
               y + height > player.y;
    }

    public void takeDamage(float amount) {
        if (!active || isDying) return;
        health -= amount;
        if (health <= 0) {
            health = 0;
            isDying = true;
            stateTime = 0f;
        }
    }

    public abstract void dispose();
}
