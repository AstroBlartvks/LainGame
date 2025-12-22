package com.astro.minor.system.apps.editor;

import com.astro.minor.system.apps.base.Application;

import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.signal.CognitiveStabilitySystem;
import com.astro.minor.system.apps.terminal.commands.Psyche;
import com.astro.minor.audio.WatcherSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MemexEditor extends Application {

    private Psyche psycheChip;
    private boolean psycheEnabled;

    private String currentFilePath;
    private String workingDirectory;
    private List<String> lines;
    private boolean modified;
    private boolean isUserFile;

    private int currentLine;
    private int currentColumn;
    private int scrollOffset;

    private float cursorBlinkTimer;
    private boolean showCursor;

    private float glitchTimer;
    private float cognitiveLoad;

    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private boolean zenMode;
    private boolean isEncrypted;
    private String encryptionKey;

    private MemexInputProcessor inputProcessor;
    private FileSystemContext fileSystemContext;

    private static final int MAX_VISIBLE_LINES = 20;
    private static final int LINE_HEIGHT = 20;
    private static final int LEFT_MARGIN = 60;
    private static final String DEFAULT_ENCRYPTION_KEY = "LAYER_07_PROTOCOL";

    public MemexEditor(int x, int y, int width, int height, String applicationName, OrthographicCamera camera, FileSystemContext fileSystemContext) {
        this(x, y, width, height, applicationName, camera, "./The Wired/Lain/User/", fileSystemContext);
    }

    public MemexEditor(int x, int y, int width, int height, String applicationName, OrthographicCamera camera, String workingDir, FileSystemContext fileSystemContext) {
        super(x, y, width, height, applicationName, camera);

        this.workingDirectory = workingDir;
        this.fileSystemContext = fileSystemContext;
        psycheChip = new Psyche();
        psycheEnabled = psycheChip.isPsycheChipDetected();

        lines = new ArrayList<>();
        lines.add("");

        currentLine = 0;
        currentColumn = 0;
        scrollOffset = 0;

        modified = false;
        isUserFile = false;
        zenMode = false;
        isEncrypted = false;
        encryptionKey = null;

        cursorBlinkTimer = 0;
        showCursor = true;
        glitchTimer = 0;
        cognitiveLoad = 0;

        createFontFromTTF();
        shapeRenderer = new ShapeRenderer();

        inputProcessor = new MemexInputProcessor(this);

        if (psycheEnabled) {
            System.out.println("[MEMEX] PSYCHE CHIP обнаружен. Cognitive Load Monitoring активен.");
        } else {
            System.out.println("[MEMEX] WARNING: PSYCHE CHIP не обнаружен. Ограниченный режим.");
        }
    }

    public com.badlogic.gdx.InputProcessor getInputProcessor() {
        return inputProcessor;
    }

    private void createFontFromTTF() {
        try {
            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator generator =
                new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator(
                    Gdx.files.internal("fonts/PTMono-Regular.ttf")
                );

            com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = 12;
            parameter.color = Color.valueOf("00FF40");
            parameter.characters = getRussianCharacters();

            font = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.err.println("[MEMEX] Ошибка загрузки шрифта: " + e.getMessage());
            font = new BitmapFont();
            font.setColor(Color.valueOf("00FF40"));
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

        return chars.toString();
    }

    public void loadFile(String path) {
        File file = new File(path);

        System.out.println("[MEMEX] Попытка загрузки: " + path);
        System.out.println("[MEMEX] Файл существует: " + file.exists());
        System.out.println("[MEMEX] Абсолютный путь: " + file.getAbsolutePath());

        lines.clear();

        if (!file.exists()) {
            System.out.println("[MEMEX] Файл не найден, создаём новый: " + path);
            currentFilePath = path;
            lines.add("");
            currentLine = 0;
            currentColumn = 0;
            scrollOffset = 0;
            modified = false;
            isUserFile = true;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                lineCount++;
            }

            if (lines.isEmpty()) {
                lines.add("");
            }

            currentFilePath = path;
            currentLine = 0;
            currentColumn = 0;
            scrollOffset = 0;
            modified = false;
            isUserFile = true;

            System.out.println("[MEMEX] ✓ Загружено: " + path + " (" + lineCount + " строк)");
        } catch (IOException e) {
            System.err.println("[MEMEX] ✗ Ошибка загрузки: " + e.getMessage());
            e.printStackTrace();
            lines.add("");
        }
    }

    public void saveFile() {
        if (currentFilePath == null || currentFilePath.isEmpty()) {
            currentFilePath = workingDirectory + "untitled.txt";
        }

        File file = new File(currentFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            System.out.println("[MEMEX] Создание директории: " + parentDir.getAbsolutePath() + " - " + created);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < lines.size(); i++) {
                writer.write(lines.get(i));
                if (i < lines.size() - 1) {
                    writer.newLine();
                }
            }

            modified = false;
            isUserFile = true;

            if (fileSystemContext != null) {
                fileSystemContext.refresh();
            }

            System.out.println("[MEMEX] Сохранено: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[MEMEX] Ошибка сохранения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openFileDialog() {
        if (modified) {
            System.out.println("[MEMEX] Предупреждение: файл изменён. Сохраните (F1) перед открытием другого файла.");
            return;
        }

        System.out.println("[MEMEX] F2 - Открытие списка файлов из текущей директории...");

        com.astro.minor.filesystem.items.Directory currentDir = fileSystemContext.getCurrentDirectory();
        java.util.List<com.astro.minor.filesystem.items.FileSystemItem> files = currentDir.getFiles();

        if (files.isEmpty()) {
            System.out.println("[MEMEX] В текущей директории нет файлов.");
            return;
        }

        System.out.println("[MEMEX] Доступные файлы:");
        for (int i = 0; i < Math.min(files.size(), 10); i++) {
            System.out.println("  " + (i + 1) + ". " + files.get(i).getName());
        }

        if (files.size() > 0 && files.get(0) instanceof com.astro.minor.filesystem.items.MyFile) {
            String firstFile = files.get(0).getName();
            String filePath = currentDir.getPath();
            if (!filePath.endsWith("/") && !filePath.endsWith("\\")) {
                filePath += "/";
            }
            filePath += firstFile;

            loadFile(filePath);
            System.out.println("[MEMEX] Загружен первый файл: " + firstFile);
            System.out.println("[MEMEX] Используйте команду 'edit <файл>' для выбора конкретного файла.");
        }
    }

    private void toggleEncryption() {
        if (isEncrypted) {
            if (encryptionKey == null) {
                System.out.println("[MEMEX] F4 - Дешифрование файла с ключом по умолчанию...");
                encryptionKey = DEFAULT_ENCRYPTION_KEY;
            }

            decryptContent();
            isEncrypted = false;
            modified = true;

            System.out.println("[MEMEX] ✓ Файл расшифрован. Сохраните (F1) для применения изменений.");
        } else {
            System.out.println("[MEMEX] F4 - Шифрование файла с ключом по умолчанию: " + DEFAULT_ENCRYPTION_KEY);
            encryptionKey = DEFAULT_ENCRYPTION_KEY;

            encryptContent();
            isEncrypted = true;
            modified = true;

            System.out.println("[MEMEX] ✓ Файл зашифрован. Сохраните (F1) для применения изменений.");
            System.out.println("[MEMEX] Внимание: зашифрованный текст может выглядеть нечитаемым!");
        }
    }

    private void encryptContent() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String encrypted = xorEncrypt(line, encryptionKey);
            lines.set(i, encrypted);
        }
    }

    private void decryptContent() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String decrypted = xorEncrypt(line, encryptionKey);
            lines.set(i, decrypted);
        }
    }

    private String xorEncrypt(String text, String key) {
        if (text == null || text.isEmpty() || key == null || key.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char textChar = text.charAt(i);
            char keyChar = key.charAt(i % key.length());
            char encrypted = (char) (textChar ^ keyChar);
            result.append(encrypted);
        }

        return result.toString();
    }

    private void insertChar(char c) {
        String line = lines.get(currentLine);
        String newLine = line.substring(0, currentColumn) + c + line.substring(currentColumn);
        lines.set(currentLine, newLine);
        currentColumn++;
        modified = true;

        if (psycheEnabled && Math.random() < 0.05 && cognitiveLoad > 0.4f) {
            tryInsertMemoryFragment();
        }
    }

    private void deleteChar() {
        if (currentColumn > 0) {
            String line = lines.get(currentLine);
            String newLine = line.substring(0, currentColumn - 1) + line.substring(currentColumn);
            lines.set(currentLine, newLine);
            currentColumn--;
            modified = true;
        } else if (currentLine > 0) {
            String currentText = lines.get(currentLine);
            lines.remove(currentLine);
            currentLine--;
            currentColumn = lines.get(currentLine).length();
            lines.set(currentLine, lines.get(currentLine) + currentText);
            modified = true;
        }
    }

    private void newLine() {
        String line = lines.get(currentLine);
        String leftPart = line.substring(0, currentColumn);
        String rightPart = line.substring(currentColumn);

        lines.set(currentLine, leftPart);
        lines.add(currentLine + 1, rightPart);

        currentLine++;
        currentColumn = 0;
        modified = true;
    }

    private void moveCursor(int dx, int dy) {
        if (dy != 0) {
            currentLine = Math.max(0, Math.min(lines.size() - 1, currentLine + dy));
            currentColumn = Math.min(currentColumn, lines.get(currentLine).length());

            if (currentLine < scrollOffset) {
                scrollOffset = currentLine;
            } else if (currentLine >= scrollOffset + MAX_VISIBLE_LINES) {
                scrollOffset = currentLine - MAX_VISIBLE_LINES + 1;
            }
        }

        if (dx != 0) {
            currentColumn = Math.max(0, Math.min(lines.get(currentLine).length(), currentColumn + dx));
        }
    }

    private void calculateCognitiveLoad() {
        if (!psycheEnabled) {
            cognitiveLoad = 0;
            return;
        }

        int totalChars = 0;
        for (String line : lines) {
            totalChars += line.length();
        }

        float cognitiveStability = CognitiveStabilitySystem.getInstance().getStability();
        cognitiveLoad = (totalChars / 1000.0f) * (1.0f / Math.max(0.1f, cognitiveStability));
        cognitiveLoad = Math.min(1.0f, cognitiveLoad);
    }

    private void applyGlitchEffect() {
        if (!psycheEnabled || cognitiveLoad < 0.4f) return;

        if (Math.random() < cognitiveLoad * 0.05f) {
            int randomLine = (int)(Math.random() * lines.size());
            String line = lines.get(randomLine);

            if (line.length() > 0) {
                char[] chars = line.toCharArray();
                int glitchPos = (int)(Math.random() * chars.length);

                char[] glitchChars = {'█', '▓', '▒', '░'};
                chars[glitchPos] = glitchChars[(int)(Math.random() * glitchChars.length)];

                lines.set(randomLine, new String(chars));
            }
        }
    }

    private void tryInsertMemoryFragment() {
        if (Math.random() > 0.05f) return;

        String[] fragments = {
            "[ФРАГМЕНТ ПАМЯТИ #0x" + Integer.toHexString((int)(Math.random() * 0xFFFF)).toUpperCase() + "]",
            "\"...школа... сестра... не могу вспомнить...\"",
            "[КОНЕЦ ФРАГМЕНТА]"
        };

        for (String fragment : fragments) {
            lines.add(currentLine + 1, fragment);
        }

        currentLine += fragments.length;
    }

    @Override
    public void handleInput() {

    }

    private static class MemexInputProcessor extends InputAdapter {
        private final MemexEditor editor;

        public MemexInputProcessor(MemexEditor editor) {
            this.editor = editor;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.F1 ||
                (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && keycode == Input.Keys.S)) {
                editor.saveFile();
                return true;
            }

            if (keycode == Input.Keys.F2) {
                editor.openFileDialog();
                return true;
            }

            if (keycode == Input.Keys.F4) {
                editor.toggleEncryption();
                return true;
            }

            if (keycode == Input.Keys.F5) {
                editor.zenMode = !editor.zenMode;
                return true;
            }

            if (keycode == Input.Keys.LEFT) {
                if (editor.currentColumn > 0) {
                    editor.currentColumn--;
                } else if (editor.currentLine > 0) {
                    editor.currentLine--;
                    editor.currentColumn = editor.lines.get(editor.currentLine).length();
                }
                return true;
            }

            if (keycode == Input.Keys.RIGHT) {
                String line = editor.lines.get(editor.currentLine);
                if (editor.currentColumn < line.length()) {
                    editor.currentColumn++;
                } else if (editor.currentLine < editor.lines.size() - 1) {
                    editor.currentLine++;
                    editor.currentColumn = 0;
                }
                return true;
            }

            if (keycode == Input.Keys.UP) {
                if (editor.currentLine > 0) {
                    editor.currentLine--;
                    String line = editor.lines.get(editor.currentLine);
                    editor.currentColumn = Math.min(editor.currentColumn, line.length());
                }
                return true;
            }

            if (keycode == Input.Keys.DOWN) {
                if (editor.currentLine < editor.lines.size() - 1) {
                    editor.currentLine++;
                    String line = editor.lines.get(editor.currentLine);
                    editor.currentColumn = Math.min(editor.currentColumn, line.length());
                }
                return true;
            }

            if (keycode == Input.Keys.ESCAPE) {
                if (editor.zenMode) {
                    editor.zenMode = false;
                } else {
                    editor.setClosed(true);
                }
                return true;
            }

            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            if (character == '\r' || character == '\n') {
                editor.newLine();
                return true;
            }

            if (character == '\b') {
                editor.deleteChar();
                return true;
            }

            if (character >= 32 && character != 127 && character < 0xF700) {
                editor.insertChar(character);
                return true;
            }

            return false;
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        float delta = Gdx.graphics.getDeltaTime();

        cursorBlinkTimer += delta;
        float blinkSpeed = (psycheEnabled && cognitiveLoad > 0.2f) ? 0.3f : 0.5f;
        if (cursorBlinkTimer >= blinkSpeed) {
            cursorBlinkTimer = 0;
            showCursor = !showCursor;
        }

        if (psycheEnabled) {
            calculateCognitiveLoad();

            glitchTimer += delta;
            if (glitchTimer >= 1.0f) {
                glitchTimer = 0;
                applyGlitchEffect();
            }
        }

        if (zenMode) {
            renderZenMode(batch);
        } else {
            renderNormalMode(batch);
        }
    }

    private void renderNormalMode(SpriteBatch batch) {
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("0A0A0A"));
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.valueOf("00FF40"));
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.rect(getX(), getY() + getHeight() - 30, getWidth(), 30);
        shapeRenderer.rect(getX(), getY() + 50, getWidth(), getHeight() - 80);
        shapeRenderer.end();

        batch.begin();

        String filename = currentFilePath != null ? new File(currentFilePath).getName() : "untitled.txt";
        String status = "";
        if (modified) status += "[ИЗМЕНЕНО] ";
        if (isEncrypted) status += "[ENCRYPTED] ";
        font.draw(batch, filename + " " + status, getX() + 10, getY() + getHeight() - 10);

        int visibleLines = Math.min(MAX_VISIBLE_LINES, lines.size() - scrollOffset);
        for (int i = 0; i < visibleLines; i++) {
            int lineIndex = scrollOffset + i;
            String lineNum = String.format("%03d", lineIndex + 1);
            String lineText = lines.get(lineIndex);

            float y = getY() + getHeight() - 60 - (i * LINE_HEIGHT);

            font.setColor(Color.GRAY);
            font.draw(batch, lineNum + " |", getX() + 10, y);

            font.setColor(Color.valueOf("00FF40"));

            if (lineIndex == currentLine && showCursor) {
                String beforeCursor = lineText.substring(0, Math.min(currentColumn, lineText.length()));
                float cursorX = getX() + LEFT_MARGIN + font.draw(batch, beforeCursor, getX() + LEFT_MARGIN, y).width;

                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0, 1, 0.25f, 0.8f);
                shapeRenderer.rect(cursorX, y - 15, 2, 16);
                shapeRenderer.end();
                batch.begin();

                font.draw(batch, lineText, getX() + LEFT_MARGIN, y);
            } else {
                font.draw(batch, lineText, getX() + LEFT_MARGIN, y);
            }
        }

        String hotkeys = "[F1] Save [F2] Open [F4] Encrypt [F5] Zen [ESC] Exit";
        font.draw(batch, hotkeys, getX() + 10, getY() + 35);

        int totalChars = 0;
        for (String line : lines) totalChars += line.length();

        String stats = String.format("Line: %03d  Chars: %d  Cognitive: %d%%",
            currentLine + 1, totalChars, (int)(cognitiveLoad * 100));
        font.draw(batch, stats, getX() + 10, getY() + 15);
    }

    private void renderZenMode(SpriteBatch batch) {
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("0A0A0A"));
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        batch.begin();

        int startY = getY() + getHeight() / 2 + 100;
        int visibleLines = Math.min(10, lines.size() - scrollOffset);

        for (int i = 0; i < visibleLines; i++) {
            int lineIndex = scrollOffset + i;
            String lineText = lines.get(lineIndex);

            float y = startY - (i * LINE_HEIGHT);

            if (lineIndex == currentLine && showCursor) {
                String beforeCursor = lineText.substring(0, Math.min(currentColumn, lineText.length()));
                float cursorX = getX() + 50 + font.draw(batch, beforeCursor, getX() + 50, y).width;

                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0, 1, 0.25f, 0.8f);
                shapeRenderer.rect(cursorX, y - 15, 2, 16);
                shapeRenderer.end();
                batch.begin();

                font.draw(batch, lineText, getX() + 50, y);
            } else {
                font.draw(batch, lineText, getX() + 50, y);
            }
        }

        font.draw(batch, "[F1] Save  [F5] Exit Zen", getX() + getWidth() / 2 - 100, getY() + 30);
    }

    @Override
    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
