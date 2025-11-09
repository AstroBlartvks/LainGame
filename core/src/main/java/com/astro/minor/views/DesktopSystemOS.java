package com.astro.minor.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DesktopSystemOS implements Runnable{
    private final SpriteBatch batch;
    private final Texture backgroundTexture;
    private final MenuBar menuBar;
    private float screenWidth;
    private float screenHeight;
    private final GlyphLayout layout = new GlyphLayout();

    public DesktopSystemOS(SpriteBatch batch) {
        this.batch = batch;
        this.menuBar = new MenuBar(batch, layout);

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
        batch.draw(backgroundTexture, 0, 52f * screenHeight/1080f, screenWidth, screenHeight - 52f * screenHeight/1080f);
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
