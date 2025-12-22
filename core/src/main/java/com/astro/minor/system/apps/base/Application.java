package com.astro.minor.system.apps.base;

import com.astro.minor.system.apps.base.IApplication;
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

    private final Color borderColor = new Color(0.56f, 0.61f, 0.8f, 0.8f);
    private final Color titleBarColor = new Color(0.42f, 0.46f, 0.6f, 0.8f);
    private final Color closeButtonColor = new Color(0.8f, 0.2f, 0.2f, 1f);
    private final Color closeButtonHoverColor = new Color(1f, 0.3f, 0.3f, 1f);
    private final BitmapFont fontName = new BitmapFont();

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private final int border = 3;
    private final int titleBarHeight = 20;
    private final int closeSize = 14;
    private final int resizeMargin = 8;
    private final int minWidth = 100;
    private final int minHeight = 80;
    private boolean isResizable = true;

    private boolean closed = false;
    private boolean hoveredClose = false;

    private boolean isDragging = false;
    private float dragOffsetX = 0;
    private float dragOffsetY = 0;

    private boolean isResizing = false;
    private ResizeEdge resizeEdge = ResizeEdge.NONE;
    private int resizeStartX, resizeStartY;
    private int resizeStartWidth, resizeStartHeight;
    private int resizeStartWindowX, resizeStartWindowY;

    private enum ResizeEdge {
        NONE, LEFT, RIGHT, TOP, BOTTOM,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public Application(int x, int y, int width, int height, String applicationName, OrthographicCamera camera) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.applicationName = applicationName;
        this.camera = camera;
    }

    public boolean isActive() {
        return activeApplication;
    }
    
    public void setActiveApplication(boolean active) {
        this.activeApplication = active;
    }
    
    public OrthographicCamera getCamera() {
        return camera;
    }

    public void setResizable(boolean resizable) {
        isResizable = resizable;
    }

    public boolean isClosed() { return closed; }
    
    public void setClosed(boolean closed) { 
        this.closed = closed;
        if (closed) {
            shapeRenderer.dispose();
        }
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

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
        handleResizing();
        handleDragging();

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        fontName.setColor(Color.WHITE);
        float textX = x + 5;
        float textY = y + height + titleBarHeight - 5;
        fontName.draw(batch, applicationName, textX, textY);

        if (activeApplication) {
            handleInput();
        } else {
            if (Gdx.input.justTouched()) {
                com.badlogic.gdx.Gdx.app.log("Application", applicationName + " NOT ACTIVE - click ignored");
            }
        }

        render(batch);
    }

    private void drawWindowFrame() {
        shapeRenderer.setColor(borderColor);
        shapeRenderer.rect(x - border, y - border, width + 2*border, height + 2*border + titleBarHeight);

        shapeRenderer.setColor(titleBarColor);
        shapeRenderer.rect(x, y + height, width, titleBarHeight);

        drawResizeHandles();
    }

    private void drawResizeHandles() {
        if (!isResizable) return;

        int handleSize = 10;

        shapeRenderer.setColor(Color.LIGHT_GRAY);

        float bottomLeftX = x - handleSize/2f;
        float bottomLeftY = y - handleSize/2f;
        shapeRenderer.triangle(
            bottomLeftX, bottomLeftY,
            bottomLeftX + handleSize, bottomLeftY,
            bottomLeftX, bottomLeftY + handleSize
        );

        float bottomRightX = x + width + handleSize/2f;
        float bottomRightY = y - handleSize/2f;
        shapeRenderer.triangle(
            bottomRightX, bottomRightY,
            bottomRightX - handleSize, bottomRightY,
            bottomRightX, bottomRightY + handleSize
        );
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
        if (isResizing) return;

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

    private void handleResizing() {
        if (isDragging) return;
        if (!isResizable) return;

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);
        float mx = mouse.x;
        float my = mouse.y;

        if (!isResizing) {
            resizeEdge = getResizeEdge(mx, my);
        }

        if (Gdx.input.isTouched()) {
            if (!isResizing && resizeEdge != ResizeEdge.NONE && Gdx.input.justTouched()) {
                isResizing = true;
                resizeStartX = (int)mx;
                resizeStartY = (int)my;
                resizeStartWidth = width;
                resizeStartHeight = height;
                resizeStartWindowX = x;
                resizeStartWindowY = y;
            }

            if (isResizing) {
                int deltaX = (int)(mx - resizeStartX);
                int deltaY = (int)(my - resizeStartY);

                switch (resizeEdge) {
                    case RIGHT:
                        width = Math.max(minWidth, resizeStartWidth + deltaX);
                        break;
                    case LEFT:
                        int rightEdge = resizeStartWindowX + resizeStartWidth;
                        int newX = Math.min(resizeStartWindowX + deltaX, rightEdge - minWidth);
                        width = rightEdge - newX;
                        x = newX;
                        break;
                    case BOTTOM:
                        int topEdge = resizeStartWindowY + resizeStartHeight;
                        int newY = Math.min(resizeStartWindowY + deltaY, topEdge - minHeight);
                        height = topEdge - newY;
                        y = newY;
                        break;
                    case TOP:
                        height = Math.max(minHeight, resizeStartHeight + deltaY);
                        break;
                    case BOTTOM_RIGHT:
                        width = Math.max(minWidth, resizeStartWidth + deltaX);
                        int topEdgeBR = resizeStartWindowY + resizeStartHeight;
                        int newYBR = Math.min(resizeStartWindowY + deltaY, topEdgeBR - minHeight);
                        height = topEdgeBR - newYBR;
                        y = newYBR;
                        break;
                    case BOTTOM_LEFT:
                        int rightEdgeBL = resizeStartWindowX + resizeStartWidth;
                        int newXBL = Math.min(resizeStartWindowX + deltaX, rightEdgeBL - minWidth);
                        width = rightEdgeBL - newXBL;
                        x = newXBL;
                        int topEdgeBL = resizeStartWindowY + resizeStartHeight;
                        int newYBL = Math.min(resizeStartWindowY + deltaY, topEdgeBL - minHeight);
                        height = topEdgeBL - newYBL;
                        y = newYBL;
                        break;
                    case TOP_RIGHT:
                        width = Math.max(minWidth, resizeStartWidth + deltaX);
                        height = Math.max(minHeight, resizeStartHeight + deltaY);
                        break;
                    case TOP_LEFT:
                        int rightEdgeTL = resizeStartWindowX + resizeStartWidth;
                        int newXTL = Math.min(resizeStartWindowX + deltaX, rightEdgeTL - minWidth);
                        width = rightEdgeTL - newXTL;
                        x = newXTL;
                        height = Math.max(minHeight, resizeStartHeight + deltaY);
                        break;
                }
            }
        } else {
            isResizing = false;
        }
    }

    private ResizeEdge getResizeEdge(float mx, float my) {
        int handleSize = 15;

        boolean onBottomLeft = mx >= x-handleSize/2f && mx <= (x + handleSize-handleSize/2f) &&
                               my >= y-handleSize/2f && my <= (y + handleSize-handleSize/2f);

        boolean onBottomRight = mx >= (x + width - handleSize + handleSize/2f) && mx <= (x + width + handleSize/2f) &&
                                my >= y-handleSize/2f && my <= (y + handleSize-handleSize/2f);

        if (onBottomLeft) return ResizeEdge.BOTTOM_LEFT;
        if (onBottomRight) return ResizeEdge.BOTTOM_RIGHT;

        return ResizeEdge.NONE;
    }

    public void onScroll(float amountX, float amountY) {
    }
}
