package com.astro.minor.views.apps;

import com.astro.minor.views.IApplication;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public abstract class Application implements IApplication {
    protected int x, y, width, height;
    protected String applicationName;
    protected OrthographicCamera camera;
    protected boolean activeApplication = false;

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

    private boolean isDragging = false;
    private float dragOffsetX = 0;
    private float dragOffsetY = 0;

    public Application(int x, int y, int width, int height, String applicationName, OrthographicCamera camera) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.applicationName = applicationName;
        this.camera = camera;
    }

    public boolean isClosed() { return closed; }

    public void resize(int width, int height) { this.width = width; this.height = height; }

    public void move(int x, int y) { this.x = x; this.y = y; }

    public boolean isActiveApplication() {
        return activeApplication;
    }

    public void run(SpriteBatch batch) {
        if (closed) return;

        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawWindowFrame();
        drawCloseButton();
        shapeRenderer.end();

        handleCloseButtonClick();
        handleDragging();
        updateActiveState();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        fontName.setColor(Color.WHITE);
        float textX = x + 5;
        float textY = y + height + titleBarHeight - 5;
        fontName.draw(batch, applicationName, textX, textY);

        if (activeApplication)
            handleInput();

        render(batch);
    }

    private void drawWindowFrame() {
        shapeRenderer.setColor(borderColor);
        shapeRenderer.rect(x - border, y - border, width + 2*border, height + 2*border + titleBarHeight);

        shapeRenderer.setColor(titleBarColor);
        shapeRenderer.rect(x, y + height, width, titleBarHeight);
    }

    private void drawCloseButton() {
        float closeX = x + width - closeSize - 4;
        float closeY = y + height + (titleBarHeight - closeSize)/2f;

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        float mx = mouse.x;
        float my = mouse.y;

        hoveredClose = mx >= closeX && mx <= closeX + closeSize &&
            my >= closeY && my <= closeY + closeSize;

        shapeRenderer.setColor(hoveredClose ? closeButtonHoverColor : closeButtonColor);
        shapeRenderer.rect(closeX, closeY, closeSize, closeSize);

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.line(closeX + 3, closeY + 3, closeX + closeSize - 3, closeY + closeSize - 3);
        shapeRenderer.line(closeX + 3, closeY + closeSize - 3, closeX + closeSize - 3, closeY + 3);
    }

    private void handleCloseButtonClick() {
        if (Gdx.input.justTouched() && hoveredClose) {
            closed = true;
            shapeRenderer.dispose();
        }
    }

    private void handleDragging() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        float mx = mouse.x;
        float my = mouse.y;

        float titleBarX = x;
        float titleBarY = y + height;
        float closeX = x + width - closeSize - 4;

        boolean isCursorOnTitleBar = mx >= titleBarX && mx <= titleBarX + width &&
                                      my >= titleBarY && my <= titleBarY + titleBarHeight &&
                                      !(mx >= closeX && hoveredClose);

        if (Gdx.input.isTouched()) {
            if (!isDragging && isCursorOnTitleBar && Gdx.input.justTouched()) {
                isDragging = true;
                dragOffsetX = mx - x;
                dragOffsetY = my - y;
            }

            if (isDragging) {
                x = (int)(mx - dragOffsetX);
                y = (int)(my - dragOffsetY);
            }
        } else {
            isDragging = false;
        }
    }

    private void updateActiveState() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        float mx = mouse.x;
        float my = mouse.y;

        float windowMinX = x - border;
        float windowMinY = y - border;
        float windowMaxX = x + width + border;
        float windowMaxY = y + height + titleBarHeight + border;

        activeApplication = mx >= windowMinX && mx <= windowMaxX &&
                           my >= windowMinY && my <= windowMaxY;
    }
}
