package com.astro.minor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;


public class Grid extends BaseUIComponent {
    private Array<UIComponent> components;
    private int columns;
    private int rows;
    private float cellWidth;
    private float cellHeight;
    private float spacing = 5f;
    private boolean autoResize = true;
    private boolean drawBackground = false;

    public Grid(float x, float y, float width, float height, int columns, int rows) {
        super(x, y, width, height);
        this.columns = columns;
        this.rows = rows;
        this.components = new Array<>();
        calculateCellSize();
    }

    
    public void addComponent(UIComponent component) {
        if (components.size >= columns * rows) {
            System.err.println("Grid is full! Cannot add more components.");
            return;
        }
        components.add(component);
        layoutComponents();
    }

    
    public void addComponent(UIComponent component, int column, int row) {
        int index = row * columns + column;

        if (index >= columns * rows) {
            System.err.println("Grid position out of bounds!");
            return;
        }

        while (components.size <= index) {
            components.add(null);
        }

        components.set(index, component);
        layoutComponent(component, column, row);
    }

    
    public void removeComponent(UIComponent component) {
        components.removeValue(component, true);
        layoutComponents();
    }

    
    public void clear() {
        components.clear();
    }

    
    public UIComponent getComponent(int column, int row) {
        int index = row * columns + column;
        if (index >= 0 && index < components.size) {
            return components.get(index);
        }
        return null;
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
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (index < components.size) {
                    UIComponent component = components.get(index);
                    if (component != null) {
                        layoutComponent(component, col, row);
                    }
                    index++;
                }
            }
        }
    }

    private void layoutComponent(UIComponent component, int column, int row) {
        float componentX = x + column * (cellWidth + spacing);
        float componentY = y + height - row * (cellHeight + spacing) - cellHeight;

        component.setPosition(componentX, componentY);

        if (autoResize) {
            component.setSize(cellWidth, cellHeight);
        }
    }

    
    private void calculateCellSize() {
        cellWidth = (width - spacing * (columns - 1)) / columns;
        cellHeight = (height - spacing * (rows - 1)) / rows;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        calculateCellSize();
        layoutComponents();
    }

    
    public void setGridSize(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        calculateCellSize();
        layoutComponents();
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    
    public void setSpacing(float spacing) {
        this.spacing = spacing;
        calculateCellSize();
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

    
    public Array<UIComponent> getComponents() {
        return components;
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