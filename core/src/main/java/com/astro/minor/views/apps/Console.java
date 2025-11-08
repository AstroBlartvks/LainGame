package com.astro.minor.views.apps;

import com.astro.minor.views.apps.commands.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.*;

import static com.astro.minor.StarField.osName;
import static com.astro.minor.StarField.version;

public class Console extends Application {
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;
    private List<String> lines;
    private StringBuilder currentInput;
    private float padding = 15f;
    private float lineHeight;
    private float lineSpacing = 8f;
    private boolean showCursor = true;
    private float cursorTimer = 0f;
    private final float CURSOR_BLINK_INTERVAL = 0.5f;
    private final Map<String, Command> commands = new HashMap<>();

    public Console(int x, int y, int width, int height, String applicationName) {
        super(x, y, width, height, applicationName);
        initialize();
    }

    private void initialize() {
        commands.put("help", new Help());
        commands.put("time", new Time());
        commands.put("version", new Version());
        commands.put("echo", new Echo());
        commands.put("clear", new Clear());

        createFontFromTTF();
        shapeRenderer = new ShapeRenderer();
        lines = new ArrayList<>();
        currentInput = new StringBuilder();

        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, "TEST");
        lineHeight = layout.height + lineSpacing + 1;

        output(osName + " [" + version + "]");
        output("(c) Navi. Tachibana General Laboratories. All rights reserved.");
        output("");
    }

    private void createFontFromTTF() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/arial.ttf")
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
        chars.append("!@#$%^&*()_+-=[]{}|;:',.<>?/`~ \"");

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
            lines.add("");
            return;
        }

        if (text.contains("\n")) {
            String[] splitText = text.split("\n");
            for (String line : splitText) {
                List<String> wrappedLines = wrapText(line, width - padding * 2);
                lines.addAll(wrappedLines);
            }
        } else {
            List<String> wrappedLines = wrapText(text, width - padding * 2);
            lines.addAll(wrappedLines);
        }
    }

    private List<String> wrapText(String text, float maxWidth) {
        List<String> result = new ArrayList<>();
        GlyphLayout layout = new GlyphLayout();

        if (text == null || text.isEmpty()) {
            result.add("");
            return result;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) continue;

            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            layout.setText(font, testLine);

            if (layout.width <= maxWidth) {
                currentLine.append(currentLine.length() == 0 ? word : " " + word);
            } else {
                if (currentLine.length() > 0) {
                    result.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    result.addAll(splitLongWord(word, maxWidth, layout));
                    currentLine = new StringBuilder();
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
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();

        batch.begin();

        float visibleTop = y + height - padding;
        float visibleBottom = y + padding;
        float currentY = visibleTop;

        int totalLines = lines.size();
        int maxVisibleLines = (int) ((height - padding * 2) / lineHeight);

        int startLine = Math.max(0, totalLines - maxVisibleLines);

        for (int i = startLine; i < totalLines; i++) {
            if (currentY < visibleBottom) break;

            String line = lines.get(i);
            font.draw(batch, line, x + padding, currentY);
            currentY -= lineHeight;
        }

        String inputLine = "> " + currentInput.toString() + (showCursor ? "_" : "");

        List<String> inputLines = wrapText(inputLine, width - padding * 2);
        for (int i = inputLines.size() - 1; i >= 0; i--) {
            if (currentY < visibleBottom) break;
            font.draw(batch, inputLines.get(i), x + padding, currentY);
            currentY -= lineHeight;
        }
    }

    @Override
    public void handleInput() {
        cursorTimer += Gdx.graphics.getDeltaTime();
        if (cursorTimer >= CURSOR_BLINK_INTERVAL) {
            showCursor = !showCursor;
            cursorTimer = 0f;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            executeCommand(currentInput.toString());
            currentInput.setLength(0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
            }
        }

        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char c = getCharFromKey(i);
                if (c != 0) {
                    currentInput.append(c);
                }
            }
        }
    }

    private char getCharFromKey(int keycode) {
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
            return (char) ((shift ? 'A' : 'a') + (keycode - Input.Keys.A));
        }

        if (keycode >= Input.Keys.NUM_0 && keycode <= Input.Keys.NUM_9) {
            boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
            if (shift) {
                switch (keycode) {
                    case Input.Keys.NUM_1: return '!';
                    case Input.Keys.NUM_2: return '@';
                    case Input.Keys.NUM_3: return '#';
                    case Input.Keys.NUM_4: return '$';
                    case Input.Keys.NUM_5: return '%';
                    case Input.Keys.NUM_6: return '^';
                    case Input.Keys.NUM_7: return '&';
                    case Input.Keys.NUM_8: return '*';
                    case Input.Keys.NUM_9: return '(';
                    case Input.Keys.NUM_0: return ')';
                }
            }
            return (char) ('0' + (keycode - Input.Keys.NUM_0));
        }

        switch (keycode) {
            case Input.Keys.SPACE: return ' ';
            case Input.Keys.PERIOD: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '>' : '.';
            case Input.Keys.COMMA: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '<' : ',';
            case Input.Keys.MINUS: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '_' : '-';
            case Input.Keys.EQUALS: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '+' : '=';
            case Input.Keys.SLASH: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '?' : '/';
            case Input.Keys.BACKSLASH: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '|' : '\\';
            case Input.Keys.LEFT_BRACKET: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '{' : '[';
            case Input.Keys.RIGHT_BRACKET: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '}' : ']';
            case Input.Keys.SEMICOLON: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? ':' : ';';
            case Input.Keys.APOSTROPHE: return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? '"' : '\'';
        }

        return 0;
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
        }

    }

    private String processCommand(String lineCommand) {

        if (lineCommand.isEmpty())
            return "";

        String command = lineCommand.split(" ")[0].toLowerCase();
        String[] arguments = Arrays.stream(lineCommand.split(" ")).skip(1).toArray(String[]::new);

        if (commands.containsKey(command.toLowerCase())) {
            String result = commands.get(command.toLowerCase()).execute(arguments);
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
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }

    public void clear() {
        lines.clear();
        output(osName + " [" + version + "]");
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
}
