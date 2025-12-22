package com.astro.minor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;


public class Label extends BaseUIComponent {
    private String text;
    private BitmapFont font;
    private Color textColor = Color.WHITE;
    private GlyphLayout layout;
    private int alignment = Align.left;
    private boolean wrapText = false;
    private boolean drawBackground = false;
    private float padding = 0f;

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

        font.setColor(textColor);

        float textX = x;
        float textY = y;

        if (wrapText && width > 0) {
            font.draw(batch, text, x + padding, y + height - padding, width, alignment, true);
        } else {
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
            font.draw(batch, text, textX + padding, textY - padding);
        }
    }

    
    public void setText(String text) {
        this.text = text;
        updateTextLayout();
    }

    public String getText() {
        return text;
    }

    
    public void setFont(BitmapFont font) {
        this.font = font;
        updateTextLayout();
    }

    public void setFont(int fontSize) {
        this.font = FontManager.getInstance().getFont(fontSize);
        updateTextLayout();
    }

    
    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setTextColor(float r, float g, float b, float a) {
        this.textColor = new Color(r, g, b, a);
    }

    
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    
    public void setWrapText(boolean wrap) {
        this.wrapText = wrap;
        updateTextLayout();
    }

    
    public void setDrawBackground(boolean draw) {
        this.drawBackground = draw;
    }

    private void updateTextLayout() {
        if (text != null && !text.isEmpty()) {
            if (wrapText && width > 0) {
                layout.setText(font, text, textColor, width, Align.left, true);
                this.height = layout.height;
            } else {
                layout.setText(font, text);
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
    }

    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }
}
