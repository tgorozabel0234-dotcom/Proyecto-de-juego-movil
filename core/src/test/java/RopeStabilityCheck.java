import io.github.some_example_name.PlayerBody;
import io.github.some_example_name.HookAimInput;

public class RopeStabilityCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static PlayerBody body() {
        PlayerBody p=new PlayerBody();
        p.addSolid(-50,-2,100,2); p.addSolid(-50,15,100,1);
        p.addSolid(-31,0,1,15); p.addSolid(30,0,1,15);
        return p;
    }
    public static void main(String[] args) {
        for(int fps : new int[]{30,60,144}) for(int side : new int[]{-1,1}) {
            PlayerBody p=body(); p.reset(0,0);
            p.update(1f/60,side,false,false,false);
            p.update(1f/60,side,true,true,true);
            check(p.isDashing() && p.y>0,"Jump while dash active");
            HookAimInput input=new HookAimInput();
            check(input.update(1f/60,side<0,side>0,true,false,true,true,side),"Diagonal V received during C/Z");
            check(p.launchHook(input.getAimX(),input.getAimY()),"Hook allowed during jump/dash");
            float ax=p.getAnchorX(),ay=p.getAnchorY();
            for(int i=0;i<fps*8;i++) {
                boolean dash=i%(fps)==0;
                p.update(1f/fps,side,false,dash,true);
                check(p.isHooked(),"Anchor survives speed/dash at "+fps+"fps");
                check(p.getAnchorX()==ax && p.getAnchorY()==ay,"Anchor stays fixed");
                check(Float.isFinite(p.x) && Float.isFinite(p.y),"Finite physics");
                check(p.getRopeUsedLength()<=p.getRopeLength()+.025f,"Rope length preserved");
            }
            p.releaseHook(); check(!p.isHooked(),"Manual release remains functional");
        }
        PlayerBody corner=body(); corner.addSolid(3,3,2,4); corner.reset(0,0);
        check(corner.launchHook(0,1),"Vertical anchor before passing obstacle");
        float anchorX=corner.getAnchorX(),anchorY=corner.getAnchorY();
        for(int i=0;i<1200;i++) {
            corner.update(1f/120,1,false,i%120==0,true);
            check(corner.isHooked(),"Obstacle never breaks hook");
            check(corner.getRopePointCount()==1,"Rope remains a straight segment through obstacles");
            check(corner.getAnchorX()==anchorX&&corner.getAnchorY()==anchorY,"Obstacle never changes anchor");
            float direct=(float)Math.hypot(corner.x+PlayerBody.WIDTH/2-anchorX,corner.y+PlayerBody.HEIGHT/2-anchorY);
            check(Math.abs(corner.getRopeUsedLength()-direct)<.001,"Rope uses direct distance without detour");
            boolean overlaps=corner.x<5&&corner.x+PlayerBody.WIDTH>3&&corner.y<7&&corner.y+PlayerBody.HEIGHT>3;
            check(!overlaps,"Player still collides with obstacle");
            check(corner.getRopeUsedLength()<=corner.getRopeLength()+.025f,"Straight rope length remains bounded");
        }

        System.out.println("PASS: jump+dash+hook, 30/60/144 fps, repeated dash, fixed anchor, non-colliding rope, player collisions and release");
    }
}
