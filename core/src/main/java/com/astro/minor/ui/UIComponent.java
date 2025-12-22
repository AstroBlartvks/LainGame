package com.astro.minor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public interface UIComponent {

    
    void render(SpriteBatch batch, ShapeRenderer shapeRenderer);

    
    void update(float delta);

    
    boolean isActive();

    
    void setActive(boolean active);

    
    boolean isHover();

    
    boolean contains(float x, float y);

    
    float getX();

    
    float getY();

    
    float getWidth();

    
    float getHeight();

    
    void setPosition(float x, float y);

    
    void setSize(float width, float height);

    
    void setVisible(boolean visible);

    
    boolean isVisible();

    
    void dispose();
}
