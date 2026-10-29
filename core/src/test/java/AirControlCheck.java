import io.github.some_example_name.PlayerBody;

public class AirControlCheck {
    static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    static PlayerBody launch(int side) {
        PlayerBody p=new PlayerBody();p.reset(0,100);p.update(.2f,side,false,true,true,true);
        check(!p.isDashing()&&Math.abs(p.getVelocityX()-22*side)<.01f,"Velocidad inicial tras dash");return p;
    }
    static void advance(PlayerBody p,int fps,float direction,float seconds) {
        for(int i=0;i<Math.round(seconds*fps);i++)p.update(1f/fps,direction,false,false,false,true);
    }
    public static void main(String[] args) {
        for(int fps:new int[]{30,60,144,240,360}) for(int side:new int[]{-1,1}) {
            PlayerBody braking=launch(side),neutral=launch(side),forward=launch(side);
            advance(braking,fps,-side,.5f);advance(neutral,fps,0,.5f);advance(forward,fps,side,.5f);
            check(braking.getVelocityX()*side<0,"Invierte direccion en menos de medio segundo a "+fps);
            check(Math.abs(Math.abs(neutral.getVelocityX())-14)<.02f,"Soltar desacelera 16 unidades/s2 a "+fps);
            check(Math.abs(forward.getVelocityX()-22*side)<.01f,"Mantener direccion conserva impulso");
            check(Math.abs(braking.getVelocityY()-neutral.getVelocityY())<.01f&&Math.abs(forward.getVelocityY()-neutral.getVelocityY())<.01f,"No altera caida ni gravedad");
        }
        PlayerBody p=launch(1);advance(p,240,0,3);
        check(p.getVelocityX()==0,"Freno neutral llega a cero sin invertir direccion");
        p=launch(-1);advance(p,240,0,3);check(p.getVelocityX()==0,"Igual al frenar hacia izquierda");
        p=launch(1);p.addSolid(-100,130,200,1);check(p.launchPendulum(0,1),"Engancha pendulo");
        float vx=p.getVelocityX(),vy=p.getVelocityY();p.releaseHook();
        check(p.getVelocityX()==vx&&p.getVelocityY()==vy,"Soltar pendulo conserva vector instantaneo");
        p.update(1f/240,0,false,false,false,true);
        check(p.getVelocityX()>vx-.1f&&p.getVelocityX()<vx,"Friccion posterior al soltar es suave");
        System.out.println("PASS: freno aereo 45, friccion neutral 16, ambos lados, FPS, inercia y velocidad vertical");
    }
}
