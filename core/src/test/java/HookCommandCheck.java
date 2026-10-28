import io.github.some_example_name.HookAimInput;
import io.github.some_example_name.PlayerBody;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Diagnostico del orden de teclas; simula la secuencia usada por TestLevelScreen. */
public class HookCommandCheck {
    static final int[][] ORDERS={{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    static PlayerBody body(boolean combo,int side) {
        PlayerBody p=new PlayerBody();
        p.addSolid(-12,-2,24,2); p.addSolid(-12,12,24,1);
        p.addSolid(-12,0,1,12); p.addSolid(11,0,1,12);
        p.reset(0,0); p.update(1f/120,0,false);
        if(combo) p.update(1f/60,side,true,true,true);
        return p;
    }
    public static void main(String[] args) throws Exception {
        List<String> rows=new ArrayList<>();
        rows.add("fps\tside\tvertical\tjumpDash\tkeyOrder\tfireX\tfireY\tlaunched\tresult");
        int correct=0, wrong=0, sameFrame=0;
        for(int fps:new int[]{30,60,144}) for(int side:new int[]{-1,1})
        for(int vertical:new int[]{-1,1}) for(boolean combo:new boolean[]{false,true}) {
            HookAimInput together=new HookAimInput();
            check(together.update(1f/fps,side<0,side>0,vertical>0,vertical<0,true,true,1),"Simultaneous diagonal fires");
            check(together.getAimX()==side && together.getAimY()==vertical,"Simultaneous diagonal direction");
            check(body(combo,side).launchHook(together.getAimX(),together.getAimY()),"Simultaneous cast hits surface");
            sameFrame++;
            for(int[] order:ORDERS) {
                HookAimInput input=new HookAimInput(); PlayerBody p=body(combo,side);
                boolean[] keys=new boolean[3]; boolean fired=false, attached=false;
                float fireX=0,fireY=0;
                for(int frame=0;frame<10;frame++) {
                    boolean pressed=false;
                    if(frame<3) { keys[order[frame]]=true; pressed=order[frame]==2; }
                    boolean fire=input.update(1f/fps,keys[0]&&side<0,keys[0]&&side>0,
                        keys[1]&&vertical>0,keys[1]&&vertical<0,pressed,keys[2],1);
                    if(fire) {
                        check(!fired,"One launch per press"); fired=true;
                        fireX=input.getAimX();fireY=input.getAimY();
                        attached=p.launchHook(fireX,fireY);
                    }
                    p.update(1f/fps,keys[0]?side:0,false,false,combo);
                }
                boolean ok=fired && attached && fireX==side && fireY==vertical;
                if(ok)correct++;else wrong++;
                String names="";
                for(int key:order) names+=(names.isEmpty()?"":" > ")+(key==0?(side<0?"A":"D"):key==1?(vertical>0?"W":"S"):"I");
                rows.add(fps+"\t"+side+"\t"+vertical+"\t"+combo+"\t"+names+"\t"+fireX+"\t"+fireY+"\t"+attached+"\t"+(ok?"PASS":"WRONG_DIRECTION"));
            }
        }
        // Comprueba el orden corregido: disparo, traccion, liberacion.
        HookAimInput tap=new HookAimInput(); PlayerBody p=body(false,1);
        tap.update(.01f,false,false,true,false,true,true,1);
        boolean fired=tap.update(.01f,false,false,true,false,false,false,1);
        boolean attached=fired && p.launchPropulsion(tap.getAimX(),tap.getAimY());
        float beforeX=p.x, beforeY=p.y;
        p.update(.016f,0,false,false,false);
        check(Math.hypot(p.x-beforeX,p.y-beforeY)>.001,"Short tap applies movement before release");
        p.releaseHook();
        check(fired && attached && !p.isHooked(),"I tap does not enter pendulum");
        check(p.isPropelling(),"I continues after U release");
        rows.add("PROPULSION_TAP\tlaunch="+attached+"\thookedAfterRelease="+p.isHooked());
        Path report=Paths.get("build/reports/hook-command.tsv");
        Files.createDirectories(report.getParent()); Files.write(report,rows,StandardCharsets.UTF_8);
        List<String> rightRows=new ArrayList<>();
        rightRows.add(rows.get(0));
        int upperCorrect=0,upperWrong=0,lowerCorrect=0,lowerWrong=0;
        for(String row:rows) {
            String[] fields=row.split("\t");
            if(fields.length!=9 || !fields[1].equals("1")) continue;
            rightRows.add(row);
            boolean pass=fields[8].equals("PASS");
            if(fields[2].equals("1")) { if(pass)upperCorrect++;else upperWrong++; }
            else { if(pass)lowerCorrect++;else lowerWrong++; }
        }
        check(upperCorrect+upperWrong==36,"Right-up coverage: six orders, three FPS, with/without jump-dash");
        check(lowerCorrect+lowerWrong==36,"Right-down coverage: six orders, three FPS, with/without jump-dash");
        Path rightReport=Paths.get("build/reports/hook-command-right.tsv");
        Files.write(rightReport,rightRows,StandardCharsets.UTF_8);
        System.out.println("RIGHT UP: "+upperCorrect+" correct, "+upperWrong+" wrong direction");
        System.out.println("RIGHT DOWN: "+lowerCorrect+" correct, "+lowerWrong+" wrong direction");
        System.out.println("RIGHT REPORT: "+rightReport.toAbsolutePath());
        int leftUpOk=0,leftUpWrong=0,leftDownOk=0,leftDownWrong=0;
        List<String> leftRows=new ArrayList<>(); leftRows.add(rows.get(0));
        for(String row:rows) {
            String[] fields=row.split("\t");
            if(fields.length!=9 || !fields[1].equals("-1")) continue;
            leftRows.add(row);
            boolean pass=fields[8].equals("PASS");
            if(fields[2].equals("1")) { if(pass)leftUpOk++;else leftUpWrong++; }
            else { if(pass)leftDownOk++;else leftDownWrong++; }
        }
        check(leftUpOk==36 && leftUpWrong==0,"Upper-left regression: every order succeeds");
        check(leftDownOk==36 && leftDownWrong==0,"Lower-left regression: every order succeeds");
        Files.write(Paths.get("build/reports/hook-command-left.tsv"),leftRows,StandardCharsets.UTF_8);
        System.out.println("LEFT UP: "+leftUpOk+" correct, "+leftUpWrong+" wrong direction");
        System.out.println("LEFT DOWN: "+leftDownOk+" correct, "+leftDownWrong+" wrong direction");
        System.out.println("SIMULTANEOUS: "+sameFrame+" passed");
        System.out.println("ORDERED: "+correct+" correct, "+wrong+" wrong direction");
        System.out.println("PROPULSION: tap continues independently of pendulum release");
        check(wrong==0,"Every ordered diagonal must use the requested direction");
        System.out.println("REPORT: "+report.toAbsolutePath());
    }
}
