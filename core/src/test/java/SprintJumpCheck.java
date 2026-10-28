import io.github.some_example_name.PlayerBody;
public class SprintJumpCheck {
    static void check(boolean value,String message) {if(!value)throw new AssertionError(message);}
    static PlayerBody body() {
        PlayerBody p=new PlayerBody();
        for(int i=-100;i<100;i++)p.addSolid(i*10,-2,10,2);
        p.reset(0,0);p.update(1f/120,0,false);return p;
    }
    public static void main(String[] args) {
        for(int fps:new int[]{30,60,144})for(int side:new int[]{-1,1}) {
            PlayerBody p=body();float dt=1f/fps;
            p.update(dt,side,false,true,true);
            for(int i=0;i<fps;i++)p.update(dt,side,false,false,true);
            check(p.isBoosted(),"Sprint remains active");
            for(int jump=0;jump<4;jump++) {
                float beforeJump=p.getVelocityX();
                p.update(dt,side,true,false,true);
                check(p.y>0&&p.getVelocityY()>0,"Jump during held sprint at "+fps+"fps");
                check(Math.abs(p.getVelocityX())>=16.099f && Math.abs(p.getVelocityX()-beforeJump)<.1f,"Jump preserves sprint and surplus momentum");
                boolean landed=false;
                for(int i=0;i<fps*3;i++) {
                    p.update(dt,side,false,false,true);
                    if(p.y==0){landed=true;break;}
                }
                check(landed&&p.isBoosted(),"Landing retains sprint");
            }
        }
        for(int fps:new int[]{30,60,144}) {
            PlayerBody p=body();float dt=1f/fps;
            p.update(dt,1,false,true,true);
            for(int i=0;i<fps;i++)p.update(dt,1,false,false,true);
            p.update(dt,1,true,false,true);
            int timeout=0;
            while((p.getVelocityY()>=0||p.y>.5f)&&timeout++<fps*2)p.update(dt,1,false,false,true);
            check(p.y>0&&p.getVelocityY()<0,"Approach ground before buffered jump");
            p.update(dt,1,true,false,true);
            boolean jumped=false;
            for(int i=0;i<fps;i++) {
                p.update(dt,1,false,false,true);
                if(p.getVelocityY()>0){jumped=true;break;}
            }
            check(jumped&&p.isBoosted(),"Jump pressed before landing executes without losing sprint");
        }
        PlayerBody airborne=body();airborne.update(1f/60,0,true,false,false);
        float vy=airborne.getVelocityY();
        airborne.update(1f/60,0,true,false,false);
        check(airborne.getVelocityY()<vy,"Buffer does not grant double jump");
        System.out.println("PASS: repeated sprint jumps, buffered landing, no double jump, both directions, 30/60/144 fps");
    }
}
