package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MenuScreen implements Screen {

    private final Game game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;
    private Image background;
    private Table buttonsTable;
    private Table mainHeading;
    private Table levelsTable;
    private Label levelStatus;
    private boolean levelsOpen;
    private float levelsProgress;


    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        if (stage != null) {
            Gdx.input.setInputProcessor(stage);
            resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            return;
        }
        // El menu adapta su espacio disponible y limita su crecimiento en pantallas grandes.
        stage = new Stage(new ScreenViewport());

        // Indicarle a LibGDX que esta pantalla aceptará toques de usuario
        Gdx.input.setInputProcessor(stage);

        // 2. Generar el estilo visual (Colores y Fuentes) por código para no necesitar imágenes externas
        skin = createCustomSkin();

        createBackground();

        // 3. Crear una Tabla para organizar los elementos visuales ordenadamente
        Table table = new Table();
        buttonsTable = table;
        table.setFillParent(true); // Ocupar toda la pantalla

        // ------------------------------------------------------------------
        // REQUISITO 1: NOMBRE DEL VIDEOJUEGO
        // ------------------------------------------------------------------
        Label titleLabel = new Label("PROYECTO CYBERPUNK", skin, "title");
        titleLabel.setAlignment(Align.center);

        // ------------------------------------------------------------------
        // REQUISITO 2: DESCRIPCIÓN DEL VIDEOJUEGO
        // ------------------------------------------------------------------
        Label descLabel = new Label(
            "DEV. version\n" +
                "0",
            skin, "description"
        );
        descLabel.setAlignment(Align.center);

        // ------------------------------------------------------------------
        // REQUISITO 4: BOTÓN PARA INICIAR + LOS OTROS 3 BOTONES (TOTAL: 4)
        // ------------------------------------------------------------------
        TextButton btnJugar = new BorderButton("¡INICIAR JUEGO!", skin, "primary_button");
        TextButton btnNiveles = new BorderButton("SELECCIONAR NIVEL", skin, "default");
        TextButton btnOpciones = new BorderButton("OPCIONES", skin, "default");
        TextButton btnSalir = new BorderButton("SALIR DEL JUEGO", skin, "default");

        // --- ASIGNAR ACCIONES A CADA BOTÓN ---
        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MENU", "Acción: Iniciar el juego");
                // Cuando tengas tu pantalla de juego lista, la cambias aquí:
                // game.setScreen(new MiPantallaDeJuego(game));
            }
        });

        btnNiveles.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setLevelsOpen(true);
            }
        });

        btnOpciones.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MENU", "Acción: Abrir opciones");
            }
        });

        btnSalir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MENU", "Saliendo del juego...");
                Gdx.app.exit(); // Cierra la aplicación en dispositivos Android
            }
        });

        // ------------------------------------------------------------------
        // REQUISITO 3: DISEÑO VISUAL Y ESTRUCTURA DE LA PANTALLA
        // ------------------------------------------------------------------
        float btnWidth = 280f;
        float btnHeight = 42f;

        Table heading = new Table();
        mainHeading = heading;
        heading.setFillParent(true);
        heading.top().padTop(28);
        heading.add(titleLabel);
        heading.row();
        heading.add(descLabel).padTop(8);
        stage.addActor(heading);
        table.center().padBottom(16f);
        table.add(btnJugar).width(280).height(42).padBottom(8);
        table.row();
        table.add(btnNiveles).width(280).height(42).padBottom(8);
        table.row();
        table.add(btnOpciones).width(280).height(42).padBottom(8);
        table.row();
        table.add(btnSalir).width(280).height(42);
        // Agregar la tabla completa al contenedor principal
        stage.addActor(table);
        createLevelsMenu();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Define los colores, bordes y estilos de texto del diseño sin usar imágenes descargadas.
     */
    /** Fondo retro con encuadre proporcional y sombreado para conservar la legibilidad. */
    private void createBackground() {
        backgroundTexture = new Texture(Gdx.files.internal("fondo_menu.jpg"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        background = new Image(backgroundTexture);
        background.setScaling(Scaling.fill);
        background.setBounds(0, 0, 800, 480);
        background.setTouchable(Touchable.disabled);
        stage.addActor(background);

    }

    private void createLevelsMenu() {
        levelsTable = new Table();
        levelsTable.setVisible(false);
        levelsTable.defaults().pad(7);
        levelsTable.add(new Label("SELECCIONAR NIVEL", skin, "title")).colspan(2).padBottom(16);
        levelsTable.row();
        for (int i = 0; i < 6; i++) {
            boolean testLevel = i == 0;
            String text = testLevel ? "00  NIVEL DE PRUEBAS" : String.format("%02d  PROXIMAMENTE", i);
            BorderButton option = new BorderButton(text, skin, "default");
            if (testLevel) {
                option.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent event, float x, float y) {
                        game.setScreen(new TestLevelScreen(game, MenuScreen.this));
                        Gdx.app.log("NIVELES", "Nivel de pruebas seleccionado");
                    }
                });
            } else {
                option.setDisabled(true);
                option.setTouchable(Touchable.disabled);
                option.getLabel().setColor(Color.GRAY);
            }
            levelsTable.add(option).width(260).height(54);
            if (i % 2 == 1) levelsTable.row();
        }
        levelStatus = new Label("Elige el nivel de pruebas", skin, "description");
        levelsTable.add(levelStatus).colspan(2).padTop(12);
        levelsTable.row();
        BorderButton back = new BorderButton("VOLVER AL MENU", skin, "default");
        back.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                setLevelsOpen(false);
            }
        });
        levelsTable.add(back).colspan(2).width(240).height(42).padTop(12);
        stage.addActor(levelsTable);
    }

    private void setLevelsOpen(boolean open) {
        if (levelsOpen == open) return;
        levelsOpen = open;
        buttonsTable.setTouchable(Touchable.disabled);
        mainHeading.setTouchable(Touchable.disabled);
        levelsTable.setTouchable(Touchable.disabled);
        levelsTable.setVisible(true);
        if (open) levelStatus.setText("Elige el nivel de pruebas");
    }

    private void updateLevelsTransition(float delta) {
        levelsProgress = MathUtils.clamp(levelsProgress + (levelsOpen ? 1 : -1) * delta / 0.55f, 0, 1);
        float progress = Interpolation.smooth.apply(levelsProgress);
        float width = stage.getViewport().getWorldWidth();
        float height = stage.getViewport().getWorldHeight();
        float zoom = 1f + 0.12f * progress;
        background.setBounds((width - width * zoom) / 2f, (height - height * zoom) / 2f,
            width * zoom, height * zoom);
        levelsTable.setBounds(0, -height * (1f - progress), width, height);
        levelsTable.getColor().a = progress;
        levelsTable.setVisible(levelsProgress > 0 || levelsOpen);
        levelsTable.setTouchable(levelsOpen && levelsProgress == 1 ? Touchable.childrenOnly : Touchable.disabled);
        buttonsTable.getColor().a = 1f - progress;
        mainHeading.getColor().a = 1f - progress;
        buttonsTable.setVisible(levelsProgress < 1);
        mainHeading.setVisible(levelsProgress < 1);
        buttonsTable.setTouchable(!levelsOpen && levelsProgress == 0 ? Touchable.childrenOnly : Touchable.disabled);
    }
    private Skin createCustomSkin() {
        Skin skin = new Skin();

        // Configuración de fuentes y tamaños
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.8f); // Tamaño grande para el título

        BitmapFont descFont = new BitmapFont();
        descFont.getData().setScale(0.85f); // Tamaño pequeño para la descripción

        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.0f); // Tamaño estándar para botones

        skin.add("titleFont", titleFont);
        skin.add("descFont", descFont);
        skin.add("buttonFont", buttonFont);
        Pixmap white = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        white.setColor(Color.WHITE);
        white.fill();
        skin.add("border_pixel", new Texture(white));
        white.dispose();
        // Estilos de Texto
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.GOLD); // Texto dorado
        Label.LabelStyle descStyle = new Label.LabelStyle(descFont, Color.LIGHT_GRAY); // Texto gris claro
        skin.add("title", titleStyle);
        skin.add("description", descStyle);

        // Creación de colores en código (Pixmaps)
        // 1. Color Verde (Para el botón de Iniciar Juego)
        Pixmap pGreen = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pGreen.setColor(new Color(0, 0, 0, 0.52f));
        pGreen.fill();
        skin.add("green_bg", new Texture(pGreen));

        // 2. Color Azul (Para botones secundarios)
        Pixmap pBlue = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pBlue.setColor(new Color(0, 0, 0, 0.52f));
        pBlue.fill();
        skin.add("blue_bg", new Texture(pBlue));

        // 3. Color Azul Oscuro (Para cuando presionas un botón)
        Pixmap pDark = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pDark.setColor(new Color(0, 0, 0, 0.70f));
        pDark.fill();
        skin.add("dark_bg", new Texture(pDark));

        // Estilo del Botón de Iniciar Juego (Verde)
        TextButton.TextButtonStyle primaryButtonStyle = new TextButton.TextButtonStyle();
        primaryButtonStyle.up = skin.newDrawable("green_bg");
        primaryButtonStyle.down = skin.newDrawable("dark_bg");
        primaryButtonStyle.font = buttonFont;
        primaryButtonStyle.fontColor = Color.WHITE;
        skin.add("primary_button", primaryButtonStyle);

        // Estilo de los demás Botones (Azul)
        TextButton.TextButtonStyle defaultButtonStyle = new TextButton.TextButtonStyle();
        defaultButtonStyle.up = skin.newDrawable("blue_bg");
        defaultButtonStyle.down = skin.newDrawable("dark_bg");
        defaultButtonStyle.font = buttonFont;
        defaultButtonStyle.fontColor = Color.WHITE;
        skin.add("default", defaultButtonStyle);

        // Liberar recursos temporales de memoria
        pGreen.dispose();
        pBlue.dispose();
        pDark.dispose();

        return skin;
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla y pintar fondo espacial gris/azulado muy oscuro
        Gdx.gl.glClearColor(0.08f, 0.09f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Dibujar el menú
        stage.getViewport().apply();
        updateLevelsTransition(delta);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (stage == null || width <= 0 || height <= 0) return;
        // Encoge en ventanas pequenas; crece como maximo un 25% sobre el tamano base.
        float uiScale = Math.min(1.25f, Math.min(width / 800f, height / 480f));
        ScreenViewport viewport = (ScreenViewport) stage.getViewport();
        viewport.setUnitsPerPixel(1f / uiScale);
        viewport.update(width, height, true);
        // El bloque completo se centra dentro de la mitad inferior de la pantalla.
        if (buttonsTable != null) {
            buttonsTable.padTop(viewport.getWorldHeight() * 0.5f + 12f);
            buttonsTable.invalidateHierarchy();
        }
        if (background != null) {
            background.setBounds(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            if (levelsTable != null) updateLevelsTransition(0);
        }
    }

    private static class BorderButton extends TextButton {
        private final Texture pixel;
        private float progress;

        BorderButton(String text, Skin skin, String style) {
            super(text, skin, style);
            pixel = skin.get("border_pixel", Texture.class);
        }

        @Override public void act(float delta) {
            super.act(delta);
            boolean active = !isDisabled() && (isOver() || isPressed());
            progress = MathUtils.clamp(progress + (active ? 1 : -1) * delta / 0.35f, 0, 1);
        }

        @Override public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            if (progress <= 0) return;
            float savedColor = batch.getPackedColor();
            batch.setColor(1f, 0.42f, 0.06f, getColor().a * parentAlpha);
            float x = getX(), y = getY(), w = getWidth(), h = getHeight();
            float thickness = 2f;
            float distance = Interpolation.smooth.apply(progress) * (h + w) / 2f;
            float vertical = Math.min(h / 2f, distance);
            batch.draw(pixel, x, y + h / 2f - vertical, thickness, vertical * 2);
            batch.draw(pixel, x + w - thickness, y + h / 2f - vertical, thickness, vertical * 2);
            float horizontal = Math.min(w / 2f, Math.max(0, distance - h / 2f));
            if (horizontal > 0) {
                batch.draw(pixel, x, y, horizontal, thickness);
                batch.draw(pixel, x + w - horizontal, y, horizontal, thickness);
                batch.draw(pixel, x, y + h - thickness, horizontal, thickness);
                batch.draw(pixel, x + w - horizontal, y + h - thickness, horizontal, thickness);
            }
            batch.setPackedColor(savedColor);
        }
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        if (Gdx.input.getInputProcessor() == stage) Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        // Limpiar memoria cuando se destruya la pantalla
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();

        skin.dispose();
    }
}
