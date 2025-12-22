package com.astro.minor.system.apps.browser;

import com.astro.minor.system.apps.base.Application;
import com.astro.minor.wired.core.*;
import com.astro.minor.wired.core.LayoutBox;
import com.astro.minor.wired.core.LinkInfo;
import com.astro.minor.wired.elements.*;
import com.astro.minor.wired.events.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Browser extends Application {
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private WiredDocumentParser parser;
    private WiredPageRenderer renderer;
    private WiredDocument currentDocument;

    private String currentUrl;
    private List<String> history;
    private int historyIndex;

    private float scrollOffset = 0;
    private float maxScroll = 0;
    private float contentHeight = 0;

    private static final int TOOLBAR_HEIGHT = 40;
    private static final int BUTTON_SIZE = 30;
    private static final int BUTTON_MARGIN = 5;
    private static final int CONTENT_PADDING = 10;
    private static final float MIN_WIDTH = 400f;
    private static final float MIN_HEIGHT = 300f;
    private static final float SCROLL_SPEED = 20f;

    private Rectangle scissors = new Rectangle();
    private Rectangle clipBounds = new Rectangle();

    private List<LinkInfo> currentLinks = new ArrayList<>();
    private String addressBarText;
    private BrowserInputProcessor inputProcessor;

    public Browser(int x, int y, int width, int height, String applicationName, OrthographicCamera camera, String initialUrl) {
        super(x, y, width, height, applicationName, camera);

        this.currentUrl = initialUrl;
        this.addressBarText = initialUrl;
        this.history = new ArrayList<>();
        this.historyIndex = -1;

        initialize();
        loadPage(initialUrl);
    }

    public com.badlogic.gdx.InputProcessor getInputProcessor() {
        return inputProcessor;
    }

    private void initialize() {
        createFont();
        shapeRenderer = new ShapeRenderer();
        parser = new WiredDocumentParser();
        renderer = new WiredPageRenderer(font);
        inputProcessor = new BrowserInputProcessor(this);
    }

    private void createFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/PTMono-Regular.ttf")
            );

            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = 14;
            parameter.color = Color.WHITE;
            parameter.characters = getRussianCharacters();

            font = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            font = new BitmapFont();
            font.setColor(Color.WHITE);
        }
    }

    private String getRussianCharacters() {
        StringBuilder chars = new StringBuilder();

        for (char c = 'А'; c <= 'я'; c++) {
            chars.append(c);
        }
        chars.append("ЁёЪъЬь");

        for (char c = 32; c < 127; c++) {
            chars.append(c);
        }

        chars.append("•");

        return chars.toString();
    }

    public void loadPage(String url) {
        try {
            com.badlogic.gdx.files.FileHandle fileHandle = Gdx.files.absolute(url);

            if (!fileHandle.exists()) {
                if (currentUrl != null) {
                    File currentFile = new File(currentUrl);
                    File parentDir = currentFile.getParentFile();
                    if (parentDir != null) {
                        File resolvedFile = new File(parentDir, url);
                        fileHandle = Gdx.files.absolute(resolvedFile.getAbsolutePath());
                    }
                }
            }

            if (!fileHandle.exists()) {
                System.err.println("[Browser] File not found: " + url);
                currentDocument = null;
                return;
            }

            String content = fileHandle.readString();
            currentDocument = parser.parse(content);

            addToHistory(fileHandle.file().getAbsolutePath());
            currentUrl = fileHandle.file().getAbsolutePath();

            if (currentDocument.getTitle() != null && !currentDocument.getTitle().isEmpty()) {
                addressBarText = currentDocument.getTitle();
            } else {
                addressBarText = fileHandle.name();
            }

            scrollOffset = 0;
            currentLinks.clear();

        } catch (Exception e) {
            System.err.println("[Browser] Failed to load page: " + e.getMessage());
            e.printStackTrace();
            currentDocument = null;
        }
    }

    private void addToHistory(String url) {
        if (historyIndex < history.size() - 1) {
            history = history.subList(0, historyIndex + 1);
        }
        history.add(url);
        historyIndex = history.size() - 1;
    }

    public void navigateBack() {
        if (historyIndex > 0) {
            historyIndex--;
            String url = history.get(historyIndex);
            currentUrl = url;
            loadPageWithoutHistory(url);
        }
    }

    public void navigateForward() {
        if (historyIndex < history.size() - 1) {
            historyIndex++;
            String url = history.get(historyIndex);
            currentUrl = url;
            loadPageWithoutHistory(url);
        }
    }

    private void loadPageWithoutHistory(String url) {
        try {
            com.badlogic.gdx.files.FileHandle fileHandle = Gdx.files.absolute(url);

            if (!fileHandle.exists()) {
                currentDocument = null;
                return;
            }

            String content = fileHandle.readString();
            currentDocument = parser.parse(content);

            if (currentDocument.getTitle() != null && !currentDocument.getTitle().isEmpty()) {
                addressBarText = currentDocument.getTitle();
            } else {
                addressBarText = new File(url).getName();
            }

            scrollOffset = 0;
            currentLinks.clear();

        } catch (Exception e) {
            System.err.println("[Browser] Failed to load page: " + e.getMessage());
            currentDocument = null;
        }
    }

    @Override
    public void handleInput() {
        if (Gdx.input.justTouched()) {
            Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(mouse);

            Gdx.app.log("Browser", "Click at (" + mouse.x + ", " + mouse.y + ")");

            float toolbarY = y + height - TOOLBAR_HEIGHT;

            if (mouse.x >= x + BUTTON_MARGIN && mouse.x <= x + BUTTON_MARGIN + BUTTON_SIZE &&
                mouse.y >= toolbarY + BUTTON_MARGIN && mouse.y <= toolbarY + BUTTON_MARGIN + BUTTON_SIZE) {
                navigateBack();
                return;
            }

            if (mouse.x >= x + BUTTON_MARGIN * 2 + BUTTON_SIZE && mouse.x <= x + BUTTON_MARGIN * 2 + BUTTON_SIZE * 2 &&
                mouse.y >= toolbarY + BUTTON_MARGIN && mouse.y <= toolbarY + BUTTON_MARGIN + BUTTON_SIZE) {
                navigateForward();
                return;
            }

        }
    }

    private void handleButtonClick(LayoutBox button) {
        String type = button.buttonType;

        if (type.equals("submit")) {
            LayoutBox form = renderer.findFormForButton(button);
            if (form != null) {
                java.util.Map<String, String> formData = renderer.collectFormData(form);
                com.astro.minor.wired.events.SubmitEvent event =
                    new com.astro.minor.wired.events.SubmitEvent(form.formName, formData);
                event.handle();
            }
        } else if (type.startsWith("onclick:")) {
            String functionName = type.substring(8); // Remove "onclick:" prefix
            com.astro.minor.wired.events.OnClickEvent event =
                new com.astro.minor.wired.events.OnClickEvent(functionName);
            event.handle();
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        updatePositions();

        renderer.update(com.badlogic.gdx.Gdx.graphics.getDeltaTime());

        batch.end();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Color bgColor = Color.BLACK;
        if (currentDocument != null && currentDocument.getBackgroundColor() != null) {
            bgColor = parseColor(currentDocument.getBackgroundColor());
        }
        shapeRenderer.setColor(bgColor);
        float contentY = y;
        float contentHeight = height - TOOLBAR_HEIGHT;
        shapeRenderer.rect(x, contentY, width, contentHeight);

        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x, y + height - TOOLBAR_HEIGHT, width, TOOLBAR_HEIGHT);

        shapeRenderer.end();
        batch.begin();

        drawToolbar(batch);

        if (currentDocument != null) {
            renderContentWithClipping(batch);
        }
    }

    private void updatePositions() {
        width = (int) Math.max(width, MIN_WIDTH);
        height = (int) Math.max(height, MIN_HEIGHT);
    }

    private void renderContentWithClipping(SpriteBatch batch) {
        float contentY = y;
        float contentAreaHeight = height - TOOLBAR_HEIGHT;
        float contentWidth = width - CONTENT_PADDING * 2;

        clipBounds.set(x, contentY, width, contentAreaHeight);
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);

        batch.flush();
        ScissorStack.pushScissors(scissors);

        float renderY = contentY + contentAreaHeight - CONTENT_PADDING + scrollOffset;
        currentLinks.clear();

        this.contentHeight = renderer.render(
            batch,
            shapeRenderer,
            currentDocument,
            x + CONTENT_PADDING,
            renderY,
            contentWidth,
            contentAreaHeight - CONTENT_PADDING * 2,
            0,  // We handle scroll offset ourselves
            currentLinks,
            currentUrl  // Pass current document path
        );

        maxScroll = Math.max(0, this.contentHeight - contentAreaHeight + CONTENT_PADDING * 2);

        batch.flush();
        ScissorStack.popScissors();
    }

    private void drawToolbar(SpriteBatch batch) {
        float toolbarY = y + height - TOOLBAR_HEIGHT;

        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        boolean canGoBack = historyIndex > 0;
        shapeRenderer.setColor(canGoBack ? 0.4f : 0.25f, canGoBack ? 0.4f : 0.25f, canGoBack ? 0.4f : 0.25f, 1f);
        shapeRenderer.rect(x + BUTTON_MARGIN, toolbarY + BUTTON_MARGIN, BUTTON_SIZE, BUTTON_SIZE);

        boolean canGoForward = historyIndex < history.size() - 1;
        shapeRenderer.setColor(canGoForward ? 0.4f : 0.25f, canGoForward ? 0.4f : 0.25f, canGoForward ? 0.4f : 0.25f, 1f);
        shapeRenderer.rect(x + BUTTON_MARGIN * 2 + BUTTON_SIZE, toolbarY + BUTTON_MARGIN, BUTTON_SIZE, BUTTON_SIZE);

        shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
        float addressBarX = x + BUTTON_MARGIN * 3 + BUTTON_SIZE * 2;
        float addressBarWidth = width - (BUTTON_MARGIN * 3 + BUTTON_SIZE * 2 + BUTTON_MARGIN);
        shapeRenderer.rect(addressBarX, toolbarY + BUTTON_MARGIN, addressBarWidth, BUTTON_SIZE);

        shapeRenderer.end();
        batch.begin();

        font.setColor(canGoBack ? Color.WHITE : Color.DARK_GRAY);
        font.draw(batch, "<", x + BUTTON_MARGIN + 10, toolbarY + BUTTON_MARGIN + 20);

        font.setColor(canGoForward ? Color.WHITE : Color.DARK_GRAY);
        font.draw(batch, ">", x + BUTTON_MARGIN * 2 + BUTTON_SIZE + 10, toolbarY + BUTTON_MARGIN + 20);

        font.setColor(Color.WHITE);
        font.draw(batch, addressBarText, addressBarX + 5, toolbarY + BUTTON_MARGIN + 20);
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        if (maxScroll > 0) {
            scrollOffset += amountY * SCROLL_SPEED;
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
        }
    }

    private Color parseColor(String hexColor) {
        try {
            if (hexColor.startsWith("#")) {
                hexColor = hexColor.substring(1);
            }
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            return new Color(r / 255f, g / 255f, b / 255f, 1f);
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    @Override
    public void dispose() {
        if (font != null) font.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (renderer != null) renderer.dispose();
    }

    @Override
    public void setClosed(boolean closed) {
        if (closed) {
            dispose();
        }
        super.setClosed(closed);
    }

    private static class BrowserInputProcessor extends InputAdapter {
        private final Browser browser;

        public BrowserInputProcessor(Browser browser) {
            this.browser = browser;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            browser.onScroll(amountX, amountY);
            return true;
        }

        @Override
        public boolean keyTyped(char character) {
            if (browser.renderer.getFocusedInput() != null) {
                browser.renderer.handleKeyInput(character);
                return true;
            }
            return false;
        }

        @Override
        public boolean keyDown(int keycode) {

            if (browser.renderer.getFocusedInput() != null) {
                if (keycode == com.badlogic.gdx.Input.Keys.BACKSPACE) {
                    browser.renderer.handleBackspace();
                    return true;
                }
                if (keycode == com.badlogic.gdx.Input.Keys.LEFT) {
                    browser.renderer.handleLeftArrow();
                    return true;
                }
                if (keycode == com.badlogic.gdx.Input.Keys.RIGHT) {
                    browser.renderer.handleRightArrow();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != com.badlogic.gdx.Input.Buttons.LEFT) {
                return false;
            }

            Vector3 mouse = new Vector3(screenX, screenY, 0);
            browser.camera.unproject(mouse);

            Gdx.app.log("Browser", "TouchDown at (" + mouse.x + ", " + mouse.y + ")");

            LayoutBox clickedInput = browser.renderer.findInputAt(mouse.x, mouse.y);
            if (clickedInput != null) {
                Gdx.app.log("Browser", "Found input: " + clickedInput.inputName);
                browser.renderer.setFocusedInput(clickedInput);
                return true;
            }

            LayoutBox clickedButton = browser.renderer.findButtonAt(mouse.x, mouse.y);
            if (clickedButton != null) {
                Gdx.app.log("Browser", "Found button: " + clickedButton.buttonText);
                browser.handleButtonClick(clickedButton);
                return true;
            }

            for (LinkInfo link : browser.currentLinks) {
                if (link.contains(mouse.x, mouse.y)) {
                    Gdx.app.log("Browser", "Clicked link: " + link.href);
                    browser.loadPage(link.href);
                    return true;
                }
            }

            browser.renderer.setFocusedInput(null);
            return false;
        }
    }
}
