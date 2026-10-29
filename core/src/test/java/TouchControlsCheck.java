import io.github.some_example_name.*;
import io.github.some_example_name.TouchControls.Action;

public class TouchControlsCheck {
    static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
    public static void main(String[] args){
        TouchControls t=new TouchControls();
        check(t.beginStick(0,1,0),"Primer dedo toma joystick");
        check(t.press(Action.JUMP,1)&&t.press(Action.ATTACK,2),"Tres dedos simultaneos");
        check(!t.press(Action.DASH,1),"Un dedo no toma dos acciones");
        t.release(1);check(t.held(Action.ATTACK)&&t.moveX()==1,"Soltar salto no suelta ataque ni joystick");
        check(t.pressed(Action.JUMP)&&!t.held(Action.JUMP),"Toque corto se conserva");
        t.endFrame();check(!t.pressed(Action.JUMP)&&t.held(Action.ATTACK),"Pulsacion no se repite cada cuadro");
        t.release(2);t.press(Action.DASH,3);t.release(3);t.endFrame();
        check(t.held(Action.DASH),"Sprint queda activo con joystick para liberar pulgar");
        t.dragStick(0,0,0);check(!t.held(Action.DASH)&&t.moveX()==0,"Centrar joystick apaga sprint");
        t.dragStick(0,.05f,.03f);check(t.aimX()==0&&t.aimY()==0,"Zona muerta sin deriva");
        t.dragStick(0,.05f,1);check(t.aimX()==0&&t.aimY()==1&&t.moveX()==0,"Apuntado vertical no mueve lateralmente");
        t.reset();t.press(Action.PENDULUM,1);t.release(1);t.endFrame();
        check(t.held(Action.PENDULUM),"Pendulo tactil se mantiene al soltar boton");
        t.press(Action.ATTACK,1);check(t.held(Action.PENDULUM)&&t.held(Action.ATTACK),"Un pulgar puede atacar durante pendulo");
        t.release(1);t.syncSwing(true,true);t.syncSwing(false,false);
        check(!t.held(Action.PENDULUM),"Cancelar enganche limpia latch");
        t.press(Action.PENDULUM,1);t.release(1);t.endFrame();t.press(Action.PENDULUM,1);
        check(!t.held(Action.PENDULUM)&&!t.pressed(Action.PENDULUM),"Segundo toque suelta sin nuevo disparo");
        t.reset();t.press(Action.PENDULUM,1);t.syncSwing(false,true);
        check(!t.held(Action.PENDULUM),"Fallo de gancho no deja toggle atascado");
        for(int fps:new int[]{30,60,144,240,360})for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++){
            if(dx==0&&dy==0)continue;
            t.reset();HookControls h=new HookControls();
            t.beginStick(0,dx,dy);t.press(Action.PROPULSION,1);t.release(1);t.release(0);
            HookControls.Command result=h.update(1f/fps,t.aimX()<0,t.aimX()>0,t.aimY()>0,t.aimY()<0,
                t.pressed(Action.PROPULSION),t.held(Action.PROPULSION),false,false,1);
            check(result==HookControls.Command.PROPULSION&&h.getAimX()==dx&&h.getAimY()==dy,"Tap breve preserva apuntado al soltar ambos dedos a "+fps);
            t.endFrame();check(!t.pressed(Action.PROPULSION)&&t.aimX()==0&&t.aimY()==0,"No repite disparo");
        }
        t.reset();check(t.beginStick(0,-1,0),"Joystick funciona tras reinicio");
        t.press(Action.JUMP,1);t.press(Action.PENDULUM,2);t.reset();
        for(Action a:Action.values())check(!t.held(a)&&!t.pressed(a),"Pausa/resize limpia todas las acciones");
        check(t.moveX()==0&&t.beginStick(0,1,0),"Se liberan propietarios de dedos");
        for(float w:new float[]{450,800,1066,1600})for(float h:new float[]{450,800}) {
            HudLayout layout=new HudLayout(w+40,h+20,20,20,20,18,144);
            for(int i=0;i<layout.actions.length;i++) {
                HudLayout.Box b=layout.actions[i];
                check(b.x>=20&&b.y>=20&&b.x+b.w<=w+20&&b.y+b.h<=h+20,"Botones dentro de area segura");
                check(!layout.stick.overlaps(b),"Botones no se superponen al joystick");
                if(h>w)check(!b.overlaps(new HudLayout.Box((w+40)/2-16,20,32,240)),"En vertical queda libre el centro bajo el jugador");
                for(int j=i+1;j<layout.actions.length;j++)check(!b.overlaps(layout.actions[j]),"Botones separados");
                check(b.w>=56,"Areas tactiles amplias");
            }
        }
        System.out.println("PASS: multitactil, pulsaciones breves, 8 direcciones, FPS, sprint/pendulo, cancelaciones y layout seguro");
    }
}
