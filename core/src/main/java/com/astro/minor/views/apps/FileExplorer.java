package com.astro.minor.views.apps;

import com.astro.minor.StarField;
import com.astro.minor.fileOS.Directory;
import com.astro.minor.fileOS.FileSystemItem;
import com.astro.minor.fileOS.FileSystemManager;
import com.astro.minor.fileOS.MyFile;
import com.astro.minor.ui.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileExplorer extends Application {
    private FileSystemManager fileSystemManager;
    private ShapeRenderer shapeRenderer;

    private Panel mainPanel;
    private Label pathLabel;
    private Button backButton;
    private Panel filesPanel;
    private Grid filesGrid;

    private final float topPanelHeight = 50f;
    private final float padding = 3f;
    private final float iconSize = 50f;
    private final float itemWidth = 80f;
    private final float itemHeight = 75f;
    private final float scrollSpeed = 20f;
    private final float minWidth = 500f;
    private final float minHeight = 400f;
    private float scrollOffset = 0f;
    private int columns = 4;
    private Rectangle scissors = new Rectangle();
    private Rectangle clipBounds = new Rectangle();

    private Texture folderIcon;
    private Texture fileIcon;
    private Map<String, Texture> extensionIcons;

    public FileExplorer(int x, int y, int width, int height, String applicationName, OrthographicCamera camera) {
        super(x, y, width, height, applicationName, camera);
        this.fileSystemManager = new FileSystemManager("./computer/");
        this.shapeRenderer = new ShapeRenderer();
        loadIcons();
        initialize();
    }

    private void loadIcons() {
        try {
            folderIcon = new Texture(Gdx.files.internal("icons/folder.png"));
            fileIcon = new Texture(Gdx.files.internal("icons/file.png"));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить иконки: " + e.getMessage());
            folderIcon = null;
            fileIcon = null;
        }

        extensionIcons = new HashMap<>();
        extensionIcons.put("txt", new Texture(Gdx.files.internal("icons/txt.png")));
        extensionIcons.put("mp4", new Texture(Gdx.files.internal("icons/mp4.png")));
        extensionIcons.put("json", new Texture(Gdx.files.internal("icons/json.png")));
        extensionIcons.put("mp3", new Texture(Gdx.files.internal("icons/mp3.png")));
    }

    public Texture getIconForFile(String extension) {
        return extensionIcons.getOrDefault(extension, fileIcon);
    }

    public void setIconForExtension(String extension, Texture icon) {
        extensionIcons.put(extension, icon);
    }

    private void initialize() {
        mainPanel = new Panel(x, y, width, height);
        mainPanel.setBackgroundColor(0.0f, 0.05f, 0.15f, 0.9f);
        mainPanel.setHoverColor(0.0f, 0.1f, 0.3f, 1f);

        createTopPanel();
        createFilesPanel();
        refreshFileList();
    }

    private void createTopPanel() {
        backButton = new Button(x + padding, y + height - topPanelHeight + padding + 5, 60, 30, "Назад");
        backButton.setBackgroundColor(0.0f, 0.05f, 0.15f, 0.9f);
        backButton.setHoverColor(0.0f, 0.1f, 0.3f, 1f);
        backButton.setBorderColor(new Color(0.0f, 0.02f, 0.08f, 0.85f));
        backButton.setBorderWidth(1);
        backButton.setTextColor(new Color(0.3f, 0.9f, 0.9f, 1f));
        backButton.setFont(16);
        backButton.setClickListener(btn -> navigateBack());

        String currentPath = fileSystemManager.getCurrentDirectory().getPath();
        pathLabel = new Label(x + padding + 75, y + height - topPanelHeight + padding - 5, width - padding - 85, 30, currentPath);
        pathLabel.setFont(14);
        pathLabel.setTextColor(new Color(0.4f, 1f, 0.6f, 1f));
        pathLabel.setDrawBackground(true);
        pathLabel.setBackgroundColor(0.0f, 0.0f, 0.0f, 0.7f);
        pathLabel.setBorderColor(new Color(0.0f, 0.02f, 0.08f, 0.85f));
        pathLabel.setBorderWidth(1);
        pathLabel.setAlignment(Align.bottomLeft);

        mainPanel.addComponent(backButton);
        mainPanel.addComponent(pathLabel);
    }

    private void createFilesPanel() {
        float filesPanelY = y + padding/2;
        float filesPanelHeight = height - topPanelHeight - padding * 2;

        filesPanel = new Panel(x + padding, filesPanelY, width - padding * 2, filesPanelHeight);
        filesPanel.setDrawBackground(true);
        filesPanel.setBackgroundColor(0.0f, 0.02f, 0.08f, 0.85f);
        filesPanel.setHoverColor(0.0f, 0.02f, 0.08f, 0.85f);
        filesPanel.setBorderWidth(1);
        filesPanel.setBorderColor(new Color(0.1f, 0.1f, 0.1f, 0.8f));

        columns = Math.max(1, (int)((width - padding * 4) / itemWidth));

        filesGrid = new Grid(x + padding * 2, filesPanelY + padding, width - padding * 4, filesPanelHeight - padding * 2, columns, 1);
        filesGrid.setDrawBackground(false);
        filesGrid.setAutoResize(false);
        filesGrid.setSpacing(10);

        mainPanel.addComponent(filesPanel);
    }

    private void refreshFileList() {
        if (filesGrid != null) {
            mainPanel.removeComponent(filesGrid);
            filesGrid.dispose();
        }

        String currentPath = fileSystemManager.getCurrentDirectory().getPath();
        pathLabel.setText(currentPath);

        Directory currentDir = fileSystemManager.getCurrentDirectory();
        List<FileSystemItem> directories = currentDir.getDirectories();
        List<FileSystemItem> files = currentDir.getFiles();

        int totalItems = directories.size() + files.size();

        float filesPanelY = y + padding;
        float filesPanelHeight = height - topPanelHeight - padding * 2;
        columns = Math.max(1, (int)((width - padding * 4) / itemWidth));

        int rows = (int) Math.ceil((double) totalItems / columns);
        rows = Math.max(1, rows);

        filesGrid = new Grid(x + padding * 2, filesPanelY + padding, width - padding * 4, filesPanelHeight - padding * 2, columns, rows);
        filesGrid.setDrawBackground(false);
        filesGrid.setAutoResize(true);
        filesGrid.setSpacing(5);

        for (FileSystemItem dir : directories) {
            VBox itemBox = createFileItem(dir.getName(), folderIcon, true);

            if (itemBox.getComponents().size > 0) {
                UIComponent firstComponent = itemBox.getComponents().get(0);
                if (firstComponent instanceof Button) {
                    ((Button) firstComponent).setClickListener(btn -> {
                        navigateToDirectory(dir.getName());
                    });
                }
            }

            filesGrid.addComponent(itemBox);
        }

        for (FileSystemItem file : files) {
            Texture icon = fileIcon;
            if (file instanceof MyFile) {
                String extension = ((MyFile) file).getExtension();
                icon = getIconForFile(extension);
            }

            VBox itemBox = createFileItem(file.getName(), icon, false);

            if (itemBox.getComponents().size > 0) {
                UIComponent firstComponent = itemBox.getComponents().get(0);
                if (firstComponent instanceof Button) {
                    ((Button) firstComponent).setClickListener(btn -> {
                        executeFile(file.getName());
                    });
                }
            }

            filesGrid.addComponent(itemBox);
        }

        mainPanel.addComponent(filesGrid);
        scrollOffset = 0;
    }

    private VBox createFileItem(String name, Texture icon, boolean isFolder) {
        VBox itemBox = new VBox(0, 0, itemWidth, 0);
        itemBox.setDrawBackground(false);
        itemBox.setSpacing(3);

        Button iconButton = new Button(0, 0, iconSize, iconSize, "");
        iconButton.setIconButton(true);

        if (icon != null) {
            iconButton.setBackgroundIcon(icon, iconSize, iconSize);
            iconButton.setBackgroundColor(0.05f, 0.1f, 0.15f, 0.9f);
            iconButton.setHoverColor(isFolder ?
                new Color(0.1f, 0.3f, 0.4f, 1f) :
                new Color(0.1f, 0.4f, 0.3f, 1f));
            iconButton.setBorderColor(new Color(0.0f, 0.4f, 0.6f, 0.5f));
            iconButton.setBorderWidth(1);
        } else {
            iconButton.setIconButton(false);
            iconButton.setText(isFolder ? "[DIR]" : "[FILE]");
            iconButton.setBackgroundColor(new Color(0.0f, 0.1f, 0.2f, 0.9f));
            iconButton.setHoverColor(new Color(0.0f, 0.2f, 0.4f, 1f));
            iconButton.setTextColor(new Color(0.3f, 0.9f, 0.9f, 1f));
            iconButton.setFont(10);
            iconButton.setBorderColor(new Color(0.0f, 0.4f, 0.6f, 0.7f));
            iconButton.setBorderWidth(1);
        }

        String displayName = name;
        if (displayName.length() > 12) {
            displayName = displayName.substring(0, 10) + "..";
        }

        Label nameLabel = new Label(0, 0, itemWidth, 20, displayName);
        nameLabel.setFont(12);
        nameLabel.setTextColor(new Color(0.5f, 0.9f, 0.7f, 1f));
        nameLabel.setAlignment(Align.center);
        nameLabel.setDrawBackground(false);

        itemBox.addComponent(iconButton);
        itemBox.addComponent(nameLabel);

        return itemBox;
    }

    private void navigateToDirectory(String dirName) {
        if (fileSystemManager.cd(dirName)) {
            refreshFileList();
        }
    }

    private void navigateBack() {
        if (fileSystemManager.cd("..")) {
            refreshFileList();
        }
    }

    private void executeFile(String fileName) {
        String result = fileSystemManager.execute(fileName);

        Console console = new Console(
            x + 50,
            y + 50,
            Math.min(600, width),
            Math.min(400, height - 100),
            "exec: " + fileName,
            camera
        );

        console.output(result);
        StarField.getAppExecutor().addApplication(console);
    }

    @Override
    public void render(SpriteBatch batch) {
        updatePositions();
        handleScroll();

        mainPanel.update(Gdx.graphics.getDeltaTime());
        shapeRenderer.setProjectionMatrix(camera.combined);

        applyScroll();

        float filesPanelY = y + padding;
        float filesPanelHeight = height - topPanelHeight - padding * 2;

        clipBounds.set(x + padding, filesPanelY, width - padding * 2, filesPanelHeight);
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);

        boolean renderTop = true;
        for (int i = 0; i < mainPanel.getComponents().size; i++) {
            try {
                UIComponent component = mainPanel.getComponents().get(i);
                if (component == filesGrid) {
                    batch.flush();
                    ScissorStack.pushScissors(scissors);
                    filesGrid.render(batch, shapeRenderer);
                    batch.flush();
                    ScissorStack.popScissors();
                    renderTop = false;
                } else if (component != filesPanel) {
                    component.render(batch, shapeRenderer);
                } else if (renderTop) {
                    filesPanel.render(batch, shapeRenderer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePositions() {
        width = (int) Math.max(width, minWidth);
        height = (int) Math.max(height, minHeight);

        mainPanel.setPosition(x, y);
        mainPanel.setSize(width, height);

        backButton.setPosition(x + padding, y + height - topPanelHeight + padding + 5);
        pathLabel.setPosition(x + padding + 75, y + height - topPanelHeight + padding + 5);
        pathLabel.setSize(width - padding - 85, 30);

        float filesPanelY = y + padding;
        float filesPanelHeight = height - topPanelHeight - padding * 2;
        filesPanel.setPosition(x + padding, filesPanelY);
        filesPanel.setSize(width - padding * 2, filesPanelHeight);

        if (filesGrid != null) {
            int newColumns = Math.max(1, (int)((width - padding * 4) / itemWidth));
            if (newColumns != columns) {
                columns = newColumns;
                refreshFileList();
            } else {
                filesGrid.setPosition(x + padding * 2, filesPanelY + padding - scrollOffset);
                filesGrid.setSize(width - padding * 4, filesPanelHeight - padding * 2);
            }
        }
    }

    private void handleScroll() {
        if (!activeApplication || filesGrid == null) return;

        float filesPanelHeight = height - topPanelHeight - padding * 2;

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
            scrollOffset += scrollSpeed * Gdx.graphics.getDeltaTime() * 60;
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.UP)) {
            scrollOffset -= scrollSpeed * Gdx.graphics.getDeltaTime() * 60;
        }

        float maxScroll = Math.max(0, filesGrid.getHeight() - filesPanelHeight + padding * 2);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    private void applyScroll() {
        float filesPanelY = y + padding;

        if (filesGrid != null) {
            filesGrid.setPosition(x + padding * 2, filesPanelY + padding - scrollOffset);
        }
    }

    @Override
    public void handleInput() {
    }

    @Override
    public void dispose() {
        mainPanel.dispose();
        shapeRenderer.dispose();

        if (folderIcon != null) {
            folderIcon.dispose();
        }
        if (fileIcon != null) {
            fileIcon.dispose();
        }
        for (Texture texture : extensionIcons.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        extensionIcons.clear();
    }
}
