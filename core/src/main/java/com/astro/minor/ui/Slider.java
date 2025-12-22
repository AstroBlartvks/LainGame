package com.astro.minor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Slider extends BaseUIComponent {
    private float value = 0f;
    private float minValue = 0f;
    private float maxValue = 1f;
    private boolean isDragging = false;
    private Color trackColor = new Color(0.0f, 0.02f, 0.08f, 0.85f);
    private Color fillColor = new Color(0.0f, 0.4f, 0.3f, 0.9f);
    private Color thumbColor = new Color(0.3f, 0.9f, 0.9f, 1f);
    private Color thumbHoverColor = new Color(0.5f, 1f, 1f, 1f);
    private float thumbRadius = 8f;
    private float trackHeight = 4f;
    private ChangeListener changeListener;

    public interface ChangeListener {
        void onChange(Slider slider, float value);
    }

    public Slider(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public Slider(float x, float y, float width, float height, float minValue, float maxValue) {
        super(x, y, width, height);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if (!active || !visible) {
            isDragging = false;
            return;
        }

        float thumbX = getThumbX();
        float thumbY = y + height / 2;

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        boolean thumbHover = Math.abs(mouseX - thumbX) <= thumbRadius &&
                            Math.abs(mouseY - thumbY) <= thumbRadius;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!isDragging && (thumbHover || hover)) {
                isDragging = true;
            }

            if (isDragging) {
                float normalizedX = (mouseX - x) / width;
                normalizedX = Math.max(0, Math.min(1, normalizedX));
                float newValue = minValue + normalizedX * (maxValue - minValue);

                if (newValue != value) {
                    setValue(newValue);
                    if (changeListener != null) {
                        changeListener.onChange(this, value);
                    }
                }
            }
        } else {
            isDragging = false;
        }
    }

    @Override
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        if (!visible) return;

        boolean wasBatchDrawing = batch.isDrawing();
        if (wasBatchDrawing) {
            batch.end();
        }

        float trackY = y + height / 2 - trackHeight / 2;
        float thumbX = getThumbX();
        float thumbY = y + height / 2;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(trackColor);
        shapeRenderer.rect(x, trackY, width, trackHeight);

        float normalizedValue = (value - minValue) / (maxValue - minValue);
        float fillWidth = width * normalizedValue;
        shapeRenderer.setColor(fillColor);
        shapeRenderer.rect(x, trackY, fillWidth, trackHeight);

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean thumbHover = Math.abs(mouseX - thumbX) <= thumbRadius &&
                            Math.abs(mouseY - thumbY) <= thumbRadius;

        shapeRenderer.setColor(thumbHover || isDragging ? thumbHoverColor : thumbColor);
        shapeRenderer.circle(thumbX, thumbY, thumbRadius);

        shapeRenderer.end();

        if (!batch.isDrawing()) {
            batch.begin();
        }
    }

    private float getThumbX() {
        float normalizedValue = (value - minValue) / (maxValue - minValue);
        return x + normalizedValue * width;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = Math.max(minValue, Math.min(maxValue, value));
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public void setTrackColor(Color color) {
        this.trackColor = color;
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
    }

    public void setThumbColor(Color color) {
        this.thumbColor = color;
    }

    public void setThumbHoverColor(Color color) {
        this.thumbHoverColor = color;
    }

    public void setThumbRadius(float radius) {
        this.thumbRadius = radius;
    }

    public void setTrackHeight(float height) {
        this.trackHeight = height;
    }

    public void setChangeListener(ChangeListener listener) {
        this.changeListener = listener;
    }

    public boolean isDragging() {
        return isDragging;
    }

    @Override
    public void dispose() {
    }
}
