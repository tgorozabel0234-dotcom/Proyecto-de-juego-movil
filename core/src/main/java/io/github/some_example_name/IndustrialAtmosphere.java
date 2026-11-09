package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonValue;

/** Luz, instalaciones y partículas dibujadas en coordenadas del escenario. */
final class IndustrialAtmosphere implements Disposable {
    private final Texture glow = gradient(false);
    private final Texture beam = gradient(true);
    private final Texture shade = vignette();
    private static Texture vignette() {
        int size=96;
        Pixmap mask=new Pixmap(size,size,Pixmap.Format.RGBA8888);
        mask.setBlending(Pixmap.Blending.None);
        for(int y=0;y<size;y++) for(int x=0;x<size;x++){
            float u=(2f*x/(size-1)-1), v=(2f*y/(size-1)-1);
            float edge=MathUtils.clamp((u*u+v*v-.4f)*.48f,0,.72f);
            mask.drawPixel(x,y,Color.rgba8888(.025f,.035f,.095f,edge*edge));
        }
        Texture result=new Texture(mask);
        mask.dispose();
        result.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        return result;
    }

    private static final Color STEEL = Color.valueOf("263747");
    private static final Color RIM = Color.valueOf("536575");
    private static final Color INK = Color.valueOf("0b1320");
    private static final Color CYAN = Color.valueOf("40eeef");
    private static final Color HOT = Color.valueOf("ff9955");
    private static final Color PINK = Color.valueOf("dc70b4");

    private static Texture gradient(boolean cone) {
        int n = 96;
        Pixmap image = new Pixmap(n,n,Pixmap.Format.RGBA8888);
        image.setBlending(Pixmap.Blending.None);
        for (int j=0;j<n;j++) for (int i=0;i<n;i++) {
            float y=j/(float)(n-1), x=2*i/(float)(n-1)-1;
            float a=cone ? (1-y)*(1-Math.abs(x+.23f*y)/(.045f+.84f*y))
                : 1-(float)Math.sqrt(x*x+(2*y-1)*(2*y-1));
            a=Math.max(0,a);
            image.drawPixel(i,j,Color.rgba8888(1,1,1,a*a));
        }
        Texture t=new Texture(image);
        image.dispose();
        t.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear);
        return t;
    }

    private static void bar(SpriteBatch b,Texture p,float x,float y,float w,float h,Color c,float a) {
        b.setColor(c.r,c.g,c.b,a);
        b.draw(p,x,y,w,h);
    }
    private static void strut(SpriteBatch b,Texture p,float x,float y,float dx,float dy,float t,Color c) {
        b.setColor(c);
        b.draw(p,x,y,0,t/2,(float)Math.hypot(dx,dy),t,1,1,MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees,0,0,1,1,false,false);
    }
    private void halo(SpriteBatch b,float x,float y,float w,float h,Color color,float alpha) {
        bar(b,glow,x-w/2,y-h/2,w,h,color,alpha);
    }
    private static float wave(float t) {return .5f+.5f*MathUtils.sin(t);}
    private static float fract(float x) {return x-(float)Math.floor(x);}

    void conduit(SpriteBatch b,Texture p,float x,float y,float w,float h,float ceiling) {
        float joint=x+w*.72f, top=y+h*.72f;
        strut(b,p,joint,top,0,3,.32f,INK);
        strut(b,p,joint+.07f,top,0,3,.08f,RIM);
        strut(b,p,joint,top+3,w*.36f,1,.32f,STEEL);
        strut(b,p,joint+w*.36f,top+4,0,ceiling-top-4,.23f,STEEL);
        for(int i=0;i<4;i++) bar(b,p,joint-.06f,top+i*.82f,.44f,.11f,RIM,1);
    }

    void fittings(SpriteBatch b,Texture p,float x,float y,float w,float h,float time,boolean hot) {
        float joint=x+w*.72f,top=y+h*.72f;
        Color lamp=hot?HOT:CYAN;
        float lx=x+w*.75f, ly=y+h*.53f;
        bar(b,p,lx,ly,1.24f,.8f,INK,.96f);
        for(int i=0;i<7;i++) bar(b,p,lx+.1f+i*.15f,ly+.11f,.09f,.12f+.45f*wave(time*2+i*.83f),lamp,.9f);
        halo(b,lx+.6f,ly+.4f,2.6f,1.8f,lamp,.24f);
        if (hot && fract(time*.73f+x)<.14f) {
            for(int i=0;i<4;i++) {
                float k=fract(time*2+i*.24f);
                strut(b,p,joint+k*(i-1),top-k*k*1.5f,.08f,-.11f,.035f,HOT);
            }
        } else if (!hot) {
            float drop=fract(time*.57f+x);
            bar(b,p,joint,top-3.2f*drop*drop,.035f,.14f,CYAN,.65f);
        }
    }

    void underDeck(SpriteBatch b,Texture p,float x,float y,float w) {
        float span=Math.min(w-.4f,8.5f);
        if(span<1) return;
        for(float start=x+.2f;start+span<=x+w;start+=span+2) {
            strut(b,p,start,y,span*.38f,-2.1f,.22f,STEEL);
            strut(b,p,start+span*.38f,y-2.1f,span*.55f,2.1f,.18f,RIM);
            for(int cable=0;cable<2;cable++) {
                float depth=1.45f+cable*.53f;
                for(int i=0;i<10;i++) {
                    float u=i*.1f,v=(i+1)*.1f;
                    float a=-4*depth*u*(1-u), z=-4*depth*v*(1-v);
                    strut(b,p,start+u*span,y+a,span*.1f,z-a,.09f+cable*.025f,INK);
                }
            }
        }
    }

    void hangers(SpriteBatch b,Texture p,float x,float y,float w,float h) {
        float span=Math.min(5f,w*.75f);
        for (int i=0;i<2;i++) {
            float foot=x+.2f+i*span, rise=4.2f+i*.55f;
            strut(b,p,foot,y+h,1.4f,rise,.09f,STEEL);
            strut(b,p,foot+.09f,y+h,1.4f,rise,.035f,RIM);
            bar(b,p,foot+1.12f,y+h+rise,.74f,.28f,INK,1);
            bar(b,p,foot+1.21f,y+h+rise+.05f,.16f,.1f,HOT,.8f);
        }
        strut(b,p,x,y+.1f,-.55f,-.27f,.14f,RIM);
        strut(b,p,x+.25f,y,-.65f,-.59f,.12f,STEEL);
    }

    void deck(SpriteBatch b,Texture p,float x,float top,float w,float time) {
        for(float at=.12f;at<w-.18f;at+=2.8f) {
            float length=Math.min(2.61f,w-at-.12f);
            int bank=(int)((at+x+top)/5)%8;
            Color color=bank==2||bank==3?HOT:bank==6?PINK:CYAN;
            float pulse=.84f+.15f*wave(time*3+at);
            halo(b,x+at+length/2,top-.19f,length+.6f,.73f,color,.48f);
            bar(b,p,x+at,top-.22f,length,.095f,color,pulse);
            bar(b,p,x+at,top-.11f,.13f,.07f,HOT,.55f);
        }
    }

    void pipe(SpriteBatch b,Texture p,float x,float y,float w,float h) {
        bar(b,p,x,y,w,h,STEEL,1);
        bar(b,p,x,y+h*.15f,w,h*.13f,INK,1);
        bar(b,p,x,y+h*.61f,w,h*.23f,CYAN,.8f);
        for(float i=.12f;i<w-.12f;i+=1.9f) bar(b,p,x+i,y,.17f,h,RIM,1);
    }

    void fluid(SpriteBatch b,Texture p,float x,float y,float w,float h,float time) {
        float level=h*(.31f+.035f*wave(time*.7f));
        float fx=x+w*.40f,fy=y+h*.23f,fw=w*.24f;
        bar(b,p,fx,fy,fw,level,CYAN,.32f);
        bar(b,p,fx,fy+level,fw,.065f,CYAN,.84f);
        halo(b,fx+fw/2,fy+level*.6f,fw*2,level*2,CYAN,.22f);
    }

    void windows(SpriteBatch b,Texture p,float x,float y,float w,float h,float time) {
        for(int i=0;i<18;i++) {
            float dx=w*fract(.317f+i*.618f),dy=h*fract(.213f+i*.367f);
            bar(b,p,x+dx,y+dy,.11f,.055f,i%4==0?PINK:CYAN,.09f+.26f*wave(time*.8f+i));
        }
    }

    void air(SpriteBatch b,Texture p,OrthographicCamera c,float time,float height) {
        float left=c.position.x-c.viewportWidth*c.zoom/2, right=2*c.position.x-left;
        float low=c.position.y-c.viewportHeight*c.zoom/2, high=2*c.position.y-low;
        for(int col=(int)Math.floor(left/19)-1;col<=Math.ceil(right/19);col++) {
            for(int row=(int)Math.floor(low/16);row<=Math.ceil(high/16);row++) {
                float x=col*19+7*MathUtils.sin(row+col),y=row*16+12;
                if(y>height+3)continue;
                bar(b,beam,x-6,y-14,12,14,CYAN,.31f);
                bar(b,p,x-.55f,y,1.1f,.22f,STEEL,1);
                bar(b,p,x-.32f,y-.07f,.64f,.09f,CYAN,.92f);
                for(int i=0;i<7;i++){
                    float k=fract(time*.07f+i*.37f+row*.13f);
                    float px=x+17*fract(i*.61f+col*.31f)-8+.35f*MathUtils.sin(time+k*6);
                    bar(b,p,px,y-k*16,.045f,.065f,CYAN,.15f+.35f*wave(time+i));
                }
            }
        }
    }

    void vent(SpriteBatch b,Texture p,JsonValue vent,float time) {
        float x=vent.getFloat("x"), y=vent.getFloat("y"), w=vent.getFloat("width"), h=vent.getFloat("height");
        float clock=SteamVent.clock(vent,time), blast=vent.getFloat("blast");
        boolean emitting=clock<blast, warning=clock>vent.getFloat("period")-vent.getFloat("warning");
        bar(b,p,x-.15f,y-.20f,w+.3f,.24f,STEEL,1);
        bar(b,p,x,y+.02f,w,.10f,emitting||warning?HOT:CYAN,warning?.45f+.55f*wave(time*21):1);
        if(emitting||warning) for(int i=0;i<13;i++){
            float u=fract(time*1.9f+i/13f), spread=w*(.34f+.4f*u);
            float center=x+w/2+MathUtils.sin(i*12+time)*spread*.4f;
            float size=spread*(1.4f+u);
            halo(b,center,y+u*h*(warning?.2f:1),size,size*1.5f,Color.WHITE,(1-u*.5f)*(warning?.23f:.82f));
        }
    }

    void foreground(SpriteBatch b,Texture p,OrthographicCamera c) {
        float hw=c.viewportWidth*c.zoom/2, hh=c.viewportHeight*c.zoom/2;
        float left=c.position.x-hw,top=c.position.y+hh,bottom=c.position.y-hh;
        b.setColor(Color.WHITE);
        b.draw(shade,left,bottom,hw*2,hh*2);
        float pitch=17*c.zoom, shift=fract(c.position.x*1.36f/pitch)*pitch;
        for(float x=left-shift;x<left+hw*2;x+=pitch) {
            float z=c.zoom;
            for(int i=0;i<12;i++) {
                float u=i/12f,v=(i+1)/12f;
                float a=4*u*(1-u),d=4*v*(1-v);
                strut(b,p,x+u*pitch,top+.3f*z-a*1.05f*z,pitch/12,(a-d)*1.05f*z,.14f*z,INK);
            }
            strut(b,p,x,bottom-.3f*z,pitch*.27f,.65f*z,.29f*z,INK);
        }
        b.setColor(Color.WHITE);
    }
    public void dispose(){glow.dispose();beam.dispose();shade.dispose();}
}
