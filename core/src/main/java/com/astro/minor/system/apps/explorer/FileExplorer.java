package com.astro.minor.system.apps.explorer;

import com.astro.minor.system.apps.base.Application;
import com.astro.minor.core.ServiceLocator;
import com.astro.minor.filesystem.items.Directory;
import com.astro.minor.filesystem.items.FileSystemItem;
import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.filesystem.items.MyFile;
import com.astro.minor.system.apps.editor.MemexEditor;
import com.astro.minor.system.apps.terminal.Console;
import com.astro.minor.ui.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileExplorer extends Application {
    private FileSystemContext fileSystemContext;
    private ShapeRenderer shapeRenderer;

    private Panel mainPanel;
    private Label pathLabel;
    private Button backButton;
    private Panel filesPanel;
    private Grid filesGrid;

    private final float topPanelHeight = 50f;
    private final float padding = 3f;
    private final float iconSize = 50f;
    private final float itemWidth = 90f;
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
        this.fileSystemContext = new FileSystemContext();
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
        extensionIcons.put("avi", new Texture(Gdx.files.internal("icons/mp4.png")));
        extensionIcons.put("mkv", new Texture(Gdx.files.internal("icons/mp4.png")));
        extensionIcons.put("mov", new Texture(Gdx.files.internal("icons/mp4.png")));
        extensionIcons.put("json", new Texture(Gdx.files.internal("icons/json.png")));
        extensionIcons.put("mp3", new Texture(Gdx.files.internal("icons/mp3.png")));
        extensionIcons.put("wav", new Texture(Gdx.files.internal("icons/wav.png")));
        extensionIcons.put("wpml", new Texture(Gdx.files.internal("icons/wpml.png")));
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

        String currentPath = fileSystemContext.getCurrentDirectory().getPath();
        pathLabel = new Label(x + padding + 75, y + height - topPanelHeight + padding - 5, width - padding - 85, 30, currentPath);
        pathLabel.setFont(14);
        pathLabel.setTextColor(new Color(0.4f, 1f, 0.6f, 1f));
        pathLabel.setDrawBackground(true);
        pathLabel.setBackgroundColor(0.0f, 0.0f, 0.0f, 0.7f);
        pathLabel.setBorderColor(new Color(0.0f, 0.02f, 0.08f, 0.85f));
        pathLabel.setBorderWidth(1);
        pathLabel.setAlignment(Align.bottomLeft);
        pathLabel.setPadding(5f);

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
        filesGrid.setSpacing(15);

        mainPanel.addComponent(filesPanel);
    }

    private void refreshFileList() {
        fileSystemContext.refresh();

        if (filesGrid != null) {
            mainPanel.removeComponent(filesGrid);
            filesGrid.dispose();
        }

        String currentPath = fileSystemContext.getCurrentDirectory().getPath();
        pathLabel.setText(currentPath);

        Directory currentDir = fileSystemContext.getCurrentDirectory();
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
        filesGrid.setSpacing(10);

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
        itemBox.setSpacing(5);

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
        if (displayName.length() > 14) {
            displayName = displayName.substring(0, 12) + "...";
        }

        Label nameLabel = new Label(0, 0, itemWidth * 1.2f, 20, displayName);
        nameLabel.setFont(13);
        nameLabel.setTextColor(new Color(0.5f, 0.9f, 0.7f, 1f));
        nameLabel.setAlignment(Align.center);
        nameLabel.setDrawBackground(false);

        itemBox.addComponent(iconButton);
        itemBox.addComponent(nameLabel);

        return itemBox;
    }

    private void navigateToDirectory(String dirName) {
        fileSystemContext.refresh();
        if (fileSystemContext.cd(dirName)) {
            refreshFileList();
        }
    }

    private void navigateBack() {
        if (fileSystemContext.cd("..")) {
            refreshFileList();
        }
    }

    private void executeFile(String fileName) {
        if (fileName.endsWith(".txt") || fileName.endsWith(".json")) {
            String currentPath = fileSystemContext.getCurrentDirectory().getPath();
            if (!currentPath.endsWith("/") && !currentPath.endsWith("\\")) {
                currentPath += "/";
            }
            String filePath = currentPath + fileName;
            String workingDir = fileSystemContext.getCurrentDirectory().getPath();

            System.out.println("[FileExplorer] Открытие файла: " + filePath);

            MemexEditor editor = new MemexEditor(
                x + 50,
                y + 50,
                800,
                600,
                "MEMEX EDITOR",
                camera,
                fileSystemContext.getVirtualPath(),
                fileSystemContext
            );

            editor.loadFile(filePath);
            ServiceLocator.getAppExecutor().addApplication(editor);
            return;
        }

        String result = fileSystemContext.execute(fileName);

        Console console = new Console(
            x + 50,
            y + 50,
            Math.min(600, width),
            Math.min(400, height - 100),
            "exec: " + fileName,
            camera
        );

        console.setPathWithUsingFSM(fileSystemContext);

        console.output(result);
        ServiceLocator.getAppExecutor().addApplication(console);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F5)) {
            fileSystemContext.refresh();
            refreshFileList();
        }

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
        if (fileSystemContext != null) {
            fileSystemContext.dispose();
        }
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
