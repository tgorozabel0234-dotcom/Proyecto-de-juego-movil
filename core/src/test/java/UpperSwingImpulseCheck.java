import io.github.some_example_name.PlayerBody;
public class UpperSwingImpulseCheck {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static PlayerBody above(float side) {
        PlayerBody p=new PlayerBody();p.addSolid(-.2f,-.2f,.4f,.4f);
        p.reset(side*5-.25f,11.5f);
        check(p.launchPendulum(-side*5,-12),"Anchor below player");return p;
    }
    public static void main(String[] args) {
        PlayerBody still=above(0);float initial=still.getRopeUsedLength();
        for(int i=0;i<48;i++)still.update(1f/240,0,false);
        check(still.getRopeUsedLength()<initial-.3f,"Upper position is not forced onto rigid circle");
        check(still.getUpperSwingBoostCount()==0,"No artificial rotation while stationary");
        for(int fps:new int[]{30,60,144,240})for(int side:new int[]{-1,1}) {
            PlayerBody p=above(side);float ax=p.getAnchorX(),ay=p.getAnchorY();
            boolean boosted=false;
            for(int i=0;i<fps;i++) {
                int before=p.getUpperSwingBoostCount();
                p.update(1f/fps,side,false);
                if(p.getUpperSwingBoostCount()>before) {
                    check(p.y+.5f>ay,"Boost occurs above anchor");
                    float rx=p.x+.25f-ax,ry=p.y+.5f-ay;
                    float angular=rx*p.getVelocityY()-ry*p.getVelocityX();
                    check(Math.signum(angular)==-side,"Impulse follows existing rotation");
                    check(p.getVelocityX()*side>2,"Tangible directional impulse");
                    boosted=true;
                }
                if(p.y+.5f>ay+.2f)check(p.getUpperSwingBoostCount()<=1,"No repeated boost while continuously above");
                check(p.isHooked()&&p.getRopePointCount()==1,"Hook remains attached without rope collision");
            }
            check(boosted,"Upper pass receives boost");
            float vx=p.getVelocityX(),vy=p.getVelocityY();
            p.releaseHook();
            check(vx==p.getVelocityX()&&vy==p.getVelocityY(),"Release preserves boosted momentum");
        }
        PlayerBody rearm=above(1);
        for(int i=0;i<120;i++)rearm.update(1f/240,1,false);
        check(rearm.getUpperSwingBoostCount()==1,"First pass gets one impulse");
        rearm.y=-3;rearm.update(1f/240,0,false);
        rearm.x=4.75f;rearm.y=11.5f;
        for(int i=0;i<120&&rearm.getUpperSwingBoostCount()<2;i++)rearm.update(1f/240,1,false);
        check(rearm.getUpperSwingBoostCount()==2,"New upper pass re-arms after going below");
        System.out.println("PASS: upper tangential boost, both rotations, no rigid top constraint, one boost per pass and release");
    }
}
