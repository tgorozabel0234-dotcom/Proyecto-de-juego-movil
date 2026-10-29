import io.github.some_example_name.*;
import com.badlogic.gdx.utils.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class PlayerAnimationCheck {
    static void check(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
    static PlayerBody floor(){PlayerBody p=new PlayerBody();p.addSolid(-100,-2,200,2);p.reset(0,0);p.update(1f/60,0,false);return p;}
    static void state(PlayerAnimation a,PlayerAnimation.State s,String msg){check(a.getState()==s,msg+": "+a.getState());}
    public static void main(String[] args){
        PlayerBody p=floor();CombatSystem c=new CombatSystem();PlayerVitals life=new PlayerVitals();PlayerAnimation a=new PlayerAnimation();a.reset(10);
        a.update(.1f,p,c,life,0);state(a,PlayerAnimation.State.IDLE,"Reposo");
        p.update(.1f,1,false);a.update(.1f,p,c,life,0);state(a,PlayerAnimation.State.WALK,"Caminar");
        p.update(.01f,1,false,true,true,true);a.update(.01f,p,c,life,0);state(a,PlayerAnimation.State.DASH,"Dash");
        p.update(.2f,1,false,false,true,true);a.update(.2f,p,c,life,0);state(a,PlayerAnimation.State.RUN,"Sprint");
        p=floor();a.reset(10);p.update(.01f,0,true);a.update(.01f,p,c,life,0);state(a,PlayerAnimation.State.JUMP,"Salto");
        for(int i=0;i<150&&p.getVelocityY()>0;i++)p.update(1f/240,0,false);
        a.update(.1f,p,c,life,0);state(a,PlayerAnimation.State.FALL,"Caida");
        a.update(.1f,p,c,life,0);
        for(int i=0;i<200&&!p.isGrounded();i++)p.update(1f/240,0,false);
        a.update(.01f,p,c,life,0);state(a,PlayerAnimation.State.LAND,"Aterrizaje");
        a.update(.2f,p,c,life,0);state(a,PlayerAnimation.State.IDLE,"Reposo tras aterrizaje");
        a.update(.01f,p,c,life,.22f);state(a,PlayerAnimation.State.HOOK_FIRE,"Disparo gancho");
        a.update(.2f,p,c,life,0);p.addSolid(-100,12,200,1);check(p.launchPendulum(0,1),"Anclaje");
        a.update(.01f,p,c,life,0);state(a,PlayerAnimation.State.PENDULUM,"Balanceo");
        p.releaseHook();a.update(.01f,p,c,life,0);state(a,PlayerAnimation.State.RELEASE,"Soltar cuerda");
        p.addSolid(20,0,1,30);check(p.launchPropulsion(1,0),"Propulsion");
        a.update(.2f,p,c,life,0);state(a,PlayerAnimation.State.PROPULSION,"Viaje lineal");
        CombatSystem.Attack[] attacks={CombatSystem.Attack.SWEEP,CombatSystem.Attack.UP,CombatSystem.Attack.DOWN,CombatSystem.Attack.THRUST,CombatSystem.Attack.AURA,CombatSystem.Attack.WHIRLWIND};
        PlayerAnimation.State[] states={PlayerAnimation.State.ATTACK_SIDE,PlayerAnimation.State.ATTACK_UP,PlayerAnimation.State.ATTACK_DOWN,PlayerAnimation.State.THRUST,PlayerAnimation.State.AURA,PlayerAnimation.State.WHIRLWIND};
        float x=p.x,y=p.y,vx=p.getVelocityX(),vy=p.getVelocityY();
        for(int i=0;i<attacks.length;i++){
            c.attack=attacks[i];c.elapsed=.15f;a.update(.02f,p,c,life,0);state(a,states[i],"Ataque "+attacks[i]);
            check(p.x==x&&p.y==y&&p.getVelocityX()==vx&&p.getVelocityY()==vy&&life.getHealth()==10,"Animacion no altera mecanicas");
        }
        p.update(0,-1,false);c.attack=CombatSystem.Attack.SWEEP;c.attackFacing=1;
        a.update(.01f,p,c,life,0);check(a.getFacing()==1,"Dibujo de ataque coincide con hitbox aunque cambie la direccion de movimiento");
        life.receiveDamage(1);a.update(.01f,p,c,life,0);state(a,PlayerAnimation.State.HURT,"Reaccion al dano");
        a.update(.2f,p,c,life,0);check(a.getState()!=PlayerAnimation.State.HURT,"No repite flinch por invulnerabilidad");
        life.update(.81f);life.receiveDamage(100);a.update(.01f,p,c,life,0);state(a,PlayerAnimation.State.DEATH,"Muerte prioritaria");
        a.update(.25f,p,c,life,0);a.update(.25f,p,c,life,0);check(a.getFrame()==11,"Muerte termina en pose caida");
        a.update(.25f,p,c,life,0);check(a.getFrame()==11,"Muerte no se repite");
        a.reset(10);check(a.getTime()==0&&a.getState()==PlayerAnimation.State.IDLE,"R reinicia selector");
        for(PlayerAnimation.State s:PlayerAnimation.State.values())for(float phase:new float[]{0,.1f,.5f,.99f,1}){
            int sheet=PlayerAnimation.sheet(s),frame=PlayerAnimation.frame(s,phase,phase);
            check(sheet>=0&&sheet<3&&frame>=0&&frame<16,"Todos los estados tienen una region valida");
        }
        check(PlayerAnimation.frame(PlayerAnimation.State.WALK,0,0)!=PlayerAnimation.frame(PlayerAnimation.State.WALK,.15f,0),"Ciclo de andar avanza fotogramas");
        File root=new File("assets/players/cyber_runner_v1");if(!root.isDirectory())root=new File("../assets/players/cyber_runner_v1");
        JsonValue data=new JsonReader().parse(new com.badlogic.gdx.files.FileHandle(new File(root,"animations.json")));
        int total=0;
        for(JsonValue sheet=data.get("sheets").child;sheet!=null;sheet=sheet.next){
            try{
                BufferedImage image=ImageIO.read(new File(root,sheet.getString("file")));
                check(image.getColorModel().hasAlpha(),"Hojas con alfa real");check(sheet.get("frames").size==16,"16 poses por hoja");
                for(JsonValue f=sheet.get("frames").child;f!=null;f=f.next){
                    int fx=f.getInt("x"),fy=f.getInt("y"),w=f.getInt("width"),h=f.getInt("height");
                    check(fx>=0&&fy>=0&&fx+w<=image.getWidth()&&fy+h<=image.getHeight(),"Region dentro de imagen");
                    check(w>20&&h>20&&f.getFloat("pixelsPerUnit")>0,"Pose con dimensiones validas");total++;
                    JsonValue exclusions=f.get("exclude");
                    for(JsonValue e=exclusions==null?null:exclusions.child;e!=null;e=e.next)
                        check(e.getInt("x")>=0&&e.getInt("y")>=0&&e.getInt("width")>0&&e.getInt("height")>0
                            &&e.getInt("x")+e.getInt("width")<=w&&e.getInt("y")+e.getInt("height")<=h,"Exclusion vecina dentro de la region");
                }
            }catch(java.io.IOException e){throw new AssertionError(e);}
        }
        check(total==48,"48 regiones de animacion disponibles");
        System.out.println("PASS: 19 estados visuales, movimiento, ganchos, ataques, dano, muerte, reset, 48 regiones alfa y fisica intacta");
    }
}
