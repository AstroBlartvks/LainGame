package com.astro.minor.views.apps;

import com.astro.minor.views.IApplication;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import static java.lang.Math.abs;

public abstract class Application implements IApplication {
    protected int x, y, width, height;
    protected String applicationName;

    private final Color borderColor = new Color(0.4f, 0.4f, 0.4f, 1f);
    private final Color titleBarColor = new Color(0.3f, 0.3f, 0.3f, 1f);
    private final Color closeButtonColor = new Color(0.8f, 0.2f, 0.2f, 1f);
    private final Color closeButtonHoverColor = new Color(1f, 0.3f, 0.3f, 1f);
    private final BitmapFont fontName = new BitmapFont();

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final int border = 3;
    private final int titleBarHeight = 20;
    private final int closeSize = 14;

    private boolean closed = false;
    private boolean hoveredClose = false;

    public Application(int x, int y, int width, int height, String applicationName) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.applicationName = applicationName;
    }

    public boolean isClosed() {
        return closed;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void run(SpriteBatch batch) {
        if (closed) return;

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawWindowFrame();
        drawCloseButton();
        shapeRenderer.end();

        handleCloseButtonClick();

        batch.begin();

        fontName.setColor(Color.WHITE);
        float textX = x + 5;
        float textY = y + height + titleBarHeight - 5;
        fontName.draw(batch, applicationName, textX, textY);

        handleInput();
        render(batch);

    }

    private void drawWindowFrame() {
        shapeRenderer.setColor(borderColor);
        shapeRenderer.rect(x - border, y - border,
            width + 2 * border, height + 2 * border + titleBarHeight);

        shapeRenderer.setColor(titleBarColor);
        shapeRenderer.rect(x, y + height, width, titleBarHeight);
    }

    private void drawCloseButton() {
        float closeX = x + width - closeSize - 4;
        float closeY = y + height + (titleBarHeight - closeSize) / 2f;

        float mx = Gdx.input.getX();
        float my = Gdx.input.getY();
        float diffX = abs(mx - closeX - (float) closeSize /2);
        float diffY = abs(my - (Gdx.graphics.getHeight() - closeY) + (float) closeSize /2);
        hoveredClose = (diffX < closeSize/2f && diffY < closeSize/2f);
        System.out.println("Hovered: " + hoveredClose + " dx" + diffX + " dy" + diffY);

        shapeRenderer.setColor(hoveredClose ? closeButtonHoverColor : closeButtonColor);
        shapeRenderer.rect(closeX, closeY, closeSize, closeSize);

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.line(closeX + 3, closeY + 3,
            closeX + closeSize - 3, closeY + closeSize - 3);
        shapeRenderer.line(closeX + 3, closeY + closeSize - 3,
            closeX + closeSize - 3, closeY + 3);
    }

    private void handleCloseButtonClick() {
        if (Gdx.input.justTouched() && hoveredClose) {
            closed = true;
            shapeRenderer.dispose();
        }
    }
}
