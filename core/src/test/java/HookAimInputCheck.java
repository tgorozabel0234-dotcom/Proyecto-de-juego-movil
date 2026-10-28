import io.github.some_example_name.HookAimInput;
public class HookAimInputCheck {
    static void check(boolean v, String s) { if(!v) throw new AssertionError(s); }
    public static void main(String[] args) {
        for(boolean upper : new boolean[]{true,false}) {
            HookAimInput h=new HookAimInput();
            check(h.update(.016f,true,false,upper,!upper,true,true,1),"Left diagonal fires");
            check(h.getAimX()==-1 && h.getAimY()==(upper?1:-1),"Left diagonal direction");
            for(int i=0;i<120;i++) check(!h.update(.016f,true,false,upper,!upper,false,true,-1),"No repeated shot");
            h=new HookAimInput();
            check(!h.update(.016f,true,false,false,false,true,true,-1),"Wait for second arrow");
            check(h.update(.016f,true,false,upper,!upper,false,true,-1),"Second arrow after V");
            check(h.getAimX()==-1 && h.getAimY()==(upper?1:-1),"Buffered left diagonal");
            h=new HookAimInput();
            check(!h.update(.016f,false,false,false,false,true,true,1),"V arrives first");
            check(h.update(.016f,true,false,upper,!upper,false,true,1),"Both arrows after V");
        }
        HookAimInput horizontal=new HookAimInput();
        boolean fired=false;
        for(int i=0;i<12;i++) fired |= horizontal.update(.016f,false,true,false,false,i==0,true,1);
        check(fired && horizontal.getAimX()==1 && horizontal.getAimY()==0,"Horizontal retained");
        HookAimInput tap=new HookAimInput();
        tap.update(.01f,false,false,true,false,true,true,1);
        check(tap.update(.01f,false,false,true,false,false,false,1),"Short tap fires on release");
        System.out.println("PASS: left diagonals, key order, hold, horizontal and short tap");
    }
}
