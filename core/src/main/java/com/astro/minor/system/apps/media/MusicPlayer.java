package com.astro.minor.system.apps.media;

import com.astro.minor.system.apps.base.Application;

import com.astro.minor.core.ServiceLocator;
import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.filesystem.items.MyFile;
import com.astro.minor.system.apps.explorer.FileExplorer;
import com.astro.minor.ui.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;

import java.io.File;
import java.util.Optional;

public class MusicPlayer extends Application {
    private ShapeRenderer shapeRenderer;
    private Panel mainPanel;
    private Label fileNameLabel;
    private Button playPauseButton;
    private Button stopButton;
    private Button openButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private Label volumeLabel;
    private Label volumePercentLabel;

    private Music currentMusic;
    private String currentFileName = "";
    private String currentFilePath = "";
    private boolean isPlaying = false;
    private FileSystemContext fileSystemContext;
    private boolean userSeekingProgress = false;
    private float musicDuration = 0f;
    private boolean durationInitialized = false;

    private final float padding = 8f;
    private final float buttonWidth = 65f;
    private final float buttonHeight = 30f;
    private final float minWidth = 480f;
    private final float minHeight = 120f;
    private final float volumeBarWidth = 20f;
    private final float volumeBarSegments = 12f;

    private static final Color LAIN_BG = new Color(0.0f, 0.02f, 0.08f, 0.85f);
    private static final Color LAIN_BG_DARK = new Color(0.02f, 0.02f, 0.02f, 0.9f);
    private static final Color LAIN_GREEN = new Color(0.0f, 1.0f, 0.25f, 1f);
    private static final Color LAIN_GREEN_DIM = new Color(0.0f, 0.7f, 0.2f, 0.9f);
    private static final Color LAIN_RED = new Color(1.0f, 0.0f, 0.2f, 1f);
    private static final Color LAIN_RED_DIM = new Color(0.7f, 0.0f, 0.15f, 0.9f);
    private static final Color LAIN_TEXT = new Color(0.75f, 0.75f, 0.75f, 1f);
    private static final Color LAIN_BORDER = new Color(0.0f, 0.6f, 0.15f, 0.6f);

    public MusicPlayer(int x, int y, int width, int height, String applicationName, OrthographicCamera camera) {
        super(x, y, width, 120, applicationName, camera); //height не юзаю, ибо нехуй высоту трогать, там пиздец иначе получается
        this.shapeRenderer = new ShapeRenderer();
        this.fileSystemContext = new FileSystemContext();
        initialize();
    }

    public MusicPlayer(int x, int y, int width, int height, String applicationName, OrthographicCamera camera, String filePath) {
        super(x, y, width, 120, applicationName, camera); //объяснение выше
        this.shapeRenderer = new ShapeRenderer();
        this.fileSystemContext = new FileSystemContext();
        initialize();
        loadMusicFile(filePath);
    }

    private void initialize() {
        setResizable(false);

        mainPanel = new Panel(x, y, width, height);
        mainPanel.setBackgroundColor(LAIN_BG);
        mainPanel.setHoverColor(LAIN_BG);
        mainPanel.setBorderColor(LAIN_BORDER);
        mainPanel.setBorderWidth(2);

        createUI();
    }

    private void createUI() {
        float contentWidth = width - volumeBarWidth - padding * 3;
        float centerX = x + contentWidth / 2;
        float currentY = y + height - padding;

        fileNameLabel = new Label(x + padding, currentY - 22, contentWidth - padding, 22, "[NO FILE LOADED]");
        fileNameLabel.setFont(14);
        fileNameLabel.setTextColor(LAIN_GREEN);
        fileNameLabel.setDrawBackground(true);
        fileNameLabel.setBackgroundColor(LAIN_BG_DARK);
        fileNameLabel.setBorderColor(LAIN_BORDER);
        fileNameLabel.setBorderWidth(1);
        fileNameLabel.setAlignment(Align.center);
        fileNameLabel.setPadding(10f);
        currentY -= 25;

        currentTimeLabel = new Label(x + padding, currentY - 15, 50, 15, "0:00");
        currentTimeLabel.setFont(11);
        currentTimeLabel.setTextColor(LAIN_TEXT);
        currentTimeLabel.setAlignment(Align.left);

        totalTimeLabel = new Label(x + contentWidth - 50, currentY - 15, 50, 15, "0:00");
        totalTimeLabel.setFont(11);
        totalTimeLabel.setTextColor(LAIN_TEXT);
        totalTimeLabel.setAlignment(Align.right);

        progressSlider = new Slider(x + padding + 55, currentY - 15, contentWidth - 110, 15, 0f, 1f);
        progressSlider.setTrackColor(new Color(0.0f, 0.08f, 0.32f, 0.9f));
        progressSlider.setFillColor(new Color(0.0f, 1.0f, 0.25f, 1.0f));
        progressSlider.setThumbColor(LAIN_GREEN);
        progressSlider.setThumbHoverColor(LAIN_GREEN);
        progressSlider.setThumbRadius(6f);
        progressSlider.setTrackHeight(6f);
        progressSlider.setChangeListener((slider, value) -> {
            if (currentMusic != null && musicDuration > 0 && slider.isDragging()) {
                userSeekingProgress = true;
                currentMusic.setPosition(value * musicDuration);
            }
        });
        currentY -= 20;

        float totalButtonWidth = buttonWidth * 3 + padding * 2;
        float buttonStartX = centerX - totalButtonWidth / 2;
        float buttonY = currentY - buttonHeight - 3;

        playPauseButton = new Button(buttonStartX, buttonY, buttonWidth, buttonHeight, "[>]");
        playPauseButton.setBackgroundColor(LAIN_BG_DARK);
        playPauseButton.setHoverColor(LAIN_GREEN_DIM);
        playPauseButton.setBorderColor(LAIN_GREEN);
        playPauseButton.setBorderWidth(2);
        playPauseButton.setTextColor(LAIN_GREEN);
        playPauseButton.setFont(13);
        playPauseButton.setClickListener(btn -> togglePlayPause());

        stopButton = new Button(buttonStartX + buttonWidth + padding, buttonY, buttonWidth, buttonHeight, "[#]");
        stopButton.setBackgroundColor(LAIN_BG_DARK);
        stopButton.setHoverColor(LAIN_RED_DIM);
        stopButton.setBorderColor(LAIN_RED);
        stopButton.setBorderWidth(2);
        stopButton.setTextColor(LAIN_RED);
        stopButton.setFont(13);
        stopButton.setClickListener(btn -> stopMusic());

        openButton = new Button(buttonStartX + buttonWidth * 2 + padding * 2, buttonY, buttonWidth, buttonHeight, "LOAD");
        openButton.setBackgroundColor(LAIN_BG_DARK);
        openButton.setHoverColor(new Color(0.1f, 0.1f, 0.1f, 1f));
        openButton.setBorderColor(LAIN_TEXT);
        openButton.setBorderWidth(2);
        openButton.setTextColor(LAIN_TEXT);
        openButton.setFont(12);
        openButton.setClickListener(btn -> openFileExplorer());

        currentY = buttonY - 8;

        volumeLabel = new Label(x + padding, currentY - 15, 40, 15, "VOL");
        volumeLabel.setFont(10);
        volumeLabel.setTextColor(LAIN_TEXT);
        volumeLabel.setAlignment(Align.left);

        volumePercentLabel = new Label(x + contentWidth - 35, currentY - 15, 35, 15, "70%");
        volumePercentLabel.setFont(10);
        volumePercentLabel.setTextColor(LAIN_TEXT);
        volumePercentLabel.setAlignment(Align.right);

        float volumeSliderWidth = buttonWidth * 3 + padding * 2;
        volumeSlider = new Slider(buttonStartX, currentY - 15, volumeSliderWidth, 15, 0f, 1f);
        volumeSlider.setValue(0.7f);
        volumeSlider.setTrackColor(new Color(0.0f, 0.08f, 0.32f, 0.9f));
        volumeSlider.setFillColor(new Color(0.0f, 1.0f, 0.25f, 1.0f));
        volumeSlider.setThumbColor(LAIN_GREEN);
        volumeSlider.setThumbHoverColor(LAIN_GREEN);
        volumeSlider.setThumbRadius(5f);
        volumeSlider.setTrackHeight(4f);
        volumeSlider.setChangeListener((slider, value) -> {
            if (currentMusic != null) {
                currentMusic.setVolume(value);
            }
            volumePercentLabel.setText(String.format("%d%%", (int)(value * 100)));
        });

        mainPanel.addComponent(fileNameLabel);
        mainPanel.addComponent(currentTimeLabel);
        mainPanel.addComponent(totalTimeLabel);
        mainPanel.addComponent(progressSlider);
        mainPanel.addComponent(playPauseButton);
        mainPanel.addComponent(stopButton);
        mainPanel.addComponent(openButton);
        mainPanel.addComponent(volumeLabel);
        mainPanel.addComponent(volumePercentLabel);
        mainPanel.addComponent(volumeSlider);
    }

    private void togglePlayPause() {
        if (currentMusic == null) {
            return;
        }

        if (isPlaying) {
            currentMusic.pause();
            isPlaying = false;
            playPauseButton.setText("[>]");
        } else {
            currentMusic.play();
            isPlaying = true;
            playPauseButton.setText("[||]");
        }
    }

    private void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            isPlaying = false;
            playPauseButton.setText("[>]");
        }
    }

    private void openFileExplorer() {
        FileExplorer explorer = new FileExplorer(
            x + 50,
            y + 50,
            500,
            400,
            "Выбрать музыку",
            camera
        );
        ServiceLocator.getAppExecutor().addApplication(explorer);
    }

    public void loadMusicFile(String filePath) {
        try {
            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic.dispose();
            }

            FileHandle fileHandle = Gdx.files.absolute(filePath);
            currentMusic = Gdx.audio.newMusic(fileHandle);

            currentFilePath = filePath;
            currentFileName = fileHandle.name();
            String displayName = currentFileName.length() > 40 ?
                currentFileName.substring(0, 37) + "..." : currentFileName;
            fileNameLabel.setText(">> " + displayName.toUpperCase());

            playPauseButton.setText("[>]");
            isPlaying = false;

            musicDuration = getMusicDuration(fileHandle.file().getPath());

            if (musicDuration > 0) {
                totalTimeLabel.setText(formatTime(musicDuration));
                durationInitialized = true;
            } else {
                totalTimeLabel.setText("...");
                durationInitialized = false;
                tryDetectDurationAsync();
            }

            currentMusic.setVolume(volumeSlider.getValue());
            currentTimeLabel.setText("0:00");
            progressSlider.setValue(0f);
        } catch (Exception e) {
            fileNameLabel.setText("[ERROR] " + e.getMessage().toUpperCase());
            currentMusic = null;
            musicDuration = 0f;
            durationInitialized = false;
        }
    }

    public float getMusicDuration(String filePath) {
        try {
            File file = new File(filePath);
            AudioFile audioFile = AudioFileIO.read(file);
            AudioHeader header = audioFile.getAudioHeader();
            return header.getTrackLength();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void tryDetectDurationAsync() {
        if (currentMusic != null && musicDuration <= 0 && !durationInitialized) {
            new Thread(() -> {
                try {
                    Music tempMusic = Gdx.audio.newMusic(Gdx.files.absolute(currentFilePath));
                    tempMusic.setVolume(0f);
                    tempMusic.play();

                    float maxWait = 5000;
                    float elapsed = 0;
                    while (elapsed < maxWait && tempMusic.getPosition() < 0.1f) {
                        Thread.sleep(50);
                        elapsed += 50;
                    }

                    if (tempMusic.getPosition() > 0) {
                        tempMusic.setPosition(999999f);
                        Thread.sleep(100);
                        final float detectedDuration = tempMusic.getPosition();

                        Gdx.app.postRunnable(() -> {
                            if (detectedDuration > 0 && !durationInitialized) {
                                musicDuration = detectedDuration;
                                totalTimeLabel.setText(formatTime(musicDuration));
                                durationInitialized = true;
                            }
                        });
                    }

                    tempMusic.dispose();
                } catch (Exception e) {
                    Gdx.app.postRunnable(() -> {
                        if (!durationInitialized) {
                            totalTimeLabel.setText("STREAM");
                        }
                    });
                }
            }).start();
        }
    }

    public void loadMusicFromFSM(String fileName) {
        try {
            Optional<MyFile> fileOpt = fileSystemContext.getCurrentDirectory().findFile(fileName);
            if (fileOpt.isPresent()) {
                MyFile file = fileOpt.get();
                loadMusicFile(file.getPath());
            } else {
                fileNameLabel.setText("[ERROR] FILE NOT FOUND");
            }
        } catch (Exception e) {
            fileNameLabel.setText("[ERROR] " + e.getMessage().toUpperCase());
        }
    }

    private String formatTime(float seconds) {
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        return String.format("%d:%02d", minutes, secs);
    }

    @Override
    public void render(SpriteBatch batch) {
        updatePositions();
        mainPanel.update(Gdx.graphics.getDeltaTime());
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (currentMusic != null) {
            float currentPos = currentMusic.getPosition();

            if (!progressSlider.isDragging()) {
                userSeekingProgress = false;
                if (musicDuration > 0) {
                    float normalizedPos = Math.min(currentPos / musicDuration, 1.0f);
                    progressSlider.setValue(normalizedPos);
                } else {
                    progressSlider.setValue(0f);
                }
            }

            currentTimeLabel.setText(formatTime(currentPos));

            if (isPlaying && !currentMusic.isPlaying()) {
                isPlaying = false;
                playPauseButton.setText("[>]");
                if (!durationInitialized && currentPos > 0) {
                    musicDuration = currentPos;
                    totalTimeLabel.setText(formatTime(musicDuration));
                    durationInitialized = true;
                }
            }
        }

        mainPanel.render(batch, shapeRenderer);


        renderVolumeBar(batch, shapeRenderer);
    }

    private void renderVolumeBar(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        boolean wasBatchDrawing = batch.isDrawing();
        if (wasBatchDrawing) {
            batch.end();
        }

        float barX = x + width - volumeBarWidth - padding;
        float barY = y + padding;
        float barHeight = height - padding * 2;

        float currentVolume = volumeSlider.getValue();
        float segmentHeight = (barHeight - (volumeBarSegments - 1) * 2) / volumeBarSegments;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(LAIN_BG_DARK);
        shapeRenderer.rect(barX, barY, volumeBarWidth, barHeight);

        for (int i = 0; i < volumeBarSegments; i++) {
            float segmentY = barY + i * (segmentHeight + 2);
            float segmentFillRatio = (i + 1) / volumeBarSegments;

            Color segmentColor;
            if (segmentFillRatio <= currentVolume) {
                if (i < volumeBarSegments * 0.5f) {
                    segmentColor = LAIN_GREEN;
                } else if (i < volumeBarSegments * 0.75f) {
                    segmentColor = new Color(0.7f, 0.9f, 0.0f, 1f);
                } else {
                    segmentColor = LAIN_RED;
                }
            } else {
                segmentColor = new Color(0.1f, 0.1f, 0.1f, 0.5f);
            }

            shapeRenderer.setColor(segmentColor);
            shapeRenderer.rect(barX + 2, segmentY, volumeBarWidth - 4, segmentHeight);
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(LAIN_BORDER);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.rect(barX, barY, volumeBarWidth, barHeight);
        shapeRenderer.end();

        if (!batch.isDrawing()) {
            batch.begin();
        }
    }

    private void updatePositions() {
        width = (int) Math.max(width, minWidth);
        height = (int) Math.max(height, minHeight);

        mainPanel.setPosition(x, y);
        mainPanel.setSize(width, height);

        float contentWidth = width - volumeBarWidth - padding * 3;
        float centerX = x + contentWidth / 2;
        float currentY = y + height - padding;

        fileNameLabel.setPosition(x + padding, currentY - 22);
        fileNameLabel.setSize(contentWidth - padding, 30);
        currentY -= 25;

        currentTimeLabel.setPosition(x + padding, currentY - 15);
        totalTimeLabel.setPosition(x + contentWidth - 50, currentY - 15);

        progressSlider.setPosition(x + padding + 55, currentY - 15);
        progressSlider.setSize(contentWidth - 110, 15);
        currentY -= 20;

        float totalButtonWidth = buttonWidth * 3 + padding * 2;
        float buttonStartX = centerX - totalButtonWidth / 2;
        float buttonY = currentY - buttonHeight - 3;

        playPauseButton.setPosition(buttonStartX, buttonY);
        stopButton.setPosition(buttonStartX + buttonWidth + padding, buttonY);
        openButton.setPosition(buttonStartX + buttonWidth * 2 + padding * 2, buttonY);

        currentY = buttonY - 8;

        volumeLabel.setPosition(x + padding, currentY - 15);
        volumePercentLabel.setPosition(x + contentWidth - 35, currentY - 15);

        float volumeSliderWidth = buttonWidth * 3 + padding * 2;
        volumeSlider.setPosition(buttonStartX, currentY - 15);
        volumeSlider.setSize(volumeSliderWidth, 15);
    }

    @Override
    public void handleInput() {
    }

    @Override
    public void dispose() {
        mainPanel.dispose();
        shapeRenderer.dispose();
        if (currentMusic != null) {
            currentMusic.dispose();
        }
    }
}
