package com.astro.minor.system.apps.base;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IApplication {
    void handleInput();
    void render(SpriteBatch batch);
    void dispose();
}
