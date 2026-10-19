package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuScreen implements Screen {

    private final Game game;
    private Stage stage;
    private Skin skin;

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        // 1. Configurar la resolución virtual de la pantalla (800 x 480 píxeles)
        stage = new Stage(new FitViewport(800, 480));

        // Indicarle a LibGDX que esta pantalla aceptará toques de usuario
        Gdx.input.setInputProcessor(stage);

        // 2. Generar el estilo visual (Colores y Fuentes) por código para no necesitar imágenes externas
        skin = createCustomSkin();

        // 3. Crear una Tabla para organizar los elementos visuales ordenadamente
        Table table = new Table();
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
        TextButton btnJugar = new TextButton("¡INICIAR JUEGO!", skin, "primary_button");
        TextButton btnNiveles = new TextButton("SELECCIONAR NIVEL", skin);
        TextButton btnOpciones = new TextButton("OPCIONES", skin);
        TextButton btnSalir = new TextButton("SALIR DEL JUEGO", skin);

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
                Gdx.app.log("MENU", "Acción: Abrir menú de niveles");
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

        // Fila 1: Título
        table.add(titleLabel).padTop(10).padBottom(5);
        table.row(); // Salto a la siguiente fila

        // Fila 2: Descripción del juego
        table.add(descLabel).padBottom(15);
        table.row();

        // Fila 3: Botón Principal para Iniciar Juego (Destacado y más grande)
        table.add(btnJugar).width(btnWidth).height(52f).padBottom(10);
        table.row();

        // Fila 4: Botón Niveles
        table.add(btnNiveles).width(btnWidth).height(btnHeight).padBottom(8);
        table.row();

        // Fila 5: Botón Opciones
        table.add(btnOpciones).width(btnWidth).height(btnHeight).padBottom(8);
        table.row();

        // Fila 6: Botón Salir
        table.add(btnSalir).width(btnWidth).height(btnHeight).padBottom(10);

        // Agregar la tabla completa al contenedor principal
        stage.addActor(table);
    }

    /**
     * Define los colores, bordes y estilos de texto del diseño sin usar imágenes descargadas.
     */
    private Skin createCustomSkin() {
        Skin skin = new Skin();

        // Configuración de fuentes y tamaños
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.8f); // Tamaño grande para el título

        BitmapFont descFont = new BitmapFont();
        descFont.getData().setScale(0.85f); // Tamaño pequeño para la descripción

        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getData().setScale(1.0f); // Tamaño estándar para botones

        // Estilos de Texto
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.GOLD); // Texto dorado
        Label.LabelStyle descStyle = new Label.LabelStyle(descFont, Color.LIGHT_GRAY); // Texto gris claro
        skin.add("title", titleStyle);
        skin.add("description", descStyle);

        // Creación de colores en código (Pixmaps)
        // 1. Color Verde (Para el botón de Iniciar Juego)
        Pixmap pGreen = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pGreen.setColor(Color.valueOf("#2E7D32"));
        pGreen.fill();
        skin.add("green_bg", new Texture(pGreen));

        // 2. Color Azul (Para botones secundarios)
        Pixmap pBlue = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pBlue.setColor(Color.valueOf("#1565C0"));
        pBlue.fill();
        skin.add("blue_bg", new Texture(pBlue));

        // 3. Color Azul Oscuro (Para cuando presionas un botón)
        Pixmap pDark = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pDark.setColor(Color.valueOf("#0D47A1"));
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
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        // Limpiar memoria cuando se destruya la pantalla
        stage.dispose();
        skin.dispose();
    }
}
