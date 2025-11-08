package com.astro.minor.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DesktopSystemOS {
    private final SpriteBatch batch;
    private final Texture backgroundTexture;

    public DesktopSystemOS(SpriteBatch batch) {
        this.batch = batch;

        if (!Gdx.files.internal("computer/root/lain/system/wall.jpg").exists()) {
            System.out.println("[ERROR] Background file not found!");
        }

        backgroundTexture = new Texture(Gdx.files.internal("computer/root/lain/system/wall.jpg"));
    }

    public void render(float screenWidth, float screenHeight) {
        batch.draw(backgroundTexture, 0, 0, screenWidth, screenHeight);
    }

    public void dispose() {
        backgroundTexture.dispose();
    }
}
