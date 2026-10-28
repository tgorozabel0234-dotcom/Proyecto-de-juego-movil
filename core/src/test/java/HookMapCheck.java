import io.github.some_example_name.PlayerBody;
public class HookMapCheck {
 static PlayerBody body(float y) {
 PlayerBody p = new PlayerBody();
 p.addSolid(0f,-1.7068063f,10f,1.7068063f);
p.addSolid(10f,-1.7068063f,10f,1.7068063f);
p.addSolid(20f,-1.7068063f,10f,1.7068063f);
p.addSolid(30f,-1.7068063f,10f,1.7068063f);
p.addSolid(40f,-1.7068063f,10f,1.7068063f);
p.addSolid(50f,-1.7068063f,10f,1.7068063f);
p.addSolid(60f,-1.7068063f,10f,1.7068063f);
p.addSolid(70f,-1.7068063f,10f,1.7068063f);
p.addSolid(-3f,-2f,3f,8.4f);
p.addSolid(-3f,6.4f,3f,8.4f);
p.addSolid(-3f,14.8f,3f,8.4f);
p.addSolid(-3f,23.2f,3f,8.4f);
p.addSolid(-3f,31.6f,3f,8.4f);
p.addSolid(80f,-2f,3f,8.4f);
p.addSolid(80f,6.4f,3f,8.4f);
p.addSolid(80f,14.8f,3f,8.4f);
p.addSolid(80f,23.2f,3f,8.4f);
p.addSolid(80f,31.6f,3f,8.4f);
p.addSolid(36f,17.634556f,8f,1.365445f);
p.addSolid(0f,32.95183f,12f,2.0481675f);
p.addSolid(68f,32.95183f,12f,2.0481675f);
 p.reset(39.75f,y); return p;
 }
 public static void main(String[] args) {
 System.out.println("Checking mirrored left/right hooks on level 00 geometry");
 for (float y : new float[]{0,5,20,30}) for (int dy : new int[]{-1,0,1}) {
 PlayerBody l=body(y), r=body(y);
 boolean left=l.launchHook(-1,dy), right=r.launchHook(1,dy);
 if(left != right) throw new AssertionError("Asymmetric hit at y="+y+", dy="+dy);
 float max=0;
 for(int i=0;i<240;i++) {
 l.update(1f/120,-1,false,false,false); r.update(1f/120,1,false,false,false);
 max=Math.max(max,Math.max(Math.abs(l.x+r.x-79.5f),Math.abs(l.y-r.y)));
 }
 if(max>.005f) throw new AssertionError("Asymmetric motion: "+max);
 }
 }
}