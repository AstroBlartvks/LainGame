package com.astro.minor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

/**
 * Компонент для отображения текста
 */
public class Label extends BaseUIComponent {
    private String text;
    private BitmapFont font;
    private Color textColor = Color.WHITE;
    private GlyphLayout layout;
    private int alignment = Align.left;
    private boolean wrapText = false;
    private boolean drawBackground = false;

    public Label(float x, float y, String text) {
        super(x, y, 0, 0);
        this.text = text;
        this.font = FontManager.getInstance().getFont(14);
        this.layout = new GlyphLayout();
        updateTextLayout();
    }

    public Label(float x, float y, String text, int fontSize) {
        super(x, y, 0, 0);
        this.text = text;
        this.font = FontManager.getInstance().getFont(fontSize);
        this.layout = new GlyphLayout();
        updateTextLayout();
    }

    public Label(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        this.text = text;
        this.font = FontManager.getInstance().getFont(14);
        this.layout = new GlyphLayout();
        this.wrapText = true;
        updateTextLayout();
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!visible) return;

        // Отрисовка фона (опционально)
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

        // Отрисовка текста (batch уже открыт)
        font.setColor(textColor);

        float textX = x;
        float textY = y;

        if (wrapText && width > 0) {
            // Текст с переносом
            font.draw(batch, text, x, y + height, width, alignment, true);
        } else {
            // Обычный текст с выравниванием
            switch (alignment) {
                case Align.center:
                    textX = x + (width > 0 ? width : layout.width) / 2 - layout.width / 2;
                    break;
                case Align.right:
                    textX = x + (width > 0 ? width : layout.width) - layout.width;
                    break;
                default:
                    textX = x;
                    break;
            }
            textY = y + layout.height;
            font.draw(batch, text, textX, textY);
        }
        // batch остается открытым
    }

    /**
     * Установка текста
     */
    public void setText(String text) {
        this.text = text;
        updateTextLayout();
    }

    public String getText() {
        return text;
    }

    /**
     * Установка шрифта
     */
    public void setFont(BitmapFont font) {
        this.font = font;
        updateTextLayout();
    }

    public void setFont(int fontSize) {
        this.font = FontManager.getInstance().getFont(fontSize);
        updateTextLayout();
    }

    /**
     * Установка цвета текста
     */
    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setTextColor(float r, float g, float b, float a) {
        this.textColor = new Color(r, g, b, a);
    }

    /**
     * Установка выравнивания текста
     * @param alignment Align.left, Align.center, Align.right
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    /**
     * Включить/выключить перенос текста
     */
    public void setWrapText(boolean wrap) {
        this.wrapText = wrap;
        updateTextLayout();
    }

    /**
     * Включить/выключить отрисовку фона
     */
    public void setDrawBackground(boolean draw) {
        this.drawBackground = draw;
    }

    private void updateTextLayout() {
        if (text != null && !text.isEmpty()) {
            if (wrapText && width > 0) {
                layout.setText(font, text, textColor, width, Align.left, true);
                // Обновляем высоту на основе переноса текста
                this.height = layout.height;
            } else {
                layout.setText(font, text);
                // Авто-размер для простого текста
                if (width == 0) {
                    this.width = layout.width;
                }
                if (height == 0) {
                    this.height = layout.height;
                }
            }
        }
    }

    @Override
    public void dispose() {
        // Шрифты управляются FontManager'ом
    }
}