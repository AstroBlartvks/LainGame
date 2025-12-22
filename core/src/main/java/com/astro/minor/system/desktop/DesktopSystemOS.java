package com.astro.minor.system.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DesktopSystemOS implements Runnable{
    private final SpriteBatch batch;
    private final Texture backgroundTexture;
    private final MenuBar menuBar;
    private float screenWidth;
    private float screenHeight;
    private final GlyphLayout layout = new GlyphLayout();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final OrthographicCamera camera;

    public DesktopSystemOS(SpriteBatch batch, OrthographicCamera camera) {
        this.batch = batch;
        this.menuBar = new MenuBar(batch, layout, shapeRenderer);
        this.camera = camera;

        if (!Gdx.files.internal("wall.jpg").exists()) {
            System.out.println("[ERROR] Background file not found!");
        }


        backgroundTexture = new Texture(Gdx.files.internal("wall.jpg"));
    }

    public void setSize(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    private void render() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.draw(backgroundTexture, 0, 0, screenWidth, screenHeight);

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
        menuBar.render(screenWidth, screenHeight);
    }

    public void dispose() {
        backgroundTexture.dispose();
    }

    @Override
    public void run() {
        render();
    }
}
