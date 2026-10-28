import io.github.some_example_name.PlayerBody;
public class PendulumRadiusCheck {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args) {
        int aboveSamples=0;
        for(int fps:new int[]{30,60,144,240})for(int side:new int[]{-1,0,1}) {
            PlayerBody p=new PlayerBody();p.addSolid(-.2f,-.2f,.4f,.4f);
            p.reset(side*5f-.25f,20-.5f);
            check(p.launchPendulum(-side*5f,-20),"Anchor below player reached");
            float ax=p.getAnchorX(),ay=p.getAnchorY();boolean locked=false;
            for(int i=0;i<fps*5;i++) {
                p.update(1f/fps,side,false,false,false);
                check(p.isHooked()&&p.getAnchorX()==ax&&p.getAnchorY()==ay,"Anchor stays fixed");
                if(!p.isReeling()) {
                    locked=true;
                    check(Math.abs(p.getRopeLength()-15)<.001,"Radius is 15");
                    check(p.getRopeUsedLength()<=15.021,"Rope keeps maximum radius without forcing minimum distance");
                    if(p.y+PlayerBody.HEIGHT/2>ay)aboveSamples++;

                }
            }
            check(locked,"Reeling completes");
            p.releaseHook();check(!p.isHooked(),"Manual release remains available");
        }
        check(aboveSamples>100,"Test actually exercises upper half of rotation");
        System.out.println("PASS: maximum radius 15 with slack above anchor, both sides, 30/60/144/240 fps; upper samples="+aboveSamples);
    }
}
