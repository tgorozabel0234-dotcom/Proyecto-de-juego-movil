import io.github.some_example_name.PlayerBody;
public class PendulumReelCheck {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    static PlayerBody body(float ceiling) {
        PlayerBody p=new PlayerBody();p.addSolid(-100,ceiling,200,1);p.reset(0,0);return p;
    }
    public static void main(String[] args) {
        for(int fps:new int[]{30,60,144,240}) {
            PlayerBody p=body(30);check(p.launchPendulum(0,1),"Distant anchor reached");
            float start=p.getRopeLength(),ax=p.getAnchorX(),ay=p.getAnchorY();
            check(start>20&&p.getRopeTargetLength()==15&&p.isReeling(),"Automatic close-range target");
            float previous=start;
            for(int i=0;i<fps*2;i++) {
                float oldY=p.y;
                p.update(1f/fps,0,false,false,false);
                check(p.isHooked()&&p.getAnchorX()==ax&&p.getAnchorY()==ay,"Anchor stays fixed while reeling");
                check(p.getRopePointCount()==1,"Rope remains non-colliding straight segment");
                check(p.getRopeLength()<=previous+.001&&p.getRopeLength()>=14.999,"Monotonic bounded reeling");
                check(Math.abs(p.y-oldY)<1.2f,"No teleport during approach");
                previous=p.getRopeLength();
            }
            check(!p.isReeling()&&Math.abs(p.getRopeLength()-15)<.001,"Short radius reached");
            check(p.y>14,"Player brought close to anchor");
            p.update(1f/fps,1,false,false,false);
            check(p.getVelocityX()>0,"Swing controllable after approach");
            float vx=p.getVelocityX(),vy=p.getVelocityY();
            p.releaseHook();check(vx==p.getVelocityX()&&vy==p.getVelocityY(),"Release preserves momentum");
        }
        PlayerBody close=body(4);close.launchPendulum(0,1);
        check(!close.isReeling()&&close.getRopeTargetLength()==close.getRopeLength(),"Short existing rope is not lengthened");
        PlayerBody obstacle=body(30);obstacle.addSolid(-5,10,10,1);obstacle.reset(0,12);
        check(obstacle.launchPendulum(0,1),"Anchor above obstacle");
        for(int i=0;i<480;i++) {
            obstacle.update(1f/240,0,false);
            check(obstacle.y>=11,"Player still respects solid platforms");
        }
        System.out.println("PASS: smooth approach to radius 15, 30/60/144/240 fps, fixed anchor, no rope collision and release");
    }
}
