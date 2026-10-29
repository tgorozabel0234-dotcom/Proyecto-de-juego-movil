package io.github.some_example_name;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** HUD adaptativo compartido por escritorio y futuros lanzadores de telefono. */
public final class GameHud {
    public final Stage stage=new Stage(new ScreenViewport());
    public final Label lifeStatus, movementStatus, hookStatus, combatStatus, hookInputStatus, debugSummary;
    public final Table helpPanel=new Table(),debugPanel=new Table();
    private final Table pausePanel=new Table(),header=new Table(), banner=new Table();
    private final Table controls=new Table();
    private final Table card=new Table();
    private final ScreenViewport viewport=(ScreenViewport)stage.getViewport();
    private final TouchControls touch;
    private final Texture pixel;
    private final BitmapFont font;
    private final Label.LabelStyle text,muted;
    private final TextButton.TextButtonStyle buttonStyle;
    private final TextureRegionDrawable white;
    private final TouchButton[] buttons=new TouchButton[TouchControls.Action.values().length];
    private final Stick stick=new Stick();
    private final HealthCells healthCells=new HealthCells();
    private final Bar dashBar=new Bar(Color.valueOf("15F4E3"));
    private Label healthReadout;
    private final Color cyan=Color.valueOf("15F4E3"),pink=Color.valueOf("FF4FBF"),violet=Color.valueOf("9467FF");
    private final float[] triangleVertices=new float[20];
    private float hudTime;
    private final float margin,stickSize,opacity;
    private final Color accent;
    private final TextButton modeButton;
    private boolean touchVisible,wasAlive=true;

    public GameHud(Texture pixel,BitmapFont font,TouchControls touch,Runnable restart,Runnable exit,Runnable map) {
        this.pixel=pixel;this.font=font;this.touch=touch;
        JsonValue config=new JsonReader().parse(Gdx.files.internal("ui/mobile_hud/layout.json"));
        margin=config.getFloat("margin",18);stickSize=config.getFloat("joystickDiameter",144);
        opacity=config.getFloat("opacity",.58f);touch.deadzone=config.getFloat("deadzone",.18f);
        accent=Color.valueOf(config.getString("accent","15F4E3"));
        cyan.set(Color.valueOf(config.getString("cyan","15F4E3")));
        pink.set(Color.valueOf(config.getString("pink","FF4FBF")));
        violet.set(Color.valueOf(config.getString("violet","9467FF")));
        healthCells.count=MathUtils.clamp(config.getInt("healthCells",10),1,10);
        touchVisible=Gdx.app.getType()==ApplicationType.Android||Gdx.app.getType()==ApplicationType.iOS
            ||Boolean.getBoolean("game.touchHud");
        text=new Label.LabelStyle(font,Color.valueOf("ECF5F8"));
        muted=new Label.LabelStyle(font,Color.valueOf("A5BAC9"));
        white=new TextureRegionDrawable(new TextureRegion(pixel));
        buttonStyle=new TextButton.TextButtonStyle();buttonStyle.font=font;buttonStyle.fontColor=Color.WHITE;
        buttonStyle.up=new CyberPanel(new Color(.025f,.035f,.085f,.92f),new Color(.12f,.5f,.58f,.6f));
        buttonStyle.over=new CyberPanel(new Color(.08f,.055f,.16f,.96f),pink);
        buttonStyle.down=new CyberPanel(new Color(.08f,.24f,.29f,1),cyan);
        controls.setTouchable(Touchable.childrenOnly);stage.addActor(controls);
        controls.addActor(stick);
        for(TouchControls.Action action:TouchControls.Action.values()) {
            TouchButton b=new TouchButton(action);buttons[action.ordinal()]=b;controls.addActor(b);
        }
        lifeStatus=new Label("INTEGRIDAD",text);
        movementStatus=new Label("DASH LISTO",muted);
        hookStatus=new Label("GANCHO LISTO",muted);hookStatus.setWrap(true);
        combatStatus=new Label("",text);combatStatus.setWrap(true);
        card.setBackground(new CyberPanel(new Color(.015f,.025f,.065f,.65f),cyan));card.pad(6);
        healthReadout=new Label("100%",muted);
        card.add(healthCells).growX().height(18);
        header.setTouchable(Touchable.childrenOnly);header.addActor(card);
        modeButton=button("TACTIL",()->toggleTouch());header.addActor(modeButton);
        TextButton pause=button("II",()->togglePause());pause.setName("pause");header.addActor(pause);
        stage.addActor(header);
        banner.setBackground(new CyberPanel(new Color(.035f,.02f,.07f,.85f),pink));banner.pad(8);
        banner.add(combatStatus).width(220);banner.setTouchable(Touchable.disabled);stage.addActor(banner);

        Table pauseCard=overlay(pausePanel,"PAUSA");
        add(pauseCard,button("CONTINUAR",()->closePanels()));
        add(pauseCard,button("AYUDA / CONTROLES",()->toggleHelp()));
        add(pauseCard,button("REINICIAR PRUEBA",()->{closePanels();restart.run();}));
        add(pauseCard,button("CONTROLES TACTILES SI / NO",()->toggleTouch()));
        add(pauseCard,button("VISTA GENERAL / MAPA",()->{closePanels();map.run();}));
        add(pauseCard,button("DATOS DE PRUEBA",()->toggleDebug()));
        add(pauseCard,button("VOLVER AL MENU",exit));
        Table helpCard=overlay(helpPanel,"CONTROLES");
        Label help=new Label("TELEFONO\nJoystick: mover y apuntar en 8 direcciones.\nSALTO: tocar / mantener para mas altura.\nDASH: tocar; sprint mientras mueves el joystick.\nATAQUE: combinar con direccion del joystick.\nTIRON: propulsion hacia superficie o enemigo.\nGIRO: tocar para enganchar; tocar para soltar.\nCon GIRO activo: ATAQUE breve o mantenido.\n\nTECLADO\nWASD: movimiento / apuntado\nH: salto | J: dash y sprint | K: ataque\nU: propulsion | Mantener I: pendulo\nF3: mostrar controles tactiles\nF1: ayuda | F2: datos | M: mapa | R: reiniciar\nEsc: pausa / continuar\n\nEl rebote, estocada acertada y TIRON contra\nun enemigo recuperan un salto con SALTO / H.",muted);
        help.setWrap(true);
        ScrollPane.ScrollPaneStyle scrollStyle=new ScrollPane.ScrollPaneStyle();
        com.badlogic.gdx.scenes.scene2d.utils.Drawable track=white.tint(new Color(.1f,.18f,.23f,1));track.setMinWidth(4);
        com.badlogic.gdx.scenes.scene2d.utils.Drawable knob=white.tint(new Color(.7f,.8f,.85f,1));knob.setMinWidth(4);knob.setMinHeight(24);
        scrollStyle.vScroll=track;scrollStyle.vScrollKnob=knob;
        ScrollPane scroll=new ScrollPane(help,scrollStyle);scroll.setFadeScrollBars(false);scroll.setScrollingDisabled(true,false);
        helpCard.add(scroll).width(310).height(235).padBottom(10);helpCard.row();
        helpCard.add(new Label("Desliza para ver todos los controles",muted)).left().padBottom(8);helpCard.row();
        add(helpCard,button("VOLVER",()->closePanels()));
        Table debugCard=overlay(debugPanel,"DATOS DE PRUEBA");
        hookInputStatus=new Label("",muted);hookInputStatus.setWrap(true);
        debugSummary=new Label("",muted);debugSummary.setWrap(true);
        debugCard.add(hookInputStatus).width(300).height(52);debugCard.row();
        debugCard.add(debugSummary).width(300).height(150);debugCard.row();
        add(debugCard,button("CERRAR",()->closePanels()));
        helpPanel.setVisible(false);debugPanel.setVisible(false);pausePanel.setVisible(false);
        controls.setVisible(touchVisible);modeButton.setVisible(false);
    }
    private Table overlay(Table root,String title) {
        root.setFillParent(true);root.setTouchable(Touchable.enabled);
        root.setBackground(white.tint(new Color(.005f,.015f,.025f,.65f)));
        Table body=new Table();body.pad(18);body.setBackground(new CyberPanel(new Color(.018f,.025f,.06f,.98f),cyan));
        body.add(new Label(title,text)).left().padBottom(14);body.row();root.add(body).pad(12);
        stage.addActor(root);return body;
    }
    private void add(Table table,Actor actor) {table.add(actor).width(310).height(40).padBottom(5);table.row();}
    private TextButton button(String title,Runnable action) {
        TextButton result=new TextButton(title,buttonStyle);
        result.setName(title);
        result.addListener(new ClickListener(){@Override public void clicked(InputEvent e,float x,float y){action.run();}});
        return result;
    }
    public boolean touchVisible(){return touchVisible;}
    public boolean isPaused(){return pausePanel.isVisible()||helpPanel.isVisible();}
    public void closePanels(){pausePanel.setVisible(false);helpPanel.setVisible(false);debugPanel.setVisible(false);cancelTouches();}
    public void togglePause(){boolean open=!pausePanel.isVisible();closePanels();pausePanel.setVisible(open);}
    public void toggleHelp(){boolean open=!helpPanel.isVisible();closePanels();helpPanel.setVisible(open);}
    public void toggleDebug(){boolean open=!debugPanel.isVisible();closePanels();debugPanel.setVisible(open);}
    public void toggleTouch(){touchVisible=!touchVisible;controls.setVisible(touchVisible);cancelTouches();}
    public void cancelTouches(){stage.cancelTouchFocus();touch.reset();}
    public void update(PlayerBody player,PlayerVitals life,CombatSystem combat) {
        life.cellCount=healthCells.count;
        healthCells.updateValues(life);
        healthReadout.setText(Math.round(life.getHealth()*100/Math.max(1,life.maxHealth))+"%");
        dashBar.value=player.getDashRecoveryFraction();
        TouchButton dash=buttons[TouchControls.Action.DASH.ordinal()];
        if(dash.ready<1&&player.canDash())dash.readyPulse=.3f;
        dash.ready=player.getDashRecoveryFraction();dash.remaining=player.getDashCooldownRemaining();
        buttons[TouchControls.Action.JUMP.ordinal()].highlight=player.hasAttackAirJump();
        buttons[TouchControls.Action.PENDULUM.ordinal()].highlight=player.isHooked();
        banner.setVisible(false);
        header.findActor("pause").setVisible(touchVisible);
        if(wasAlive&&!life.alive())cancelTouches();
        wasAlive=life.alive();controls.setTouchable(life.alive()?Touchable.childrenOnly:Touchable.disabled);
        modeButton.setText(touchVisible?"TACTIL SI":"TACTIL NO");
    }
    public void resize(int width,int height) {
        cancelTouches();
        float left=Gdx.graphics.getSafeInsetLeft(),right=Gdx.graphics.getSafeInsetRight();
        float bottom=Gdx.graphics.getSafeInsetBottom(),top=Gdx.graphics.getSafeInsetTop();
        boolean portrait=height>width;
        float scale=Math.min((width-left-right)/(portrait?450f:800f),(height-top-bottom)/(portrait?800f:450f));
        scale=Math.max(.2f,Math.min(4,scale));viewport.setUnitsPerPixel(1/scale);viewport.update(width,height,true);
        left/=scale;right/=scale;bottom/=scale;top/=scale;
        float w=viewport.getWorldWidth(),h=viewport.getWorldHeight();
        controls.setBounds(0,0,w,h);header.setBounds(0,0,w,h);
        HudLayout layout=new HudLayout(w,h,left,right,bottom,margin,stickSize);
        bounds(stick,layout.stick);
        for(TouchControls.Action a:TouchControls.Action.values())bounds(buttons[a.ordinal()],layout.actions[a.ordinal()]);
        float cardWidth=Math.min(200,(w-left-right)*.5f);
        card.setBounds(left+margin,h-top-margin-32,cardWidth,32);
        modeButton.setBounds(w-right-margin-132,h-top-margin-44,76,44);
        header.findActor("pause").setBounds(w-right-margin-48,h-top-margin-44,48,44);
        banner.setBounds(left+margin,h-top-margin-168,cardWidth,36);
        combatStatus.setWrap(true);banner.getCell(combatStatus).width(cardWidth-16);
    }
    private void bounds(Actor actor,HudLayout.Box b){actor.setBounds(b.x,b.y,b.w,b.h);}
    private void line(Batch b,float x,float y,float dx,float dy,float thickness) {
        float length=(float)Math.hypot(dx,dy);
        b.draw(pixel,x,y-thickness/2,0,thickness/2,length,thickness,1,1,MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees,0,0,1,1,false,false);
    }
    private void ring(Batch b,float x,float y,float radius,float fraction,float thickness) {
        for(int i=0;i<48*fraction;i++) {
            float a=MathUtils.PI/2+i*MathUtils.PI2/48,c=MathUtils.PI/2+Math.min(i+1,48*fraction)*MathUtils.PI2/48;
            line(b,x+MathUtils.cos(a)*radius,y+MathUtils.sin(a)*radius,(MathUtils.cos(c)-MathUtils.cos(a))*radius,(MathUtils.sin(c)-MathUtils.sin(a))*radius,thickness);
        }
    }
    private void disc(Batch b,float x,float y,float radius) {
        float row=radius/12;
        for(int i=-12;i<12;i++) {
            float yy=(i+.5f)*row,half=(float)Math.sqrt(Math.max(0,radius*radius-yy*yy));
            b.draw(pixel,x-half,y+i*row,half*2,row+.2f);
        }
    }
    private void triangle(Batch b,float x1,float y1,float x2,float y2,float x3,float y3) {
        float color=b.getPackedColor();
        float[] v=triangleVertices;
        v[0]=x1;v[1]=y1;v[5]=x2;v[6]=y2;v[10]=x3;v[11]=y3;v[15]=x3;v[16]=y3;
        for(int i=0;i<20;i+=5){v[i+2]=color;v[i+3]=.5f;v[i+4]=.5f;}
        b.draw(pixel,v,0,20);
    }
    private void sector(Batch b,float x,float y,float r,float fraction) {
        float end=MathUtils.PI2*MathUtils.clamp(fraction,0,1);
        for(int i=0;i<48*fraction;i++) {
            float a=MathUtils.PI/2-i*MathUtils.PI2/48,c=MathUtils.PI/2-Math.min((i+1)*MathUtils.PI2/48,end);
            triangle(b,x,y,x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r,x+MathUtils.cos(c)*r,y+MathUtils.sin(c)*r);
        }
    }
    private void octagonFill(Batch b,float x,float y,float r) {
        for(int i=0;i<8;i++) {
            float a=(i+.5f)*MathUtils.PI/4,c=(i+1.5f)*MathUtils.PI/4;
            triangle(b,x,y,x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r,x+MathUtils.cos(c)*r,y+MathUtils.sin(c)*r);
        }
    }
    private void octagon(Batch b,float x,float y,float r,float thickness) {
        for(int i=0;i<8;i++) {
            float a=(i+.5f)*MathUtils.PI/4,c=(i+1.5f)*MathUtils.PI/4;
            line(b,x+MathUtils.cos(a)*r,y+MathUtils.sin(a)*r,(MathUtils.cos(c)-MathUtils.cos(a))*r,(MathUtils.sin(c)-MathUtils.sin(a))*r,thickness);
        }
    }
    private void bevel(Batch b,float x,float y,float w,float h,boolean fill) {
        if(w<=0||h<=0)return;
        float c=Math.min(7,Math.min(w,h)/3);
        float[] xs={x+c,x+w,x+w,x+w-c,x,x};
        float[] ys={y+h,y+h,y+c,y,y,y+h-c};
        for(int i=0;i<6;i++){
            int j=(i+1)%6;
            if(fill)triangle(b,x+w/2,y+h/2,xs[i],ys[i],xs[j],ys[j]);
            else line(b,xs[i],ys[i],xs[j]-xs[i],ys[j]-ys[i],1);
        }
    }
    private final class CyberPanel extends com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable {
        final Color fill,edge;
        CyberPanel(Color fill,Color edge){this.fill=new Color(fill);this.edge=new Color(edge);}
        @Override public void draw(Batch b,float x,float y,float w,float h){
            float alpha=b.getColor().a;
            b.setColor(fill.r,fill.g,fill.b,fill.a*alpha);bevel(b,x,y,w,h,true);
            b.setColor(edge.r,edge.g,edge.b,edge.a*.55f*alpha);bevel(b,x,y,w,h,false);
            b.setColor(edge.r,edge.g,edge.b,.9f*alpha);b.draw(pixel,x+8,y+h-2,Math.min(28,w/4),2);
            b.setColor(pink.r,pink.g,pink.b,.7f*alpha);b.draw(pixel,x+w-22,y,12,2);
            b.setColor(Color.WHITE);
        }
    }
    private final class HealthCells extends Actor {
        int count=10;float[] fill=new float[10],ghost=new float[10];float flash,previous=10;
        HealthCells(){setTouchable(Touchable.disabled);java.util.Arrays.fill(ghost,1);}
        void updateValues(PlayerVitals life){
            if(fill.length!=count){fill=new float[count];ghost=new float[count];}
            if(life.getHealth()<previous)flash=.3f;
            for(int i=0;i<count;i++){
                fill[i]=life.getCellFill(i);
                if(life.getHealth()>=previous)ghost[i]=Math.max(ghost[i],fill[i]);
            }
            previous=life.getHealth();
        }
        @Override public void act(float dt){
            super.act(dt);hudTime+=dt;flash=Math.max(0,flash-dt);
            if(flash==0)for(int i=0;i<count;i++)ghost[i]=Math.max(fill[i],ghost[i]-dt*2);
        }
        @Override public void draw(Batch b,float alpha){
            float gap=3,w=(getWidth()-gap*(count-1))/count,h=getHeight();
            for(int i=0;i<count;i++){
                float x=getX()+i*(w+gap),y=getY();
                b.setColor(.055f,.045f,.14f,.95f);bevel(b,x,y,w,h,true);
                b.setColor(violet.r,violet.g,violet.b,.55f);bevel(b,x,y,w,h,false);
                if(ghost[i]>fill[i]){b.setColor(pink.r,pink.g,pink.b,.7f);bevel(b,x+3,y+3,(w-6)*ghost[i],h-6,true);}
                if(fill[i]>0){
                    Color cell=previous<=2?pink:cyan;
                    float pulse=previous<=2?.6f+.3f*Math.abs(MathUtils.sin(hudTime*4)):.65f;
                    b.setColor(cell.r,cell.g,cell.b,flash>0?.95f:pulse);bevel(b,x+3,y+3,(w-6)*fill[i],h-6,true);
                    b.setColor(cell.r,cell.g,cell.b,1);line(b,x+5,y+h-5,Math.max(0,(w-10)*fill[i]),0,1.4f);
                }
            }
            b.setColor(Color.WHITE);
        }
    }
    private final class Bar extends Actor {
        float value=1;final Color tint;
        Bar(Color color){tint=new Color(color);setTouchable(Touchable.disabled);}
        @Override public void draw(Batch b,float alpha){
            b.setColor(.1f,.18f,.23f,alpha);b.draw(pixel,getX(),getY(),getWidth(),getHeight());
            b.setColor(tint);b.draw(pixel,getX(),getY(),getWidth()*MathUtils.clamp(value,0,1),getHeight());b.setColor(Color.WHITE);
        }
    }
    private final class Stick extends Actor {
        Stick(){addListener(new InputListener(){
            @Override public boolean touchDown(InputEvent e,float x,float y,int pointer,int button){return touch.beginStick(pointer,(x-getWidth()/2)/(getWidth()*.32f),(y-getHeight()/2)/(getHeight()*.32f));}
            @Override public void touchDragged(InputEvent e,float x,float y,int pointer){touch.dragStick(pointer,(x-getWidth()/2)/(getWidth()*.32f),(y-getHeight()/2)/(getHeight()*.32f));}
            @Override public void touchUp(InputEvent e,float x,float y,int pointer,int button){touch.release(pointer);}
        });}
        @Override public void draw(Batch b,float alpha){
            float x=getX()+getWidth()/2,y=getY()+getHeight()/2,r=getWidth()*.45f;
            b.setColor(.025f,.06f,.09f,opacity*.7f);disc(b,x,y,r);
            b.setColor(cyan.r,cyan.g,cyan.b,opacity);octagon(b,x,y,r,1.4f);ring(b,x,y,r*.47f,1,.7f);
            for(int i=0;i<8;i++) {float a=i*MathUtils.PI/4;line(b,x+MathUtils.cos(a)*(r-6),y+MathUtils.sin(a)*(r-6),MathUtils.cos(a)*3,MathUtils.sin(a)*3,2);}
            float knobX=x+touch.stickX()*r*.62f,knobY=y+touch.stickY()*r*.62f;
            b.setColor(.08f,.18f,.23f,.9f);disc(b,knobX,knobY,22);
            b.setColor(accent);octagon(b,knobX,knobY,22,2);b.setColor(Color.WHITE);
        }
    }
    private final class TouchButton extends Actor {
        final TouchControls.Action action;final GlyphLayout label=new GlyphLayout();
        boolean highlight;float ready=1,remaining,readyPulse;
        @Override public void act(float dt){super.act(dt);readyPulse=Math.max(0,readyPulse-dt);}
        TouchButton(TouchControls.Action action){this.action=action;addListener(new InputListener(){
            @Override public boolean touchDown(InputEvent e,float x,float y,int pointer,int button){return touch.press(action,pointer);}
            @Override public void touchUp(InputEvent e,float x,float y,int pointer,int button){touch.release(pointer);}
        });}
        @Override public Actor hit(float x,float y,boolean touchable){
            if(touchable&&getTouchable()!=Touchable.enabled)return null;
            float dx=x-getWidth()/2,dy=y-getHeight()/2;return dx*dx+dy*dy<=getWidth()*getWidth()/4?this:null;
        }
        @Override public void draw(Batch b,float alpha){
            boolean active=touch.held(action)||highlight;
            float x=getX()+getWidth()/2,y=getY()+getHeight()/2,r=getWidth()/2-2;
            Color key=action==TouchControls.Action.ATTACK?pink:action==TouchControls.Action.PROPULSION?violet:cyan;
            b.setColor(key.r,key.g,key.b,active?.18f:.06f);disc(b,x,y,r+3);
            b.setColor(active?.08f:.018f,active?.13f:.035f,active?.2f:.08f,active?.94f:opacity);
            if(action==TouchControls.Action.DASH&&ready<1)b.setColor(.025f,.20f,.27f,.92f);
            octagonFill(b,x,y,r);
            b.setColor(key.r,key.g,key.b,active?1:.75f);octagon(b,x,y,r,active?2.5f:1.4f);
            line(b,x-r*.45f,y-r*.72f,r*.9f,0,1);
            if(action==TouchControls.Action.DASH&&ready<1){
                b.setColor(.005f,.005f,.025f,.86f);sector(b,x,y,r*.91f,1-ready);
                b.setColor(cyan);ring(b,x,y,r-5,ready,2);
            }
            if(readyPulse>0){b.setColor(cyan.r,cyan.g,cyan.b,readyPulse/.3f);octagon(b,x,y,r+2,3);}
            String name=action==TouchControls.Action.JUMP?"SALTO":action==TouchControls.Action.DASH?"DASH":action==TouchControls.Action.ATTACK?"ATAQUE":action==TouchControls.Action.PROPULSION?"TIRON":"GIRO";
            label.setText(font,name);font.setColor(active?key:Color.WHITE);
            font.draw(b,name,x-label.width/2,y+label.height/2+(action==TouchControls.Action.DASH&&ready<1?5:0));
            if(action==TouchControls.Action.DASH&&ready<1){
                String time=String.format(java.util.Locale.ROOT,"%.1fs",Math.ceil(remaining*10)/10);
                float sx=font.getData().scaleX,sy=font.getData().scaleY;font.getData().setScale(sx*.75f,sy*.75f);
                label.setText(font,time);font.setColor(cyan);font.draw(b,time,x-label.width/2,y-8);
                font.getData().setScale(sx,sy);
            }
            font.setColor(Color.WHITE);b.setColor(Color.WHITE);
        }
    }
}
