package com.astro.minor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;


public class VBox extends BaseUIComponent {
    private Array<UIComponent> components;
    private float spacing = 5f;
    private boolean autoResize = true;
    private boolean drawBackground = false;

    public VBox(float x, float y, float width) {
        super(x, y, width, 0);
        this.components = new Array<>();
    }

    public VBox(float x, float y, float width, float spacing) {
        super(x, y, width, 0);
        this.spacing = spacing;
        this.components = new Array<>();
    }

    
    public void addComponent(UIComponent component) {
        components.add(component);
        layoutComponents();
    }

    
    public void removeComponent(UIComponent component) {
        components.removeValue(component, true);
        layoutComponents();
    }

    
    public void clear() {
        components.clear();
        height = 0;
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

    
    private void layoutComponents() {
        float currentY = y + height;
        float totalHeight = 0;

        for (UIComponent component : components) {
            if (component != null) {
                totalHeight += component.getHeight();
            }
        }
        totalHeight += spacing * Math.max(0, components.size - 1);

        this.height = totalHeight;
        currentY = y + height;

        for (UIComponent component : components) {
            if (component != null) {
                currentY -= component.getHeight();
                component.setPosition(x, currentY);

                if (autoResize) {
                    component.setSize(width, component.getHeight());
                }

                currentY -= spacing;
            }
        }

        bounds.setSize(width, height);
    }

    
    public void setSpacing(float spacing) {
        this.spacing = spacing;
        layoutComponents();
    }

    public float getSpacing() {
        return spacing;
    }

    
    public void setAutoResize(boolean autoResize) {
        this.autoResize = autoResize;
        if (autoResize) {
            layoutComponents();
        }
    }

    public boolean isAutoResize() {
        return autoResize;
    }

    
    public void setDrawBackground(boolean draw) {
        this.drawBackground = draw;
    }

    @Override
    public void setSize(float width, float height) {
        this.width = width;
        layoutComponents();
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