package com.astro.minor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;


public class TextField extends BaseUIComponent {
    private StringBuilder text;
    private String placeholder = "";
    private BitmapFont font;
    private Color textColor = Color.WHITE;
    private Color placeholderColor = new Color(0.5f, 0.5f, 0.5f, 1f);
    private Color focusedBorderColor = new Color(0.3f, 0.6f, 1f, 1f);
    private GlyphLayout layout;
    private boolean focused = false;
    private float cursorBlinkTime = 0;
    private boolean showCursor = true;
    private int maxLength = -1; // -1 = без ограничения
    private float padding = 5f;
    private TextChangeListener changeListener;
    private InputAdapter inputAdapter;

    public interface TextChangeListener {
        void onTextChanged(TextField field, String newText);
    }

    public TextField(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.text = new StringBuilder();
        this.font = FontManager.getInstance().getFont(14);
        this.layout = new GlyphLayout();
        setupInputListener();
    }

    public TextField(float x, float y, float width, float height, String placeholder) {
        super(x, y, width, height);
        this.text = new StringBuilder();
        this.placeholder = placeholder;
        this.font = FontManager.getInstance().getFont(14);
        this.layout = new GlyphLayout();
        setupInputListener();
    }

    private void setupInputListener() {
        inputAdapter = new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!focused || !active) return false;

                if (character == '\b') { // Backspace
                    if (text.length() > 0) {
                        text.deleteCharAt(text.length() - 1);
                        notifyTextChanged();
                    }
                } else if (character == '\n' || character == '\r') {
                    return true;
                } else if (character >= 32 || character == '\t') { // Печатаемые символы
                    if (maxLength < 0 || text.length() < maxLength) {
                        text.append(character);
                        notifyTextChanged();
                    }
                }
                return true;
            }
        };
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!active || !visible) {
            if (focused) {
                setFocused(false);
            }
            return;
        }

        if (Gdx.input.justTouched()) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (contains(mouseX, mouseY)) {
                setFocused(true);
            } else if (focused) {
                setFocused(false);
            }
        }

        if (focused) {
            cursorBlinkTime += delta;
            if (cursorBlinkTime >= 0.5f) {
                showCursor = !showCursor;
                cursorBlinkTime = 0;
            }
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!visible) return;

        boolean wasBatchDrawing = batch.isDrawing();
        if (wasBatchDrawing) {
            batch.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        if (borderWidth > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(focused ? focusedBorderColor : borderColor);
            Gdx.gl.glLineWidth(focused ? borderWidth * 2 : borderWidth);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();
        }

        if (!batch.isDrawing()) {
            batch.begin();
        }


        String displayText = text.toString();
        boolean isEmpty = displayText.isEmpty();

        if (isEmpty && !placeholder.isEmpty()) {
            font.setColor(placeholderColor);
            font.draw(batch, placeholder, x + padding, y + height / 2 + font.getLineHeight() / 2);
        } else {
            font.setColor(textColor);
            layout.setText(font, displayText);

            float textX = x + padding;
            float textY = y + height / 2 + layout.height / 2;

            if (layout.width > width - padding * 2) {
                int visibleChars = displayText.length();
                while (visibleChars > 0) {
                    layout.setText(font, displayText.substring(displayText.length() - visibleChars));
                    if (layout.width <= width - padding * 2 - 10) break;
                    visibleChars--;
                }
                displayText = displayText.substring(displayText.length() - visibleChars);
            }

            font.draw(batch, displayText, textX, textY);

            if (focused && showCursor) {
                layout.setText(font, displayText);
                float cursorX = textX + layout.width + 2;
                float cursorY1 = y + height / 2 - font.getLineHeight() / 2;
                float cursorY2 = y + height / 2 + font.getLineHeight() / 2;

                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(textColor);
                shapeRenderer.rectLine(cursorX, cursorY1, cursorX, cursorY2, 2);
                shapeRenderer.end();
                batch.begin();
            }
        }
    }

    
    public void setFocused(boolean focused) {
        if (this.focused != focused) {
            this.focused = focused;

            if (focused) {
                Gdx.input.setInputProcessor(inputAdapter);
            } else {
                if (Gdx.input.getInputProcessor() == inputAdapter) {
                    Gdx.input.setInputProcessor(null);
                }
            }

            showCursor = focused;
            cursorBlinkTime = 0;
        }
    }

    public boolean isFocused() {
        return focused;
    }

    
    public void setText(String text) {
        this.text = new StringBuilder(text);
        notifyTextChanged();
    }

    public String getText() {
        return text.toString();
    }

    
    public void clear() {
        text.setLength(0);
        notifyTextChanged();
    }

    
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    
    public void setFont(BitmapFont font) {
        this.font = font;
    }

    public void setFont(int fontSize) {
        this.font = FontManager.getInstance().getFont(fontSize);
    }

    
    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setPlaceholderColor(Color color) {
        this.placeholderColor = color;
    }

    public void setFocusedBorderColor(Color color) {
        this.focusedBorderColor = color;
    }

    
    public void setPadding(float padding) {
        this.padding = padding;
    }

    
    public void setChangeListener(TextChangeListener listener) {
        this.changeListener = listener;
    }

    private void notifyTextChanged() {
        if (changeListener != null) {
            changeListener.onTextChanged(this, text.toString());
        }
    }

    @Override
    public void dispose() {
        if (focused && Gdx.input.getInputProcessor() == inputAdapter) {
            Gdx.input.setInputProcessor(null);
        }
    }
}