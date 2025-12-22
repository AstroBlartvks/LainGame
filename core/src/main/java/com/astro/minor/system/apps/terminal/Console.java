package com.astro.minor.system.apps.terminal;

import com.astro.minor.system.apps.base.Application;
import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.filesystem.network.NetworkManager;
import com.astro.minor.system.apps.terminal.commands.*;
import com.astro.minor.audio.WatcherSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.*;

import static com.astro.minor.core.config.AppConfig.OS_NAME;
import static com.astro.minor.core.config.AppConfig.VERSION;

public class Console extends Application implements WatcherSystem.WatcherEventListener {
    private ConsoleInputProcessor inputProcessor;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private List<String> lines;
    private List<String> rawLines;
    private StringBuilder currentInput;
    private float padding = 15f;
    private float lineHeight;
    private float lineSpacing = 8f;
    private boolean showCursor = true;
    private float cursorTimer = 0f;
    private int scrollOffset = 0;
    private final Map<String, Command> commands = new HashMap<>();
    private final FileSystemContext fileSystemContext;
    private final NetworkManager networkManager;
    private int lastWidth = 0;
    private int mouseScrollAccumulator = 0;
    private static final int TAB_SIZE = 4;

    public Console(int x, int y, int width, int height, String applicationName, OrthographicCamera camera) {
        super(x, y, width, height, applicationName, camera);
        networkManager = new NetworkManager();
        fileSystemContext = new FileSystemContext();

        inputProcessor = new ConsoleInputProcessor(this);

        initialize();

        WatcherSystem.getInstance().registerListener(this);
        com.astro.minor.signal.ProcessWatcher.getInstance().setOutputConsole(this);
    }



    private void initialize() {
        commands.put("help", new Help());
        commands.put("time", new Time());
        commands.put("version", new Version());
        commands.put("echo", new Echo());
        commands.put("clear", new Clear());
        commands.put("start", new Start());
        commands.put("ls", new Ls(fileSystemContext));
        commands.put("cd", new Cd(fileSystemContext));
        commands.put("exec", new Exec(fileSystemContext));
        commands.put("pwd", new Pwd(fileSystemContext));
        commands.put("touch", new Touch(fileSystemContext));
        commands.put("mkdir", new Mkdir(fileSystemContext));

        commands.put("nmap", new Nmap(networkManager));
        Connect connectCmd = new Connect(networkManager, fileSystemContext);
        connectCmd.setConsole(this);
        commands.put("connect", connectCmd);

        commands.put("lain", new Lain());
        commands.put("love", new Love());
        commands.put("watcher", new Watcher());
        commands.put("crt", new Crt());
        commands.put("psyche", new Psyche());
        commands.put("signal", new Signal());
        commands.put("edit", new Edit(fileSystemContext));
        commands.put("memex", new Memex(fileSystemContext));

        createFontFromTTF();
        shapeRenderer = new ShapeRenderer();
        lines = new ArrayList<>();
        rawLines = new ArrayList<>();
        currentInput = new StringBuilder();

        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, "TEST");
        lineHeight = layout.height + lineSpacing + 1;

        lastWidth = width;

        output(OS_NAME + " [" + VERSION + "]");
        output("(c) Navi. Tachibana General Laboratories. All rights reserved.");
        output("");

        updateApplicationName();
    }

    public void updatePrompt() {
        updateApplicationName();
    }

    private void updateApplicationName() {
        if (networkManager.getCurrentHost() != null) {
            String hostname = networkManager.getCurrentHost().getHostname();
            if (hostname == null) {
                hostname = networkManager.getCurrentHost().getUsername();
            }
            this.applicationName = "Console [" + hostname + "@localhost]";
        }
    }

    private String getPromptPrefix() {
        if (networkManager.getCurrentHost() != null) {
            String hostname = networkManager.getCurrentHost().getHostname();
            if (hostname == null) {
                hostname = networkManager.getCurrentHost().getUsername();
            }
            return "[" + hostname + "@localhost]> ";
        }
        return "> ";
    }

    private void createFontFromTTF() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/PTMono-Regular.ttf")
            );

            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = 14;
            parameter.color = Color.GREEN;
            parameter.characters = getRussianCharacters();

            font = generator.generateFont(parameter);
            generator.dispose();

        } catch (Exception e) {
            font = new BitmapFont();
            font.setColor(Color.GREEN);
        }
    }

    private String getRussianCharacters() {
        StringBuilder chars = new StringBuilder();

        chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        chars.append("abcdefghijklmnopqrstuvwxyz");
        chars.append("0123456789");
        chars.append("!@#$%^&*()_+-=[]{}|;:',.<>?/`~ \"\\");
        chars.append("─═│║╔╗╚╝╠╣╦╩╬");

        for (char c = 'А'; c <= 'Я'; c++) {
            chars.append(c);
        }

        for (char c = 'а'; c <= 'я'; c++) {
            chars.append(c);
        }

        chars.append('Ё');
        chars.append('ё');

        return chars.toString();
    }

    public void output(String text) {
        if (text == null || text.isEmpty()) {
            rawLines.add("");
            lines.add("");
            return;
        }

        if (text.contains("\n")) {
            String[] splitText = text.split("\n");
            for (String line : splitText) {
                rawLines.add(line);
                String expandedLine = expandTabs(line);
                List<String> wrappedLines = wrapText(expandedLine, width - padding * 2);
                lines.addAll(wrappedLines);
            }
        } else {
            rawLines.add(text);
            String expandedLine = expandTabs(text);
            List<String> wrappedLines = wrapText(expandedLine, width - padding * 2);
            lines.addAll(wrappedLines);
        }
    }

    private String expandTabs(String text) {
        if (text == null || !text.contains("\t")) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\t') {
                int spacesToAdd = TAB_SIZE;
                for (int j = 0; j < spacesToAdd; j++) {
                    result.append(' ');
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private void rewrapAllLines() {
        lines.clear();
        for (String rawLine : rawLines) {
            String expandedLine = expandTabs(rawLine);
            if (expandedLine.isEmpty()) {
                lines.add("");
            } else {
                List<String> wrappedLines = wrapText(expandedLine, width - padding * 2);
                lines.addAll(wrappedLines);
            }
        }
    }

    private List<String> wrapText(String text, float maxWidth) {
        List<String> result = new ArrayList<>();
        GlyphLayout layout = new GlyphLayout();

        if (text == null || text.isEmpty()) {
            result.add("");
            return result;
        }

        layout.setText(font, text);

        if (layout.width <= maxWidth) {
            result.add(text);
            return result;
        }

        StringBuilder currentLine = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String testLine = currentLine.toString() + c;
            layout.setText(font, testLine);

            if (layout.width <= maxWidth) {
                currentLine.append(c);
            } else {
                if (currentLine.length() > 0) {
                    result.add(currentLine.toString());
                    currentLine = new StringBuilder();
                    currentLine.append(c);
                } else {
                    result.add(String.valueOf(c));
                }
            }
        }

        if (currentLine.length() > 0) {
            result.add(currentLine.toString());
        }

        return result;
    }

    private List<String> splitLongWord(String word, float maxWidth, GlyphLayout layout) {
        List<String> result = new ArrayList<>();
        StringBuilder currentPart = new StringBuilder();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            String testPart = currentPart.toString() + c;
            layout.setText(font, testPart);

            if (layout.width <= maxWidth) {
                currentPart.append(c);
            } else {
                if (currentPart.length() > 0) {
                    result.add(currentPart.toString());
                    currentPart = new StringBuilder(String.valueOf(c));
                } else {
                    result.add(String.valueOf(c));
                }
            }
        }

        if (currentPart.length() > 0) {
            result.add(currentPart.toString());
        }

        return result;
    }

    @Override
    public void render(SpriteBatch batch) {
        if (width != lastWidth) {
            rewrapAllLines();
            lastWidth = width;
        }

        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        batch.begin();

        float visibleTop = y + height - padding;
        float visibleBottom = y + padding;
        float currentY = visibleTop;

        int totalLines = lines.size();
        int maxVisibleLines = (int) ((height - padding * 2 - lineHeight * 2) / lineHeight);

        int startLine = Math.max(0, totalLines - maxVisibleLines - scrollOffset);
        int endLine = Math.min(totalLines, startLine + maxVisibleLines);

        for (int i = startLine; i < endLine; i++) {
            String line = lines.get(i);
            font.draw(batch, line, x + padding, currentY);
            currentY -= lineHeight;
        }

        String inputLine = getPromptPrefix() + currentInput.toString() + (showCursor ? "_" : "");
        font.draw(batch, inputLine, x + padding, y + padding + lineHeight);

    }

    @Override
    public void handleInput() {
        cursorTimer += Gdx.graphics.getDeltaTime();
        float CURSOR_BLINK_INTERVAL = 0.5f;
        if (cursorTimer >= CURSOR_BLINK_INTERVAL) {
            showCursor = !showCursor;
            cursorTimer = 0f;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mouseScrollAccumulator = 0;
        }
    }

    void appendCharacter(char c) {
        String testInput = getPromptPrefix() + currentInput.toString() + c + "_";
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, testInput);

        if (layout.width <= width - padding * 2) {
            currentInput.append(c);
        }
    }

    void deleteLastCharacter() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
        }
    }

    void submitCommand() {
        executeCommand(currentInput.toString());
        currentInput.setLength(0);
    }

    @Override
    public void onScroll(float amountX, float amountY) {
        int maxVisibleLines = (int) ((height - padding * 2) / lineHeight);
        int maxScrollOffset = Math.max(0, lines.size() - maxVisibleLines);

        scrollOffset -= (int) amountY;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }

    public com.badlogic.gdx.InputProcessor getInputProcessor() {
        return inputProcessor;
    }

    private static class ConsoleInputProcessor extends InputAdapter {
        private final Console console;

        public ConsoleInputProcessor(Console console) {
            this.console = console;
        }

        @Override
        public boolean keyTyped(char character) {
            if (!console.isActive()) {
                return false;
            }

            if (character == '\r' || character == '\n') {
                console.submitCommand();
                return true;
            }

            if (character == '\b') {
                console.deleteLastCharacter();
                return true;
            }

            if (character >= 32 && character != 127) {
                console.appendCharacter(character);
                return true;
            }

            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            if (!console.isActive()) {
                return false;
            }
            console.onScroll(amountX, amountY);
            return true;
        }
    }

    private void executeCommand(String command) {
        if (command.trim().isEmpty()) {
            output(">");
            return;
        }

        output("> " + command);

        String result = processCommand(command.trim());
        if (result != null && !result.isEmpty()) {
            output(result);
            output("");
            scrollOffset = 0;
        }

    }

    private String processCommand(String lineCommand) {

        if (lineCommand.isEmpty())
            return "";

        String command = lineCommand.split(" ")[0].toLowerCase();
        String[] arguments = Arrays.stream(lineCommand.split(" ")).skip(1).toArray(String[]::new);

        if (commands.containsKey(command.toLowerCase())) {
            String result = commands.get(command.toLowerCase()).execute(arguments).toString();
            if (result.startsWith("#")) {
                if (result.substring(1).equals("clear")) {
                    clear();
                    return "";
                }
            } else {
                return result;
            }
        }
        return "\"" + command + "\" Неизвестная команда, попробуйте 'help'.";
    }


    @Override
    public void onWatcherEvent() {
        String log = WatcherSystem.getInstance().getLastConnectionLog();
        if (log != null) {
            Gdx.app.postRunnable(() -> {
                output(log);
            });
        }
    }

    public void clear() {
        lines.clear();
        rawLines.clear();
        output(OS_NAME + " [" + VERSION + "]");
        output("(c) Navi. Tachibana General Laboratories. All rights reserved.");
        output("");
    }

    public void setFontColor(Color color) {
        font.setColor(color);
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    public void setLineSpacing(float spacing) {
        this.lineSpacing = spacing;
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, "Test");
        lineHeight = layout.height + lineSpacing;
    }

    public void setFontSize(int size) {
        if (font != null) {
            font.dispose();
        }

        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/arial.ttf")
            );

            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

            parameter.size = size;
            parameter.color = Color.GREEN;
            parameter.characters = getRussianCharacters();

            font = generator.generateFont(parameter);
            generator.dispose();

            GlyphLayout layout = new GlyphLayout();
            layout.setText(font, "Test");
            lineHeight = layout.height + lineSpacing;

        } catch (Exception e) {
        }
    }

    public FileSystemContext getFileSystemContext() {
        return fileSystemContext;
    }

    public void setPathWithUsingFSM(FileSystemContext ctx) {
        this.fileSystemContext.setCurrentDirectory(ctx.getCurrentDirectory());
    }

    @Override
    public void dispose() {
        WatcherSystem.getInstance().unregisterListener(this);
        if (fileSystemContext != null) {
            fileSystemContext.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
