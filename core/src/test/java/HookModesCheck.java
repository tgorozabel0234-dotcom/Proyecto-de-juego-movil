import io.github.some_example_name.HookAimInput;
import io.github.some_example_name.HookControls;
import io.github.some_example_name.PlayerBody;

public class HookModesCheck {
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    static PlayerBody body() {
        PlayerBody p=new PlayerBody();
        p.addSolid(-11,-11,1,22);p.addSolid(10,-11,1,22);
        p.addSolid(-11,10,22,1);p.addSolid(-11,-11,22,1);
        p.reset(-.25f,-.5f);return p;
    }
    public static void main(String[] args) {
        for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++) {
            if(x==0&&y==0)continue;
            PlayerBody p=body();
            check(p.launchPendulum(x,y)&&p.isHooked()&&!p.isPropelling(),"U attaches only pendulum");
            check(p.getVelocityX()==0&&p.getVelocityY()==0,"U does not inject propulsion");
            check(p.launchPropulsion(x,y)&&p.isPropelling()&&!p.isHooked(),"I replaces pendulum");
            check(p.getVelocityX()==0&&p.getVelocityY()==0,"Propulsion preserves initial velocity at launch");
            p.releaseHook();
            check(p.isPropelling(),"Releasing U does not cancel I");
            float beforeX=p.x,beforeY=p.y;
            p.update(1f/60,0,false,false,false);
            check(Math.hypot(p.getVelocityX(),p.getVelocityY())>0&&Math.hypot(p.getVelocityX(),p.getVelocityY())<28,"Propulsion accelerates progressively");
            check((x==0||Math.signum(p.x-beforeX)==x)&&(y==0||Math.signum(p.y-beforeY)==y),"Linear direction");
            if(x!=0&&y!=0) check(Math.abs(Math.abs(p.x-beforeX)-Math.abs(p.y-beforeY))<.001,"Diagonal propulsion stays straight");
            check(p.launchPendulum(x,y)&&!p.isPropelling()&&p.isHooked(),"U replaces propulsion");
            float vx=p.getVelocityX(),vy=p.getVelocityY();
            p.releaseHook();
            check(!p.isHooked()&&vx==p.getVelocityX()&&vy==p.getVelocityY(),"U release conserves momentum");
            p.reset(0,0);check(!p.isHooked()&&!p.isPropelling(),"Reset clears both modes");
        }
        HookControls h=new HookControls();
        check(h.update(.016f,true,false,true,false,true,true,true,true,1)==HookControls.Command.PROPULSION,"I priority if both pressed");
        for(int i=0;i<60;i++) check(h.update(.016f,true,false,true,false,false,true,false,true,1)==HookControls.Command.NONE,"No auto-repeat or unintended mode switch");
        h.update(.016f,true,false,true,false,false,false,false,false,1);
        check(h.update(.016f,true,false,true,false,false,false,true,true,1)==HookControls.Command.PENDULUM,"U re-arms after release");
        PlayerBody empty=new PlayerBody();empty.reset(0,0);
        check(!empty.launchPropulsion(1,1)&&!empty.launchPendulum(1,1),"Both modes require real surface");

        int total=0;
        int[][] orders={{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
        for(int fps:new int[]{30,60,144})for(int side:new int[]{-1,1})for(int vertical:new int[]{-1,1})
        for(boolean linear:new boolean[]{false,true})for(int[] order:orders) {
            HookControls controls=new HookControls();PlayerBody p=body();
            boolean[] keys=new boolean[3];boolean launched=false;
            for(int frame=0;frame<12;frame++) {
                boolean pressed=false;
                if(frame<3){keys[order[frame]]=true;pressed=order[frame]==2;}
                HookControls.Command command=controls.update(1f/fps,keys[0]&&side<0,keys[0]&&side>0,
                    keys[1]&&vertical>0,keys[1]&&vertical<0,linear&&pressed,linear&&keys[2],
                    !linear&&pressed,!linear&&keys[2],1);
                if(command!=HookControls.Command.NONE){
                    check(!launched,"One command per press");launched=true;
                    check(controls.getAimX()==side&&controls.getAimY()==vertical,"Both mode buffers preserve diagonal");
                    check(command==(linear?HookControls.Command.PROPULSION:HookControls.Command.PENDULUM),"Mode isolation");
                    check(linear?p.launchPropulsion(controls.getAimX(),controls.getAimY()):p.launchPendulum(controls.getAimX(),controls.getAimY()),"Surface reached");
                }
                p.update(1f/fps,keys[0]?side:0,false,false,false);
            }
            check(launched,"Ordered command detected");total++;
        }
        PlayerBody wall=body();wall.launchPropulsion(1,0);
        for(int i=0;i<240;i++)wall.update(1f/120,0,false,false,false);
        check(!wall.isPropelling()&&wall.x+PlayerBody.WIDTH<=10.001,"Propulsion stops without crossing wall");
        System.out.println("PASS: U propulsion / I pendulum, 8 directions, release, switching, collision and "+total+" ordered mode sequences");
    }
}
