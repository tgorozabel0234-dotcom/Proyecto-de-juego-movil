/** Ejecutar con gradlew :core:physicsCheck. No requiere ventana ni dependencias de pruebas. */
public class PlayerPhysicsCheck {
    public static void main(String[] args) {
        PlayerBodyCheck.main(args);
        PlayerHeldDashCheck.main(args);
        PlayerHookCheck.main(args);
        HookMapCheck.main(args);
        HookAimInputCheck.main(args);
        RopeStabilityCheck.main(args);
        HookAssistCheck.main(args);
        HookModesCheck.main(args);
        SprintJumpCheck.main(args);
        MovementPolishCheck.main(args);
        PendulumReelCheck.main(args);
        PendulumRadiusCheck.main(args);
        UpperSwingImpulseCheck.main(args);
    }
}
