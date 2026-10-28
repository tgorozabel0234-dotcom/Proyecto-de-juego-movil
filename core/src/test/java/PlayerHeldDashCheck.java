import io.github.some_example_name.PlayerBody;
public class PlayerHeldDashCheck {
    static void check(boolean v, String name) { if (!v) throw new AssertionError(name); }
    public static void main(String[] args) {
        PlayerBody p = new PlayerBody();
        p.addSolid(-100, -2, 200, 2);
        p.reset(0, 0);
        p.update(.1f, 1, false, true, true);
        p.update(.1f, 1, false, false, true);
        check(p.isBoosted() && !p.isDashing(), "Boost after dash");
        for (int i=0; i<300; i++) p.update(1f/120, 0, false, false, true);
        check(p.isBoosted() && p.canDash(), "Holding preserves boost without repeating dash");
        float x = p.x;
        for(int i=0;i<60;i++)p.update(1f/120,1,false,false,true);
        x=p.x;
        p.update(.1f, 1, false, false, true);
        check(Math.abs(p.x-x-1.61f)<.001, "Speed is 7 times 2.3");
        x = p.x;
        p.update(.1f, 1, false, false, false);
        check(!p.isBoosted() && p.getVelocityX()>=7 && p.getVelocityX()<16.1f, "Release brakes progressively");
        for(int i=0;i<60;i++)p.update(1f/120,1,false,false,false);
        x=p.x;p.update(.1f,1,false,false,false);
        check(Math.abs(p.x-x-.7f)<.001,"Normal speed settles after release");
        p.update(.1f, 1, false, false, true);
        check(!p.isBoosted(), "Holding alone does not start boost");
        p.update(.1f, -1, false, true, true);
        p.update(.1f, -1, false, false, true);
        check(p.isBoosted(), "Second dash works");
        p.reset(0,0);
        check(!p.isBoosted() && p.canDash(), "Reset");
        System.out.println("PASS: sustained x2.3 boost, release, no repeat, second dash and reset");
    }
}
