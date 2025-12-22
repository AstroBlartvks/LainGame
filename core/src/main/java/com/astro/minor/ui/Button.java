package com.astro.minor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public class Button extends BaseUIComponent {
    private String text;
    private BitmapFont font;
    private Color textColor = Color.WHITE;
    private Color pressedColor = new Color(0.15f, 0.15f, 0.15f, 1f);
    private Texture icon;
    private float iconWidth = 0;
    private float iconHeight = 0;
    private float iconPadding = 5f;
    private GlyphLayout layout;
    private boolean pressed = false;
    private boolean wasPressed = false;
    private ClickListener clickListener;
    public boolean isIconButton = false;

    public interface ClickListener {
        void onClick(Button button);
    }

    public Button(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        this.text = text;
        this.font = FontManager.getInstance().getFont(16);
        this.layout = new GlyphLayout();
        updateTextLayout();
    }

    public Button(float x, float y, float width, float height, String text, int fontSize) {
        super(x, y, width, height);
        this.text = text;
        this.font = FontManager.getInstance().getFont(fontSize);
        this.layout = new GlyphLayout();
        updateTextLayout();
    }

    public void setIconButton(boolean isIconButton) {
        this.isIconButton = isIconButton;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!active || !visible) {
            pressed = false;
            return;
        }

        if (hover && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            pressed = true;
        } else {
            if (pressed && !Gdx.input.isButtonPressed(Input.Buttons.LEFT) && hover) {
                handleClick();
            }
            pressed = false;
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!visible) return;

        boolean wasBatchDrawing = batch.isDrawing();
        if (wasBatchDrawing) {
            batch.end();
        }

        if (!isIconButton) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            if (pressed) {
                shapeRenderer.setColor(pressedColor);
            } else if (hover) {
                shapeRenderer.setColor(hoverColor);
            } else {
                shapeRenderer.setColor(backgroundColor);
            }
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

        if (!batch.isDrawing()) {
            batch.begin();
        }


        float contentX = x;
        float totalContentWidth = 0;

        if (icon != null) {
            totalContentWidth += iconWidth + iconPadding;
        }
        if (text != null && !text.isEmpty()) {
            totalContentWidth += layout.width;
        }

        contentX = x + (width - totalContentWidth) / 2;

        if (icon != null) {
            float iconY = y + (height - iconHeight) / 2;
            batch.draw(icon, contentX, iconY, iconWidth, iconHeight);
            contentX += iconWidth + iconPadding;
        }

        if (!isIconButton) {
            if (text != null && !text.isEmpty()) {
                font.setColor(textColor);
                float textY = y + (height + layout.height) / 2;
                font.draw(batch, text, contentX, textY);
            }
        }
    }

    
    public void handleClick() {
        if (clickListener != null) {
            clickListener.onClick(this);
        }
    }

    
    public void setClickListener(ClickListener listener) {
        this.clickListener = listener;
    }

    
    public void setText(String text) {
        this.text = text;
        updateTextLayout();
    }

    public String getText() {
        return text;
    }

    
    public void setBackgroundIcon(Texture icon, float width, float height) {
        this.icon = icon;
        this.iconWidth = width;
        this.iconHeight = height;
    }

    public void setBackgroundIcon(Texture icon) {
        this.icon = icon;
        if (icon != null) {
            this.iconWidth = icon.getWidth();
            this.iconHeight = icon.getHeight();
        }
    }

    public void setIconSize(float width, float height) {
        this.iconWidth = width;
        this.iconHeight = height;
    }

    public void setIconPadding(float padding) {
        this.iconPadding = padding;
    }

    
    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setTextColor(float r, float g, float b, float a) {
        this.textColor = new Color(r, g, b, a);
    }

    
    public void setPressedColor(Color color) {
        this.pressedColor = color;
    }

    public void setPressedColor(float r, float g, float b, float a) {
        this.pressedColor = new Color(r, g, b, a);
    }

    
    public void setFont(BitmapFont font) {
        this.font = font;
        updateTextLayout();
    }

    public void setFont(int fontSize) {
        this.font = FontManager.getInstance().getFont(fontSize);
        updateTextLayout();
    }

    private void updateTextLayout() {
        if (text != null && !text.isEmpty()) {
            layout.setText(font, text);
        }
    }

    @Override
    public void dispose() {
        if (icon != null) {
        }
    }
}
