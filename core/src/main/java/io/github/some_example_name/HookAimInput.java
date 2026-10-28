package io.github.some_example_name;

/** Agrupa una pulsacion de la tecla de accion y sus flechas sin repetir disparos al mantener V. */
public final class HookAimInput {
    private float remaining = -1;
    private float aimX = 1, aimY;
    private float bufferedX = 1, bufferedY;
    private boolean wasHeld;
    public float getAimX() { return aimX; }
    public float getAimY() { return aimY; }
    public void reset() { remaining = -1; wasHeld = false; }

    public boolean update(float delta, boolean left, boolean right, boolean up, boolean down,
                          boolean pressed, boolean held, float facing) {
        float x = (right ? 1 : 0) - (left ? 1 : 0);
        float y = (up ? 1 : 0) - (down ? 1 : 0);
        aimX = x == 0 && y == 0 ? facing : x;
        aimY = y;
        boolean newPress = pressed || (held && !wasHeld);
        wasHeld = held;
        if (newPress) {
            remaining = 0.12f;
            bufferedX = aimX; bufferedY = aimY;
        } else if (remaining >= 0 && (x != 0 || y != 0)) {
            bufferedX = x; bufferedY = y;
        }
        if (remaining < 0) return false;
        // No descuenta tiempo anterior a la pulsacion de la tecla de accion en el primer cuadro.
        if (!newPress) remaining -= Math.max(0, Math.min(delta, 0.1f));
        // Diagonal completa: inmediata. Direccion simple: hasta 120 ms para completar
        // la combinacion. Conserva el apuntado si todas las teclas se sueltan juntas.
        if ((x != 0 && y != 0) || remaining <= 0 || !held) {
            remaining = -1;
            aimX = bufferedX; aimY = bufferedY;
            return true;
        }
        return false;
    }
}
