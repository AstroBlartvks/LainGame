package com.astro.minor.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Базовый интерфейс для всех UI компонентов
 */
public interface UIComponent {

    /**
     * Отрисовка компонента
     * @param batch SpriteBatch для отрисовки текстур и текста
     * @param shapeRenderer ShapeRenderer для отрисовки фигур
     */
    void render(SpriteBatch batch, ShapeRenderer shapeRenderer);

    /**
     * Обновление состояния компонента
     * @param delta Время с прошлого кадра
     */
    void update(float delta);

    /**
     * Проверка, активен ли компонент
     * @return true если компонент активен
     */
    boolean isActive();

    /**
     * Установка активности компонента
     * @param active Активен ли компонент
     */
    void setActive(boolean active);

    /**
     * Проверка, находится ли курсор над компонентом
     * @return true если курсор над компонентом
     */
    boolean isHover();

    /**
     * Проверка, находится ли точка внутри компонента
     * @param x Координата X
     * @param y Координата Y
     * @return true если точка внутри компонента
     */
    boolean contains(float x, float y);

    /**
     * Получение позиции X
     * @return Позиция X
     */
    float getX();

    /**
     * Получение позиции Y
     * @return Позиция Y
     */
    float getY();

    /**
     * Получение ширины
     * @return Ширина компонента
     */
    float getWidth();

    /**
     * Получение высоты
     * @return Высота компонента
     */
    float getHeight();

    /**
     * Установка позиции
     * @param x Координата X
     * @param y Координата Y
     */
    void setPosition(float x, float y);

    /**
     * Установка размера
     * @param width Ширина
     * @param height Высота
     */
    void setSize(float width, float height);

    /**
     * Установка видимости
     * @param visible Видимость компонента
     */
    void setVisible(boolean visible);

    /**
     * Проверка видимости
     * @return true если компонент видим
     */
    boolean isVisible();

    /**
     * Освобождение ресурсов
     */
    void dispose();
}
