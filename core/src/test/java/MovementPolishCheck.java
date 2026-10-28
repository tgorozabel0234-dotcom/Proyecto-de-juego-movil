import io.github.some_example_name.PlayerBody;

public class MovementPolishCheck {
    static void check(boolean ok,String msg) {if(!ok)throw new AssertionError(msg);}
    static final float DT=1f/240;
    static PlayerBody floor() {
        PlayerBody p=new PlayerBody();p.addSolid(-1000,-2,2000,2);p.reset(0,0);p.update(DT,0,false);return p;
    }
    static float[] run(int fps) {
        PlayerBody p=floor();
        for(int i=0;i<fps*3;i++) {
            boolean jump=i==fps/2;
            p.update(1f/fps,1,jump,i==0,true,i>=fps/2&&i<fps*3/4);
        }
        return new float[]{p.x,p.y,p.getVelocityX(),p.getVelocityY()};
    }
    static float apex(float holdTime) {
        PlayerBody p=floor();float maximum=0;
        for(int i=0;i<480;i++) {
            p.update(DT,0,i==0,false,false,i*DT<holdTime);
            maximum=Math.max(maximum,p.y);
        }
        return maximum;
    }
    static PlayerBody leaveEdge(float delay) {
        PlayerBody p=new PlayerBody();p.addSolid(0,-2,2,2);p.reset(1.5f,0);p.update(DT,0,false);
        int guard=0;
        while(p.isGrounded()&&guard++<240)p.update(DT,1,false);
        check(!p.isGrounded(),"Leaves platform edge");
        for(int i=0;i<Math.round(delay/DT);i++)p.update(DT,1,false);
        return p;
    }
    public static void main(String[] args) {
        float[] baseline=run(240);
        for(int fps:new int[]{30,60,144,360}) {
            float[] p=run(fps);
            for(int i=0;i<4;i++)check(Math.abs(p[i]-baseline[i])<.01,"Fixed step at "+fps+"fps component "+i);
        }
        PlayerBody tiny=floor();tiny.update(1f/1000,1,true,true,true,true);
        for(int i=0;i<6;i++)tiny.update(1f/1000,1,false,false,true,true);
        check(tiny.isDashing()&&tiny.y>0,"Inputs survive render frames without a physics tick");
        check(apex(.25f)>apex(.015f)*1.5f,"Held jump is higher than a tap");
        PlayerBody early=leaveEdge(.04f);early.update(DT,1,true);
        check(early.getVelocityY()>0,"Coyote jump within 100 ms");
        PlayerBody late=leaveEdge(.15f);late.update(DT,1,true);
        check(late.getVelocityY()<0,"No air jump beyond coyote window");

        PlayerBody landing=floor();landing.reset(0,10);
        check(landing.launchPropulsion(1,-.4f),"Downward propulsion to floor");
        int guard=0;
        while(!landing.isGrounded()&&guard++<720)landing.update(DT,1,false,false,false,true);
        check(landing.isGrounded(),"Propulsion lands");
        float before=landing.getVelocityX();
        check(before>16,"Landing keeps hook horizontal momentum");
        landing.update(1f/30,1,false,false,false,true);
        check(landing.getVelocityX()>16&&landing.getVelocityX()<=before+.001,"Progressive landing friction");
        for(int i=0;i<960;i++)landing.update(DT,1,false);
        check(Math.abs(landing.getVelocityX()-7)<.01,"Momentum settles to walking speed");

        PlayerBody launch=floor();launch.addSolid(-50,20,100,1);
        check(launch.launchPropulsion(1,1),"Start smooth propulsion");
        check(Math.abs(launch.getVelocityX())<.01,"No instant velocity replacement");
        launch.update(1f/60,0,false,false,false,true);
        float speed=(float)Math.hypot(launch.getVelocityX(),launch.getVelocityY());
        check(speed>0&&speed<28,"Brief propulsion acceleration");
        launch.update(DT,1,false,true,true,true);
        check(!launch.isPropelling()&&launch.isDashing(),"Dash interrupts propulsion");
        PlayerBody cancel=floor();cancel.addSolid(-50,20,100,1);cancel.launchPropulsion(0,1);
        cancel.update(DT,0,true,false,false,true);
        check(!cancel.isPropelling()&&cancel.getVelocityY()>0,"Jump cancels ground propulsion and jumps");

        PlayerBody slide=floor();slide.addSolid(4,0,1,30);slide.launchPropulsion(1,1);
        boolean collided=false;
        for(int i=0;i<360;i++) {
            slide.update(DT,0,false);
            if(slide.x+PlayerBody.WIDTH>=3.999f&&slide.getVelocityX()==0) {
                check(slide.getVelocityY()>2,"Wall contact preserves vertical slide");collided=true;break;
            }
        }
        check(collided,"Reached wall for tangential contact test");
        System.out.println("PASS: fixed step, latched inputs, variable jump, coyote, landing momentum, smooth propulsion, cancellations and sliding");
    }
}
