import io.github.some_example_name.*;
import com.badlogic.gdx.files.FileHandle;
import java.io.File;
import javax.imageio.ImageIO;

public final class RedesignIntegrationCheck {
    static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);}
    static void near(float a,float b){check(Math.abs(a-b)<.0001f,"Transformacion: "+a+" / "+b);}
    static PlayerBody floor(){PlayerBody p=new PlayerBody();p.addSolid(-100,-2,200,2);p.reset(0,0);p.update(.01f,0,false);return p;}
    static CombatSystem sizedCombat(com.badlogic.gdx.utils.JsonValue data){CombatSystem c=new CombatSystem();c.reach=data.getFloat("reach");c.downReach=data.getFloat("downReach");c.attackOriginHeight=data.getFloat("attackOriginHeight");c.whirlwindContactRadius=data.getFloat("whirlwindContactRadius");return c;}
    public static void main(String[] args){
        File assets=new File("assets");if(!assets.isDirectory())assets=new File("../assets");
        RedesignClips clips=new RedesignClips(new FileHandle(new File(assets,RedesignClips.CONFIG)));int total=0;
        for(RedesignClips.Clip c:clips.all())for(RedesignClips.Frame f:c.frames){
            try{java.awt.image.BufferedImage im=ImageIO.read(new File(assets,f.file));check(im!=null&&im.getColorModel().hasAlpha(),"PNG RGBA: "+f.file);check(f.px>=0&&f.px<im.getWidth()&&f.py>=0&&f.py<im.getHeight(),"Pivote dentro del lienzo");}catch(Exception e){throw new AssertionError(e);}total++;
        }
        check(total==63,"63 poses nuevas y aprobadas disponibles");check(clips.get("run").frames.length==6,"Carrera seis");check(clips.get("walk").frames.length==4,"Caminar cuatro");
        near(clips.getRenderScale(),2);
        for(RedesignClips.Clip clip:clips.all())for(RedesignClips.Frame f:clip.frames)for(int face:new int[]{-1,1}){
            SpritePose ground=new SpritePose(),air=new SpritePose();float ppu=clip.ppu/clips.getRenderScale();
            ground.placeAtFeet(f,4,2,ppu,face,false);air.placeAtFeet(f,4,2,ppu,face,true);
            near(ground.pointY(f.px,f.floorY),2);near(ground.pointX(f.px,f.floorY),4);
            near(ground.pointX(f.handX,f.handY),air.pointX(f.handX,f.handY));near(ground.pointY(f.handX,f.handY),air.pointY(f.handX,f.handY));
            near(Math.abs(ground.pointX(f.px+100,f.floorY)-4),200/clip.ppu);
        }
        for(RedesignClips.Frame f:clips.get("attack_side").frames){
            try{java.awt.image.BufferedImage image=ImageIO.read(new File(assets,f.file));int floor=0;for(int yy=0;yy<image.getHeight();yy++)for(int xx=0;xx<image.getWidth();xx++)if((image.getRGB(xx,yy)>>>24)>128)floor=Math.max(floor,yy);near(f.floorY,floor+1);}catch(Exception e){throw new AssertionError(e);}
        }
        com.badlogic.gdx.utils.JsonValue attacks=new com.badlogic.gdx.utils.JsonReader().parse(new FileHandle(new File(assets,"combat/prototype/attacks.json")));
        near(attacks.getFloat("reach"),3.3f);near(attacks.getFloat("damage"),10);near(attacks.getFloat("stunDuration"),.75f);
        PlayerBody falling=floor();falling.reset(0,5);CombatSystem down=sizedCombat(attacks);
        CombatSystem.Enemy close=new CombatSystem.Enemy(.25f,4.8f,.1f,30),far=new CombatSystem.Enemy(.25f,2,.1f,30);down.enemies.add(close);down.enemies.add(far);
        down.update(.05f,falling,true,true,false,true);check(close.health==20&&far.health==30,"El ataque inferior respeta la punta, sin golpes invisibles muy por debajo");check(close.isStunned(),"El ajuste visual conserva el stun");
        CombatSystem sweep=sizedCombat(attacks);CombatSystem.Enemy lateral=new CombatSystem.Enemy(3,.7f,.2f,30);sweep.enemies.add(lateral);sweep.update(.05f,floor(),true,true,false,false);check(lateral.health==20,"Alcance lateral ampliado");
        CombatSystem up=sizedCombat(attacks);CombatSystem.Enemy above=new CombatSystem.Enemy(.25f,4.7f,.2f,30);up.enemies.add(above);up.update(.05f,floor(),true,true,true,false);check(above.health==20,"Alcance superior desde altura visual");
        check(clips.get("death").index(100,false)==2,"Muerte conserva pose");check(clips.get("rest").index(100,false)==7,"Una rodilla hasta moverse");
        check(clips.get("thrust").index(.25f,false)==2&&clips.get("thrust").index(.25f,true)==3,"Impacto y fallo separados");
        check(clips.get("attack_down").index(.3f,false)==1&&clips.get("attack_down").index(.3f,true)==2,"Rebote solo con impacto");
        for(String id:new String[]{"pendulum","whirlwind"})for(RedesignClips.Frame f:clips.get(id).frames)for(int deg=-180;deg<=180;deg+=45)for(int sign:new int[]{-1,1}){
            SpritePose p=new SpritePose();p.ppu=clips.get(id).ppu;p.originX=f.hx;p.originY=f.hy;
            double a=Math.toRadians(deg);p.x=(float)Math.sin(a)*10;p.y=-(float)Math.cos(a)*10;
            float vx=sign*(float)Math.cos(a),vy=sign*(float)Math.sin(a);
            p.radial(f,0,0,vx,vy,id.equals("whirlwind"));
            float hx=p.pointX(f.handX,f.handY),hy=p.pointY(f.handX,f.handY);
            if(id.equals("pendulum")){near(hx*p.y-hy*p.x,0);check(Math.hypot(hx,hy)<10,"Mano entre jugador y anclaje");}
            else near((float)Math.IEEEremainder(p.rotation-deg,360),0);
        }
        PlayerBody p=floor();CombatSystem c=new CombatSystem();PlayerVitals life=new PlayerVitals();RedesignAnimation a=new RedesignAnimation();a.reset(10);
        for(int i=0;i<400;i++)a.update(.01f,p,c,life,0);check(a.getState().equals("rest"),"Reposo ocasional");
        for(int i=0;i<400;i++)a.update(.01f,p,c,life,0);check(clips.get("rest").index(a.getTime(),false)==7,"Permanece arrodillado");
        p.update(.02f,1,false);a.update(.02f,p,c,life,0);check(a.getState().equals("stand_up"),"Se levanta sin bloquear movimiento");
        p.update(.01f,1,true);a.update(.01f,p,c,life,0);check(a.getState().equals("jump"),"Saltar interrumpe levantarse");
        float x=p.x,y=p.y,vx=p.getVelocityX(),vy=p.getVelocityY();c.attack=CombatSystem.Attack.HOOK_STRIKE;a.update(.01f,p,c,life,0);check(a.getState().equals("hook_strike"),"Ataque rapido propio");check(x==p.x&&y==p.y&&vx==p.getVelocityX()&&vy==p.getVelocityY(),"Selector visual no modifica fisica");
        life.receiveDamage(1);a.update(.01f,p,c,life,0);check(a.getState().equals("hurt"),"Daño");life.update(1);life.receiveDamage(20);a.update(.01f,p,c,life,0);check(a.getState().equals("death"),"Muerte");
        System.out.println("PASS: integración de 21 clips / 63 PNG, ramas de impacto, reposo y orientación invertida, física intacta");
    }
}
