import io.github.some_example_name.*;

public class VitalsHudCheck {
    static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
    static void close(float value,float expected,String msg){check(Math.abs(value-expected)<.001f,msg);}
    public static void main(String[] args){
        PlayerVitals life=new PlayerVitals();
        check(life.maxHealth==10&&life.cellCount==10,"10 puntos y 10 celdas por defecto");
        for(int i=0;i<10;i++)close(life.getCellFill(i),1,"Diez celdas completas al iniciar");
        check(life.receiveDamage(1),"Golpe quita un punto");
        close(life.getHealth(),9,"Quedan nueve puntos");close(life.getCellFill(9),0,"Una celda por punto perdido");
        check(!life.receiveDamage(1),"Proteccion impide golpes consecutivos por cuadro");
        for(int hit=2;hit<=10;hit++){
            life.update(.81f);life.receiveDamage(1);
            close(life.getHealth(),10-hit,"Cada golpe quita exactamente un punto");
            float sum=0;
            for(int i=0;i<10;i++){
                float fill=life.getCellFill(i);sum+=fill;
                check(fill==0||fill==1,"Dano normal vacia celdas completas");
            }
            close(sum,life.getHealth(),"Una celda corresponde a un punto");
        }
        check(!life.alive(),"Muere tras perder los diez puntos");
        close(life.getCellFill(-1),0,"Indice inferior invalido");close(life.getCellFill(10),0,"Indice superior invalido");
        life.reset();close(life.getHealth(),10,"R restaura diez puntos");
        for(int i=0;i<10;i++)close(life.getCellFill(i),1,"R rellena cada celda");
        for(int fps:new int[]{30,60,144,240,360}){
            PlayerBody p=new PlayerBody();p.addSolid(-100,-2,200,2);p.reset(0,0);p.update(.02f,1,false,true,true,true);
            check(p.getDashCooldownRemaining()>.5f&&p.getDashCooldownRemaining()<.6f,"Tiempo real de recarga disponible para HUD");
            float last=p.getDashCooldownRemaining();
            for(int i=0;i<fps;i++){
                p.update(1f/fps,1,false,false,true,true);
                float remaining=p.getDashCooldownRemaining();
                check(remaining<=last+.0001f,"Sombra disminuye con el tiempo");
                close(remaining/.6f,1-p.getDashRecoveryFraction(),"Sombra representa exactamente el tiempo restante");last=remaining;
            }
            close(last,0,"No queda sombra al terminar");check(p.canDash(),"Dash listo coincide con sombra retirada");
        }
        System.out.println("PASS: 10 celdas, 1 punto por celda y golpe, muerte, reinicio y sombra de recarga sincronizada a varios FPS");
    }
}
