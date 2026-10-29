import io.github.some_example_name.*;

public class EnemyAICheck {
    static void check(boolean ok,String msg) { if(!ok)throw new AssertionError(msg); }
    static PlayerBody ground(float x) {
        PlayerBody p=new PlayerBody();p.addSolid(-100,-2,200,2);p.reset(x,0);p.update(1f/60,0,false);return p;
    }
    static CombatSystem.Enemy enemy(CombatSystem.EnemyType type,float x,float y,boolean immune) {
        return new CombatSystem.Enemy(x,y,.5f,50,immune,type);
    }
    static void tick(float dt,PlayerBody p,PlayerVitals life,CombatSystem c,EnemyAI ai) {
        life.update(dt);c.update(dt,p,false,false,false,false);ai.update(dt,p,life,c.enemies);
    }
    public static void main(String[] args) {
        PlayerVitals life=new PlayerVitals();
        check(life.receiveDamage(1)&&life.getHealth()==9,"Dano reduce vida");
        check(!life.receiveDamage(1)&&life.getHealth()==9,"Proteccion impide dano por cada frame");
        life.update(.81f);check(life.receiveDamage(100)&&!life.alive(),"Dano mortal limita vida a cero");
        check(!life.receiveDamage(10),"Muerto no recibe mas impactos");life.reset();
        check(life.getHealth()==10&&!life.isInvulnerable(),"Restaura vida y proteccion");
        for(int fps:new int[]{30,60,144,240}) {
            float dt=1f/fps/(float)Math.ceil(240f/fps);
            PlayerBody p=ground(0);life=new PlayerVitals();CombatSystem c=new CombatSystem();EnemyAI ai=new EnemyAI();
            CombatSystem.Enemy e=enemy(CombatSystem.EnemyType.MELEE,5,.5f,false);c.enemies.add(e);
            for(int i=0;i<Math.round(3/dt);i++)tick(dt,p,life,c,ai);
            check(e.x<4&&e.y>=.49f,"Terrestre persigue y respeta suelo a "+fps);
            check(life.getHealth()<10&&life.getHealth()>4,"Ataques espaciados causan dano a "+fps);
            e.receiveHit(10,.75f);float x=e.x,y=e.y,h=life.getHealth();
            for(int i=0;i<(int)(.7f/dt);i++)tick(dt,p,life,c,ai);
            check(e.x==x&&e.y==y&&life.getHealth()==h&&e.windupRemaining==0,"Stun bloquea movimiento y ataque real");
            for(int i=0;i<Math.round(2/dt);i++)tick(dt,p,life,c,ai);
            check(life.getHealth()<h,"Recupera ataques tras stun");

            p=ground(0);life=new PlayerVitals();c=new CombatSystem();ai=new EnemyAI();
            e=enemy(CombatSystem.EnemyType.FLYING,4,5,false);c.enemies.add(e);
            for(int i=0;i<Math.round(3/dt);i++)tick(dt,p,life,c,ai);
            check(e.x<4&&e.y<5&&e.y>=.49f,"Volador persigue en ambos ejes sin entrar al suelo");
            check(life.getHealth()<10,"Volador ataca");
        }
        PlayerBody p=ground(0);life=new PlayerVitals();CombatSystem c=new CombatSystem();EnemyAI ai=new EnemyAI();
        CombatSystem.Enemy e=enemy(CombatSystem.EnemyType.MELEE,1,.5f,false);c.enemies.add(e);
        tick(.01f,p,life,c,ai);check(e.windupRemaining>0&&life.getHealth()==10,"Ataque anunciado sin dano instantaneo");
        p.reset(-8,0);for(int i=0;i<100;i++)tick(1f/240,p,life,c,ai);
        check(life.getHealth()==10,"Salir del alcance esquiva el ataque");
        p=ground(0);e=enemy(CombatSystem.EnemyType.MELEE,1,.5f,true);c=new CombatSystem();c.enemies.add(e);
        tick(.01f,p,life,c,ai);e.receiveHit(10,.75f);
        check(e.windupRemaining>0&&!e.isStunned(),"Inmune no interrumpe anuncio por stun");
        for(int i=0;i<100;i++)tick(1f/240,p,life,c,ai);
        check(life.getHealth()<10,"Inmune sigue atacando");
        e.receiveHit(100,.75f);float health=life.getHealth(), ex=e.x;
        for(int i=0;i<500;i++)tick(1f/240,p,life,c,ai);
        check(e.x==ex&&life.getHealth()==health,"Enemigo muerto no se mueve ni ataca");
        c.reset();check(e.alive()&&e.windupRemaining==0&&e.recovery==0,"Reset limpia estado IA");

        // Pared bloquea ataques y desplazamiento, incluso en vuelo.
        p=ground(0);p.addSolid(2,0,1,20);life=new PlayerVitals();c=new CombatSystem();
        e=enemy(CombatSystem.EnemyType.FLYING,4,.7f,false);c.enemies.add(e);
        for(int i=0;i<500;i++)tick(1f/240,p,life,c,ai);
        check(e.x>=3.49f&&life.getHealth()==10,"No atraviesa ni golpea a traves de pared");
        // Terrestre no abandona una plataforma para perseguir.
        p=new PlayerBody();p.addSolid(0,3,4,1);p.reset(8,4);c=new CombatSystem();life=new PlayerVitals();
        e=enemy(CombatSystem.EnemyType.MELEE,3,4.5f,false);c.enemies.add(e);
        for(int i=0;i<400;i++)tick(1f/240,p,life,c,ai);
        check(e.x<3.6f&&e.y>=4.49f,"Evita precipicios");
        // Cuerda sigue al enemigo que vuela y conserva la carga de salto al llegar.
        p=ground(0);p.reset(0,3);c=new CombatSystem();life=new PlayerVitals();
        e=enemy(CombatSystem.EnemyType.FLYING,8,6,false);c.enemies.add(e);
        check(c.tryPull(p,1,.3125f),"Gancho apunta a enemigo movil");
        for(int i=0;i<240&&!p.hasAttackAirJump();i++) {
            p.update(1f/240,0,false);tick(1f/240,p,life,c,ai);
        }
        check(p.hasAttackAirJump(),"Gancho alcanza al volador movil y restaura salto");
        System.out.println("PASS: vida, proteccion, muerte, IA terrestre/voladora, ataques, stun, inmunidad, paredes, bordes y gancho movil");
    }
}
