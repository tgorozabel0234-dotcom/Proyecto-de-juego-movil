import io.github.some_example_name.PlayerBody;
public class HookAssistCheck {
    static void check(boolean v,String s) { if(!v) throw new AssertionError(s); }
    static PlayerBody body() { PlayerBody p=new PlayerBody(); p.reset(-.25f,-.5f); return p; }
    public static void main(String[] args) {
        PlayerBody p=body(); p.addSolid(10,1,2,1);
        PlayerBody.HookTarget t=p.findHookTarget(1,0);
        check(t!=null && t.assisted,"Near miss assisted");
        check(Math.hypot(t.x-t.probeX,t.y-t.probeY)<=PlayerBody.HOOK_ASSIST_RADIUS+.001,"Circular margin");
        check(p.launchHook(1,0) && p.getAnchorY()>=1,"Anchor on real surface");
        p=body(); p.addSolid(10,1.4f,2,1);
        check(!p.launchHook(1,0),"Outside radius misses");
        p=body(); p.addSolid(35.8f,.8f,1,1);
        check(!p.launchHook(1,0),"Assist never exceeds range 35");
        p=body(); p.addSolid(10,1,2,1); p.addSolid(20,-1,1,2);
        t=p.findHookTarget(1,0);
        check(t!=null && !t.assisted && Math.abs(t.x-20)<.001,"Exact hit has priority");
        p=body(); p.addSolid(-20,-.5f,40,.1f);
        check(!p.launchHook(1,0),"No automatic nearby-floor attachment");
        p=body(); p.addSolid(10,1,2,1); p.addSolid(20,.9f,2,1);
        check(p.findHookTarget(1,0).x<13,"First assist candidate");
        for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++) {
            if(x==0 && y==0) continue;
            float n=(float)Math.hypot(x,y), dx=x/n,dy=y/n;
            p=body();
            p.addSolid(dx*10-dy*1.1f-.05f,dy*10+dx*1.1f-.05f,.1f,.1f);
            t=p.findHookTarget(x,y);
            check(t!=null && t.assisted,"Assist works in all 8 directions");
            check(p.launchHook(x,y),"Launch matches preview");
        }
        System.out.println("PASS: circular assist, 8 directions, real surface, radius/range limits and exact-hit priority");
    }
}
