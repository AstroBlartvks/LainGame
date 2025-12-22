package com.astro.minor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;


public abstract class BaseUIComponent implements UIComponent {
    protected float x, y;
    protected float width, height;
    protected boolean active = true;
    protected boolean visible = true;
    protected boolean hover = false;
    protected Rectangle bounds;
    protected Color backgroundColor = new Color(0.2f, 0.2f, 0.2f, 1f);
    protected Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1f);
    protected Color hoverColor = new Color(0.3f, 0.3f, 0.3f, 1f);
    protected float borderWidth = 1f;

    public BaseUIComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
    }

    @Override
    public void update(float delta) {
        if (!active || !visible) {
            hover = false;
            return;
        }

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        hover = contains(mouseX, mouseY);
    }

    @Override
    public boolean contains(float px, float py) {
        return bounds.contains(px, py);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isHover() {
        return hover;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.setPosition(x, y);
    }

    @Override
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        bounds.setSize(width, height);
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        this.backgroundColor = new Color(r, g, b, a);
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
    }

    public void setBorderColor(float r, float g, float b, float a) {
        this.borderColor = new Color(r, g, b, a);
    }

    public void setHoverColor(Color color) {
        this.hoverColor = color;
    }

    public void setHoverColor(float r, float g, float b, float a) {
        this.hoverColor = new Color(r, g, b, a);
    }

    public void setBorderWidth(float width) {
        this.borderWidth = width;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public Color getHoverColor() {
        return hoverColor;
    }

    
    protected void renderBackground(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hover ? hoverColor : backgroundColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        if (borderWidth > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(borderColor);
            Gdx.gl.glLineWidth(borderWidth);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();
        }
    }

    @Override
    public void dispose() {
    }
}