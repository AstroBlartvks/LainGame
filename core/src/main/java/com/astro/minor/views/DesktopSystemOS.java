package com.astro.minor.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DesktopSystemOS {
    private final SpriteBatch batch;
    private final Texture backgroundTexture;
    private final Texture menubarTexture;

    public DesktopSystemOS(SpriteBatch batch) {
        this.batch = batch;

        if (!Gdx.files.internal("wall.jpg").exists()) {
            System.out.println("[ERROR] Background file not found!");
        }

        backgroundTexture = new Texture(Gdx.files.internal("wall.jpg"));
        menubarTexture = new Texture(Gdx.files.internal("menubar.png"));
    }

    public void render(float screenWidth, float screenHeight) {
        batch.draw(backgroundTexture, 0, 52f * screenHeight/1080f, screenWidth, screenHeight - 52f * screenHeight/1080f);
        batch.draw(menubarTexture, 0, 0, screenWidth, 52f * screenHeight/1080f);
    }

    public void dispose() {
        backgroundTexture.dispose();
    }
}
