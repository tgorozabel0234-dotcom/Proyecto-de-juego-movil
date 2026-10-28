import io.github.some_example_name.PlayerBody;

public class PlayerBodyCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        PlayerBody p = new PlayerBody();
        p.addSolid(-10, -2, 30, 2);
        p.addSolid(4, 0, 1, 10);
        p.reset(0, 3);
        for (int i = 0; i < 240; i++) p.update(1f / 120, 0, false);
        check(Math.abs(p.y) < 0.001, "Landing on floor");
        p.update(1f / 60, 0, true);
        check(p.y > 0, "Ground jump");
        for (int i = 0; i < 240; i++) p.update(1f / 120, 1, false);
        check(p.x <= 3.5001f, "Wall collision");
        check(Math.abs(p.y) < 0.001, "Return to floor");
        PlayerBody platform = new PlayerBody();
        platform.addSolid(-5, 2, 10, 0.5f);
        platform.reset(0, 6);
        for (int i = 0; i < 180; i++) platform.update(1f / 120, 0, false);
        check(Math.abs(platform.y - 2.5f) < 0.001, "Landing on platform");
        PlayerBody ceiling = new PlayerBody();
        ceiling.addSolid(-5, -1, 10, 1);
        ceiling.addSolid(-5, 1.5f, 10, 0.5f);
        ceiling.reset(0, 0);
        ceiling.update(1f / 60, 0, false);
        for (int i = 0; i < 30; i++) {
            ceiling.update(1f / 120, 0, i == 0);
            check(ceiling.y + PlayerBody.HEIGHT <= 1.5001f, "Platform underside");
        }
        System.out.println("PASS: floor, jump, walls, platform and underside collisions");
    }
}
