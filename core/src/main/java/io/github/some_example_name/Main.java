package io.github.some_example_name;

import com.badlogic.gdx.Game;

public class Main extends Game {
    private MenuScreen menu;

    @Override public void create() {
        menu = new MenuScreen(this);
        setScreen(menu);
    }

    @Override public void dispose() {
        if (getScreen() != null && getScreen() != menu) getScreen().dispose();
        if (menu != null) menu.dispose();
    }
}
