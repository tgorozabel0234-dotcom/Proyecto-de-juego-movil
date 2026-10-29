package io.github.some_example_name;

/** Vida y proteccion breve del jugador, sin depender de la ventana. */
public final class PlayerVitals {
    public int cellCount=10;
    public float maxHealth=10, invulnerabilityDuration=0.8f;
    private float health=10, invulnerability;
    public float getHealth() { return health; }
    public float getCellFill(int index) {
        if(index<0||index>=Math.max(1,cellCount))return 0;
        return Math.max(0,Math.min(1,health*Math.max(1,cellCount)/Math.max(1,maxHealth)-index));
    }
    public boolean alive() { return health>0; }
    public boolean isInvulnerable() { return invulnerability>0; }
    public void reset() { health=maxHealth; invulnerability=0; }
    public void update(float dt) { invulnerability=Math.max(0,invulnerability-dt); }
    public boolean receiveDamage(float amount) {
        if(!alive()||isInvulnerable()||amount<=0) return false;
        health=Math.max(0,health-amount); invulnerability=invulnerabilityDuration;return true;
    }
}
