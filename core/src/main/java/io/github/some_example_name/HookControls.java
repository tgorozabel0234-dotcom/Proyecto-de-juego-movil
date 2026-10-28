package io.github.some_example_name;

/** U: propulsion por pulsacion. I: pendulo mantenido. Cada accion tiene su propio buffer. */
public final class HookControls {
    public enum Command { NONE, PROPULSION, PENDULUM }
    private final HookAimInput linear = new HookAimInput();
    private final HookAimInput swing = new HookAimInput();
    private boolean ignoreSwingUntilRelease;
    private float aimX = 1, aimY;
    public float getAimX() { return aimX; }
    public float getAimY() { return aimY; }
    public void reset() { linear.reset(); swing.reset(); ignoreSwingUntilRelease = false; }

    public Command update(float dt, boolean left, boolean right, boolean up, boolean down,
                          boolean propulsionPressed, boolean propulsionHeld, boolean pendulumPressed, boolean pendulumHeld, float facing) {
        if (!pendulumHeld) ignoreSwingUntilRelease = false;
        boolean fireLinear = linear.update(dt,left,right,up,down,propulsionPressed,propulsionHeld,facing);
        boolean fireSwing = swing.update(dt,left,right,up,down,
            !ignoreSwingUntilRelease && pendulumPressed,!ignoreSwingUntilRelease && pendulumHeld,facing);
        if (fireLinear) {
            aimX = linear.getAimX(); aimY = linear.getAimY();
            ignoreSwingUntilRelease = pendulumHeld;
            swing.reset();
            return Command.PROPULSION;
        }
        if (fireSwing) {
            aimX = swing.getAimX(); aimY = swing.getAimY();
            return Command.PENDULUM;
        }
        aimX = (right ? 1 : 0) - (left ? 1 : 0);
        aimY = (up ? 1 : 0) - (down ? 1 : 0);
        if (aimX == 0 && aimY == 0) aimX = facing;
        return Command.NONE;
    }
}
