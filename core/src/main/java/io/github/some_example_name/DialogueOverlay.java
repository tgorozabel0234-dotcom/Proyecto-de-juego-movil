package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Presentacion de las escenas del guion sobre el HUD existente (mismo Stage y viewport).
 * Cubre toda la pantalla para que un toque avance el texto y ningun toque llegue a los
 * controles; no cambia la disposicion de botones ni el area segura del HUD.
 */
public final class DialogueOverlay {
    private final Table root = new Table(), panel = new Table();
    private final Label speaker, body, hint;
    private final Label skip;
    private final Actor bars;
    private final DialoguePlayer player;
    private final TextureRegionDrawable white;
    private final Color cyan = Color.valueOf("15F4E3"), pink = Color.valueOf("FF4FBF");
    private float time;

    public DialogueOverlay(Stage stage, Texture pixel, BitmapFont font, DialoguePlayer player,
                           Runnable advance, Runnable skipScene) {
        this.player = player;
        white = new TextureRegionDrawable(new TextureRegion(pixel));
        Label.LabelStyle strong = new Label.LabelStyle(font, Color.valueOf("ECF5F8"));
        Label.LabelStyle muted = new Label.LabelStyle(font, Color.valueOf("A5BAC9"));
        speaker = new Label("", new Label.LabelStyle(font, cyan));
        body = new Label("", strong);
        body.setWrap(true);
        hint = new Label("TOCA PARA CONTINUAR >", muted);
        skip = new Label("SALTAR >>", muted);
        // Franjas de cine y oscurecido; se dibujan detras del panel.
        bars = new Actor() {
            @Override public void draw(Batch b, float alpha) {
                float h = getStage().getViewport().getWorldHeight(), w = getStage().getViewport().getWorldWidth();
                b.setColor(0, 0, 0, .35f);
                b.draw(pixel, 0, 0, w, h);
                b.setColor(0, 0, 0, .92f);
                b.draw(pixel, 0, h - 34, w, 34);
                b.draw(pixel, 0, 0, w, 34);
                DialoguePlayer.Line line = player.current();
                if (line != null && line.is("glitch")) {
                    // Interferencia VHS del flashback
                    for (int i = 0; i < 7; i++) {
                        b.setColor(MathUtils.randomBoolean() ? .58f : .1f, .2f, MathUtils.randomBoolean() ? 1 : .6f, .12f);
                        b.draw(pixel, MathUtils.random(-40, w), MathUtils.random(0, h), MathUtils.random(60, w * .6f), MathUtils.random(2, 7));
                    }
                }
                b.setColor(Color.WHITE);
            }
        };
        bars.setTouchable(Touchable.disabled);
        root.setFillParent(true);
        root.setTouchable(Touchable.enabled);
        root.addActor(bars);
        root.addListener(new InputListener() {
            @Override public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) { return true; }
            @Override public void touchUp(InputEvent e, float x, float y, int pointer, int button) {
                if (e.getTarget() != skip) advance.run();
            }
        });
        skip.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { skipScene.run(); }
        });
        panel.setBackground(white.tint(new Color(.015f, .025f, .065f, .94f)));
        panel.pad(12, 16, 10, 16);
        panel.add(speaker).left().padBottom(6);
        panel.row();
        panel.add(body).growX().left().top().minHeight(52);
        panel.row();
        panel.add(hint).right().padTop(6);
        root.bottom();
        root.add(skip).expandY().top().right().padTop(8).padRight(16);
        root.row();
        root.add(panel).width(720).pad(0, 24, 44, 24);
        root.setVisible(false);
        stage.addActor(root);
    }

    public boolean isVisible() { return root.isVisible(); }

    public void update(float realDt) {
        boolean active = player.isActive();
        root.setVisible(active);
        if (!active) return;
        root.toFront();
        time += realDt;
        DialoguePlayer.Line line = player.current();
        String who = line.speaker;
        Color accent = cyan;
        // Narracion: solo el nombre del personaje; el color morado la distingue del dialogo.
        if (line.is("narration")) accent = Color.valueOf("9467FF");
        else if (line.is("glitch")) { who = "FLASHBACK"; accent = pink; }
        else if (line.is("action")) { who = ""; accent = Color.valueOf("A5BAC9"); }
        else if (line.is("datapad")) { who = "DATAPAD ENCRIPTADO"; accent = Color.valueOf("36F29B"); }
        else if (line.is("tutorial")) { who = "TUTORIAL"; accent = Color.valueOf("FFE066"); }
        speaker.setText(who);
        speaker.setColor(accent);
        body.setText(player.visibleText());
        body.setColor(line.is("action") ? Color.valueOf("C9D6DF") : line.is("datapad") ? Color.valueOf("8CFFC6") : Color.valueOf("ECF5F8"));
        if (line.is("glitch") && MathUtils.random() < .08f) body.setColor(pink);
        hint.setVisible(player.isLineComplete() && MathUtils.sin(time * 5) > -.3f);
        float worldWidth = root.getStage() == null ? Gdx.graphics.getWidth() : root.getStage().getViewport().getWorldWidth();
        root.getCell(panel).width(Math.min(720, worldWidth - 48));
        root.invalidateHierarchy();
    }
}
