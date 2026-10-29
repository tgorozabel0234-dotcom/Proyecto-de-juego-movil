import io.github.some_example_name.PlayerBody;
import io.github.some_example_name.CombatSystem;

public class GroundThrustCheck {
    static void check(boolean ok,String message) { if(!ok)throw new AssertionError(message); }
    static PlayerBody supported(float height) {
        PlayerBody p=new PlayerBody();p.addSolid(-100,height-2,200,2);p.reset(0,height);
        p.update(1f/60,0,false);return p;
    }
    public static void main(String[] args) {
        for(int fps:new int[]{30,60,144,240,360}) {
            float dt=1f/fps/(float)Math.ceil(240f/fps);
            for(float height:new float[]{0,5}) {
                PlayerBody p=supported(height);CombatSystem c=new CombatSystem();
                p.update(dt,1,false,true,true,true);c.update(dt,p,true,true,false,false);
                check(c.attack==CombatSystem.Attack.THRUST,"Estocada desde suelo/plataforma a "+fps);
                check(!p.hasAttackAirJump(),"Fallar no regala salto");
            }
            PlayerBody p=supported(0);p.reset(0,5);CombatSystem c=new CombatSystem();
            p.update(dt,1,false,true,true,true);c.update(dt,p,true,true,false,false);
            check(c.attack!=CombatSystem.Attack.THRUST,"Sin apoyo no inicia estocada a "+fps);
            p=supported(0);c=new CombatSystem();
            p.update(dt,1,true,true,true,true);c.update(dt,p,true,true,false,false);
            check(c.attack!=CombatSystem.Attack.THRUST,"Salto y ataque simultaneos no inician estocada a "+fps);

            p=supported(0);c=new CombatSystem();
            c.enemies.add(new CombatSystem.Enemy(3,.5f,.4f,100));
            boolean struck=false;
            for(int i=0;i<2000&&!struck;i++) {
                p.update(dt,1,false,i==0,true,true);c.update(dt,p,i==0,true,false,false);
                struck=p.hasAttackAirJump();
            }
            check(struck&&!p.isGrounded(),"Impacto habilita salto aereo a "+fps);
            int guard=0;
            while(p.getVelocityY()>0&&guard++<2000) {
                p.update(dt,0,false,false,false,true);c.update(dt,p,false,false,false,false);
            }
            check(p.hasAttackAirJump(),"La carga dura hasta usarla o aterrizar");
            p.update(1f/60,0,true,false,false,true);
            check(p.getVelocityY()>9&&!p.hasAttackAirJump(),"H permite un salto aereo y consume carga");
            float vy=p.getVelocityY();p.update(1f/60,0,true,false,false,true);
            check(p.getVelocityY()<vy,"No permite saltos infinitos");
        }
        PlayerBody p=supported(0);p.update(.01f,1,false,true,true,true);
        check(Math.abs(p.getVelocityX()-22)<.01f,"Dash ligeramente reducido a 22");
        p.risingThrustImpact(22);
        for(int i=0;i<1000&&!p.isGrounded();i++)p.update(1f/240,0,false);
        check(p.isGrounded()&&!p.hasAttackAirJump(),"Aterrizar limpia salto pendiente");
        p.risingThrustImpact(22);p.reset(0,0);
        check(!p.hasAttackAirJump(),"Reinicio limpia salto pendiente");
        System.out.println("PASS: estocada con apoyo, bloqueo aereo, salto adicional unico, aterrizaje y dash 22");
    }
}
