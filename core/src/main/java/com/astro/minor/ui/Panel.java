package com.astro.minor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;


public class Panel extends BaseUIComponent {
    private Array<UIComponent> components;
    private boolean drawBackground = true;

    public Panel(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.components = new Array<>();
    }

    
    public void addComponent(UIComponent component) {
        components.add(component);
    }

    
    public void removeComponent(UIComponent component) {
        components.removeValue(component, true);
    }

    
    public void clear() {
        components.clear();
    }

    
    public UIComponent getComponent(int index) {
        if (index >= 0 && index < components.size) {
            return components.get(index);
        }
        return null;
    }

    
    public Array<UIComponent> getComponents() {
        return components;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!active || !visible) return;

        for (int i = 0; i < components.size; i++) {
            UIComponent component = components.get(i);
            if (component != null) {
                component.update(delta);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!visible) return;

        if (drawBackground) {
            boolean wasBatchDrawing = batch.isDrawing();
            if (wasBatchDrawing) {
                batch.end();
            }
            renderBackground(shapeRenderer);
            if (!batch.isDrawing()) {
                batch.begin();
            }
        }

        for (int i = 0; i < components.size; i++) {
            UIComponent component = components.get(i);
            if (component != null && component.isVisible()) {
                component.render(batch, shapeRenderer);
            }
        }
    }

    
    public void setDrawBackground(boolean draw) {
        this.drawBackground = draw;
    }

    public boolean isDrawBackground() {
        return drawBackground;
    }

    @Override
    public void dispose() {
        for (UIComponent component : components) {
            if (component != null) {
                component.dispose();
            }
        }
        components.clear();
    }
}