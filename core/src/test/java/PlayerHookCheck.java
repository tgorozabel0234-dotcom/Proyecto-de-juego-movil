import io.github.some_example_name.PlayerBody;

public class PlayerHookCheck {
    static void check(boolean value, String name) { if (!value) throw new AssertionError(name); }
    static PlayerBody room() {
        PlayerBody p = new PlayerBody();
        p.addSolid(-11,-11,1,22); p.addSolid(10,-11,1,22);
        p.addSolid(-11,-11,22,1); p.addSolid(-11,10,22,1);
        p.reset(-.25f,-.5f);
        return p;
    }
    static PlayerBody swing() {
        PlayerBody p = new PlayerBody();
        p.addSolid(-30,10,60,1);
        p.reset(0,0);
        check(p.launchHook(1,1), "Swing anchor");
        return p;
    }
    static float speed(PlayerBody p) { return (float)Math.hypot(p.getVelocityX(),p.getVelocityY()); }
    public static void main(String[] args) {
        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}};
        for (int[] aim : directions) {
            PlayerBody p = room();
            check(p.launchHook(aim[0], aim[1]), "8 direction hit");
            if (aim[0]!=0) check(Math.abs(p.getAnchorX()-aim[0]*10)<.001, "Horizontal aim");
            if (aim[1]!=0) check(Math.abs(p.getAnchorY()-aim[1]*10)<.001, "Vertical aim");
        }
        PlayerBody empty = new PlayerBody(); empty.reset(0,0);
        check(!empty.launchHook(0,1) && !empty.isHooked(), "No air anchors");
        empty.addSolid(-5,PlayerBody.HOOK_RANGE + 10,10,1);
        check(!empty.launchHook(0,1), "Limited range");
        PlayerBody near = room(); near.addSolid(2,-2,.1f,4);
        check(near.launchHook(1,0) && Math.abs(near.getAnchorX()-2)<.001, "First surface blocks ray");

        PlayerBody neutral = swing(), brake = swing();
        boolean checkedBrake = false;
        for(int i=0;i<800;i++) {
            float vx = neutral.getVelocityX();
            if (!checkedBrake && i>60 && Math.abs(vx)>2 && neutral.isHooked()) {
                neutral.update(1f/240,0,false,false,false);
                brake.update(1f/240,-Math.signum(vx),false,false,false);
                check(speed(brake) < speed(neutral)+.001, "Opposing input brakes swing");
                checkedBrake = true;
                break;
            }
            neutral.update(1f/240,0,false,false,false);
            brake.update(1f/240,0,false,false,false);
            check(neutral.isHooked(), "Anchor maintained during swing");
            float distance = (float)Math.hypot(neutral.x+.25f-neutral.getAnchorX(),neutral.y+.5f-neutral.getAnchorY());
            check(distance<=neutral.getRopeLength()+.151f, "Rope constraint");
        }
        check(checkedBrake, "Reached moving pendulum");
        float vx = neutral.getVelocityX(), vy = neutral.getVelocityY();
        neutral.releaseHook();
        check(!neutral.isHooked() && vx==neutral.getVelocityX() && vy==neutral.getVelocityY(), "Release keeps momentum");
        neutral.update(1f/120,0,false,false,false);
        float expectedVx=Math.signum(vx)*Math.max(0,Math.abs(vx)-16f/120);
        check(Math.abs(neutral.getVelocityX()-expectedVx)<.001, "Free flight smoothly brakes with no directional input");
        PlayerBody limit = swing();
        for (int i=0;i<5000;i++) {
            limit.update(1f/240,Math.signum(limit.getVelocityX()),false,false,false);
            check(speed(limit)<=36.001f, "Speed bounded");
        }
        PlayerBody falling = new PlayerBody();
        falling.addSolid(-40,15,80,1);
        falling.reset(-10,8);
        for (int i=0;i<120;i++) falling.update(1f/120,0,false);
        check(falling.getVelocityY() < -20 && falling.launchHook(1,1), "Hook during fast fall");
        boolean roseAgain = false;
        for (int i=0;i<720;i++) {
            falling.update(1f/240,1,false,false,false);
            if (falling.isHooked() && falling.getVelocityY()>2) roseAgain = true;
        }
        check(roseAgain, "Falling swing produces upward motion");
        PlayerBody blocked = swing();
        blocked.addSolid(3,2,1,5);
        blocked.update(1f/240,0,false,false,false);
        check(blocked.isHooked() && blocked.getRopePointCount()==1, "Rope crosses obstacle without collision or extra pivots");
        near.reset(0,0); check(!near.isHooked(), "Reset releases anchor");
        System.out.println("PASS: 8 directions, first hit, range, swing, braking, release and speed limit");
    }
}
