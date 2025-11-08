package com.astro.minor;

import com.astro.minor.views.DesktopSystemOS;
import com.astro.minor.views.apps.Console;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StarField extends ApplicationAdapter {
    static public String osName = "Copland OS";
    static public String version = "Version 1.0.0";

    private SpriteBatch batch;
    private DesktopSystemOS desktopSystemOS;
    private Console console;

    @Override
    public void create() {
        Gdx.graphics.setWindowedMode(1600, 900);
        this.batch = new SpriteBatch();
        this.desktopSystemOS = new DesktopSystemOS(batch);

        this.console = new Console(150, 200, 400, 500, "console_1");
    }

    @Override
    public void render() {
        handleInput();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        desktopSystemOS.render();
        console.run(batch);
        batch.end();
    }


    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1280, 720);
            } else {
                Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
                Gdx.graphics.setFullscreenMode(displayMode);
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        desktopSystemOS.dispose();
    }
}
