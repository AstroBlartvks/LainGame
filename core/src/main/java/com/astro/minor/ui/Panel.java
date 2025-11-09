package com.astro.minor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

/**
 * Простой контейнер для группировки UI компонентов
 */
public class Panel extends BaseUIComponent {
    private Array<UIComponent> components;
    private boolean drawBackground = true;

    public Panel(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.components = new Array<>();
    }

    /**
     * Добавление компонента в панель
     */
    public void addComponent(UIComponent component) {
        components.add(component);
    }

    /**
     * Удаление компонента
     */
    public void removeComponent(UIComponent component) {
        components.removeValue(component, true);
    }

    /**
     * Очистка всех компонентов
     */
    public void clear() {
        components.clear();
    }

    /**
     * Получение компонента по индексу
     */
    public UIComponent getComponent(int index) {
        if (index >= 0 && index < components.size) {
            return components.get(index);
        }
        return null;
    }

    /**
     * Получение всех компонентов
     */
    public Array<UIComponent> getComponents() {
        return components;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!active || !visible) return;

        // Используем индексный цикл чтобы избежать nested iterator
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

        // Отрисовка фона
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

        // Отрисовка компонентов
        for (int i = 0; i < components.size; i++) {
            UIComponent component = components.get(i);
            if (component != null && component.isVisible()) {
                component.render(batch, shapeRenderer);
            }
        }
    }

    /**
     * Включить/выключить отрисовку фона
     */
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