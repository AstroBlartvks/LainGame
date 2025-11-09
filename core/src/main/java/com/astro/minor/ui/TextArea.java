package com.astro.minor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

/**
 * Компонент для многострочного ввода текста
 */
public class TextArea extends BaseUIComponent {
    private Array<StringBuilder> lines;
    private BitmapFont font;
    private Color textColor = Color.WHITE;
    private Color focusedBorderColor = new Color(0.3f, 0.6f, 1f, 1f);
    private GlyphLayout layout;
    private boolean focused = false;
    private float cursorBlinkTime = 0;
    private boolean showCursor = true;
    private float padding = 5f;
    private int scrollOffset = 0; // Для прокрутки
    private int maxVisibleLines = 0;
    private InputAdapter inputAdapter;
    private int currentLine = 0; // Текущая линия курсора
    private boolean editable = true;

    public TextArea(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.lines = new Array<>();
        this.lines.add(new StringBuilder());
        this.font = FontManager.getInstance().getFont(14);
        this.layout = new GlyphLayout();
        calculateMaxVisibleLines();
        setupInputListener();
    }

    private void setupInputListener() {
        inputAdapter = new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (!focused || !active || !editable) return false;

                StringBuilder currentLineText = lines.get(currentLine);

                if (character == '\b') { // Backspace
                    if (currentLineText.length() > 0) {
                        currentLineText.deleteCharAt(currentLineText.length() - 1);
                    } else if (currentLine > 0) {
                        // Объединить с предыдущей строкой
                        lines.removeIndex(currentLine);
                        currentLine--;
                    }
                } else if (character == '\n' || character == '\r') {
                    // Новая строка
                    currentLine++;
                    lines.insert(currentLine, new StringBuilder());
                    adjustScroll();
                } else if (character >= 32 || character == '\t') {
                    currentLineText.append(character);
                }
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (!focused || !active) return false;

                scrollOffset += (int) amountY;
                if (scrollOffset < 0) scrollOffset = 0;
                if (scrollOffset > Math.max(0, lines.size - maxVisibleLines)) {
                    scrollOffset = Math.max(0, lines.size - maxVisibleLines);
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

        // Обработка клика для фокуса
        if (Gdx.input.justTouched()) {
            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            if (contains(mouseX, mouseY)) {
                setFocused(true);
            } else if (focused) {
                setFocused(false);
            }
        }

        // Мигание курсора
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

        // Отрисовка фона
        boolean wasBatchDrawing = batch.isDrawing();
        if (wasBatchDrawing) {
            batch.end();
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        // Граница
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

        // Отрисовка текста
        font.setColor(textColor);

        float lineHeight = font.getLineHeight();
        float textY = y + height - padding - lineHeight;

        // Отображаем только видимые строки
        int startLine = scrollOffset;
        int endLine = Math.min(lines.size, scrollOffset + maxVisibleLines);

        for (int i = startLine; i < endLine; i++) {
            String lineText = lines.get(i).toString();
            font.draw(batch, lineText, x + padding, textY);

            // Курсор на текущей строке
            if (focused && showCursor && i == currentLine) {
                layout.setText(font, lineText);
                float cursorX = x + padding + layout.width + 2;

                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(textColor);
                shapeRenderer.rectLine(cursorX, textY - lineHeight + 5, cursorX, textY + 5, 2);
                shapeRenderer.end();
                batch.begin();
            }

            textY -= lineHeight;
        }
        // batch остается открытым
    }

    /**
     * Установка/снятие фокуса
     */
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

    /**
     * Установка текста (поддерживает многострочный текст)
     */
    public void setText(String text) {
        lines.clear();
        String[] textLines = text.split("\n");
        for (String line : textLines) {
            lines.add(new StringBuilder(line));
        }
        if (lines.size == 0) {
            lines.add(new StringBuilder());
        }
        currentLine = lines.size - 1;
        adjustScroll();
    }

    /**
     * Добавление строки текста
     */
    public void appendLine(String line) {
        lines.add(new StringBuilder(line));
        currentLine = lines.size - 1;
        adjustScroll();
    }

    /**
     * Добавление текста к последней строке
     */
    public void appendText(String text) {
        if (lines.size == 0) {
            lines.add(new StringBuilder());
        }
        lines.get(lines.size - 1).append(text);
    }

    /**
     * Получение всего текста
     */
    public String getText() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.size; i++) {
            result.append(lines.get(i).toString());
            if (i < lines.size - 1) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Очистка текста
     */
    public void clear() {
        lines.clear();
        lines.add(new StringBuilder());
        currentLine = 0;
        scrollOffset = 0;
    }

    /**
     * Установка возможности редактирования
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    /**
     * Установка шрифта
     */
    public void setFont(BitmapFont font) {
        this.font = font;
        calculateMaxVisibleLines();
    }

    public void setFont(int fontSize) {
        this.font = FontManager.getInstance().getFont(fontSize);
        calculateMaxVisibleLines();
    }

    /**
     * Установка цвета текста
     */
    public void setTextColor(Color color) {
        this.textColor = color;
    }

    public void setFocusedBorderColor(Color color) {
        this.focusedBorderColor = color;
    }

    /**
     * Установка padding
     */
    public void setPadding(float padding) {
        this.padding = padding;
        calculateMaxVisibleLines();
    }

    /**
     * Прокрутка к последней строке
     */
    public void scrollToBottom() {
        scrollOffset = Math.max(0, lines.size - maxVisibleLines);
    }

    private void calculateMaxVisibleLines() {
        float availableHeight = height - padding * 2;
        maxVisibleLines = (int) (availableHeight / font.getLineHeight());
    }

    private void adjustScroll() {
        // Автоматическая прокрутка к текущей строке
        if (currentLine >= scrollOffset + maxVisibleLines) {
            scrollOffset = currentLine - maxVisibleLines + 1;
        } else if (currentLine < scrollOffset) {
            scrollOffset = currentLine;
        }
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        calculateMaxVisibleLines();
    }

    @Override
    public void dispose() {
        if (focused && Gdx.input.getInputProcessor() == inputAdapter) {
            Gdx.input.setInputProcessor(null);
        }
    }
}