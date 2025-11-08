package com.astro.minor.views;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IApplication {
    void handleInput();
    void render(SpriteBatch batch);
    void dispose();
}
