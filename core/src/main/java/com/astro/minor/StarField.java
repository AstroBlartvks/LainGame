package com.astro.minor;

import com.astro.minor.views.DesktopSystemOS;
import com.astro.minor.views.apps.AppExecutor;
import com.astro.minor.views.apps.Console;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StarField extends ApplicationAdapter {
    static public String osName = "Copland OS";
    static public String version = "Version 1.0.0";

    private SpriteBatch batch;
    private static OrthographicCamera uiCamera;
    private static AppExecutor appExecutor;
    private DesktopSystemOS desktopSystemOS;

    public static AppExecutor getAppExecutor() {
        return appExecutor;
    }

    public static OrthographicCamera getUiCamera() {
        return uiCamera;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();

        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        appExecutor = new AppExecutor(batch);

        desktopSystemOS = new DesktopSystemOS(batch);

        Console console1 = new Console(150, 200, 400, 500, "console_1", uiCamera);
        Console console2 = new Console(300, 400, 200, 300, "console_2", uiCamera);

        appExecutor.addApplication(console1);
        appExecutor.addApplication(console2);
    }

    @Override
    public void render() {
        handleInput();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        desktopSystemOS.render(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        uiCamera.update();
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        appExecutor.run();
        batch.end();
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1600, 900);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        desktopSystemOS.dispose();
        appExecutor.dispose();
    }
}
