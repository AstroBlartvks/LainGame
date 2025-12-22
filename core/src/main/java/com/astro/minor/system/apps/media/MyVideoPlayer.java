package com.astro.minor.system.apps.media;

import com.astro.minor.system.apps.base.Application;

import com.astro.minor.core.ServiceLocator;
import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.filesystem.items.MyFile;
import com.astro.minor.system.apps.explorer.FileExplorer;
import com.astro.minor.ui.*;
import com.astro.minor.system.graphics.CRTShaderSystemVHS;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MyVideoPlayer extends Application {
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Panel mainPanel;
    private Panel videoPanel;
    private Label fileNameLabel;
    private Label videoInfoLabel;
    private Button playPauseButton;
    private Button stopButton;
    private Button openButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;
    private Label volumeLabel;
    private Label volumePercentLabel;

    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter;
    private Texture currentFrameTexture;
    private String currentFileName = "";
    private String currentFilePath = "";
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private FileSystemContext fileSystemContext;
    private float videoDuration = 0f;
    private float currentPosition = 0f;
    private int videoWidth = 0;
    private int videoHeight = 0;
    private double frameRate = 30.0;
    private long videoStartTime = 0;
    private volatile boolean isBuffering = false;

    private static class TimedFrame {
        BufferedImage image;
        long timestampMicros;

        TimedFrame(BufferedImage image, long timestampMicros) {
            this.image = image;
            this.timestampMicros = timestampMicros;
        }
    }

    private BlockingQueue<TimedFrame> frameBuffer;
    private Thread decoderThread;
    private volatile boolean shouldDecode = false;
    private static final int BUFFER_SIZE = 10;

    private SourceDataLine audioLine;
    private volatile boolean shouldPlayAudio = false;
    private BlockingQueue<byte[]> audioBuffer;
    private static final int AUDIO_BUFFER_SIZE = 30;
    private Thread audioPlaybackThread;

    private final float padding = 8f;
    private final float buttonWidth = 65f;
    private final float buttonHeight = 30f;
    private final float controlsHeight = 120f;
    private final float minWidth = 640f;
    private final float minHeight = 480f;

    private static final Color LAIN_BG = new Color(0.0f, 0.02f, 0.08f, 0.85f);
    private static final Color LAIN_BG_DARK = new Color(0.02f, 0.02f, 0.02f, 0.9f);
    private static final Color LAIN_GREEN = new Color(0.0f, 1.0f, 0.25f, 1f);
    private static final Color LAIN_GREEN_DIM = new Color(0.0f, 0.7f, 0.2f, 0.9f);
    private static final Color LAIN_RED = new Color(1.0f, 0.0f, 0.2f, 1f);
    private static final Color LAIN_RED_DIM = new Color(0.7f, 0.0f, 0.15f, 0.9f);
    private static final Color LAIN_TEXT = new Color(0.75f, 0.75f, 0.75f, 1f);
    private static final Color LAIN_BORDER = new Color(0.0f, 0.6f, 0.15f, 0.6f);

    public MyVideoPlayer(int x, int y, int width, int height, String applicationName, OrthographicCamera camera) {
        super(x, y, width, height, applicationName, camera);
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.fileSystemContext = new FileSystemContext();
        this.frameBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);
        this.audioBuffer = new LinkedBlockingQueue<>(AUDIO_BUFFER_SIZE);
        this.converter = new Java2DFrameConverter();
        initialize();
    }

    public MyVideoPlayer(int x, int y, int width, int height, String applicationName, OrthographicCamera camera, String filePath) {
        super(x, y, width, height, applicationName, camera);
        this.shapeRenderer = new ShapeRenderer();
        this.font = new BitmapFont();
        this.fileSystemContext = new FileSystemContext();
        this.frameBuffer = new LinkedBlockingQueue<>(BUFFER_SIZE);
        this.audioBuffer = new LinkedBlockingQueue<>(AUDIO_BUFFER_SIZE);
        this.converter = new Java2DFrameConverter();
        initialize();
        loadVideoFile(filePath);
    }

    private void initialize() {
        mainPanel = new Panel(x, y, width, height);
        mainPanel.setBackgroundColor(LAIN_BG);
        mainPanel.setHoverColor(LAIN_BG);
        mainPanel.setBorderColor(LAIN_BORDER);
        mainPanel.setBorderWidth(2);

        createUI();
    }

    private void createUI() {
        float videoAreaHeight = height - controlsHeight;
        videoPanel = new Panel(x + padding, y + controlsHeight + padding, width - padding * 2, videoAreaHeight - padding * 2);
        videoPanel.setBackgroundColor(LAIN_BG_DARK);
        videoPanel.setHoverColor(LAIN_BG_DARK);
        videoPanel.setBorderColor(LAIN_BORDER);
        videoPanel.setBorderWidth(2);
        mainPanel.addComponent(videoPanel);

        videoInfoLabel = new Label(x + padding * 2, y + height / 2, width - padding * 4, 60, "[NO VIDEO]");
        videoInfoLabel.setFont(16);
        videoInfoLabel.setTextColor(new Color(0.0f, 0.4f, 0.2f, 0.5f));
        videoInfoLabel.setDrawBackground(false);
        videoInfoLabel.setAlignment(Align.center);

        float contentWidth = width - padding * 2;
        float currentY = y + controlsHeight - padding;

        fileNameLabel = new Label(x + padding, currentY - 22, contentWidth, 22, "[NO FILE LOADED]");
        fileNameLabel.setFont(14);
        fileNameLabel.setTextColor(LAIN_GREEN);
        fileNameLabel.setDrawBackground(true);
        fileNameLabel.setBackgroundColor(LAIN_BG_DARK);
        fileNameLabel.setBorderColor(LAIN_BORDER);
        fileNameLabel.setBorderWidth(1);
        fileNameLabel.setAlignment(Align.center);
        fileNameLabel.setPadding(10f);
        currentY -= 27;

        currentTimeLabel = new Label(x + padding, currentY - 15, 50, 15, "0:00");
        currentTimeLabel.setFont(11);
        currentTimeLabel.setTextColor(LAIN_TEXT);
        currentTimeLabel.setAlignment(Align.left);

        totalTimeLabel = new Label(x + width - 50 - padding, currentY - 15, 50, 15, "0:00");
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
            if (videoDuration > 0 && slider.isDragging()) {
                seekToPosition(value * videoDuration);
            }
        });
        currentY -= 22;

        float centerX = x + width / 2;
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
        stopButton.setClickListener(btn -> stopVideo());

        openButton = new Button(buttonStartX + buttonWidth * 2 + padding * 2, buttonY, buttonWidth, buttonHeight, "LOAD");
        openButton.setBackgroundColor(LAIN_BG_DARK);
        openButton.setHoverColor(new Color(0.1f, 0.1f, 0.1f, 1f));
        openButton.setBorderColor(LAIN_TEXT);
        openButton.setBorderWidth(2);
        openButton.setTextColor(LAIN_TEXT);
        openButton.setFont(12);
        openButton.setClickListener(btn -> openFileExplorer());

        currentY = buttonY - 10;

        volumeLabel = new Label(x + padding, currentY - 15, 40, 15, "VOL");
        volumeLabel.setFont(10);
        volumeLabel.setTextColor(LAIN_TEXT);
        volumeLabel.setAlignment(Align.left);

        volumePercentLabel = new Label(x + width - 35 - padding, currentY - 15, 35, 15, "100%");
        volumePercentLabel.setFont(10);
        volumePercentLabel.setTextColor(LAIN_TEXT);
        volumePercentLabel.setAlignment(Align.right);

        float volumeSliderWidth = buttonWidth * 3 + padding * 2;
        volumeSlider = new Slider(buttonStartX, currentY - 15, volumeSliderWidth, 15, 0f, 1f);
        volumeSlider.setValue(1.0f);
        volumeSlider.setTrackColor(new Color(0.0f, 0.08f, 0.32f, 0.9f));
        volumeSlider.setFillColor(new Color(0.0f, 1.0f, 0.25f, 1.0f));
        volumeSlider.setThumbColor(LAIN_GREEN);
        volumeSlider.setThumbHoverColor(LAIN_GREEN);
        volumeSlider.setThumbRadius(5f);
        volumeSlider.setTrackHeight(4f);
        volumeSlider.setChangeListener((slider, value) -> {
            if (audioLine != null && audioLine.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float)(Math.log(Math.max(value, 0.0001)) / Math.log(10.0) * 20.0);
                gainControl.setValue(Math.max(dB, gainControl.getMinimum()));
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
        mainPanel.addComponent(videoInfoLabel);
    }

    private void togglePlayPause() {
        if (grabber == null) {
            return;
        }

        if (isPlaying && !isPaused) {
            System.out.println("Pausing playback");
            isPaused = true;
            shouldDecode = false;
            shouldPlayAudio = false;
            stopPlayback();
            playPauseButton.setText("[>]");
        } else {
            System.out.println("Starting playback from position: " + currentPosition);
            isPaused = false;
            isPlaying = true;
            isBuffering = false;
            videoStartTime = System.currentTimeMillis() - (long)(currentPosition * 1000);
            shouldDecode = true;
            shouldPlayAudio = true;
            startPlayback();
            playPauseButton.setText("[||]");
        }
    }

    private void stopVideo() {
        if (grabber != null) {
            isPaused = false;
            isPlaying = false;
            isBuffering = false;
            shouldDecode = false;
            shouldPlayAudio = false;
            stopPlayback();
            playPauseButton.setText("[>]");
            currentPosition = 0f;
            progressSlider.setValue(0f);
            currentTimeLabel.setText("0:00");

            try {
                grabber.setTimestamp(0);
                loadInitialFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void seekToPosition(float seconds) {
        if (grabber == null || videoDuration <= 0) return;

        try {
            boolean wasPlaying = isPlaying && !isPaused;
            if (wasPlaying) {
                shouldDecode = false;
                shouldPlayAudio = false;
                stopPlayback();
            }

            currentPosition = Math.max(0, Math.min(seconds, videoDuration));
            long timestampMicros = (long)(currentPosition * 1_000_000);

            System.out.println("Seeking to: " + currentPosition + "s (" + timestampMicros + " micros)");
            grabber.setTimestamp(timestampMicros);

            frameBuffer.clear();
            audioBuffer.clear();

            loadInitialFrame();

            currentTimeLabel.setText(formatTime(currentPosition));
            progressSlider.setValue(currentPosition / videoDuration);

            if (wasPlaying) {
                isBuffering = true;
                shouldDecode = true;
                shouldPlayAudio = false; // Audio will start after buffering
                startPlayback();

                new Thread(() -> {
                    try {
                        System.out.println("Buffering after seek...");
                        int waitCount = 0;
                        while (frameBuffer.size() < 3 && shouldDecode && waitCount < 100) {
                            Thread.sleep(50);
                            waitCount++;
                        }

                        if (shouldDecode) {
                            System.out.println("Buffer ready (" + frameBuffer.size() + " frames), resuming playback");
                            videoStartTime = System.currentTimeMillis() - (long)(currentPosition * 1000);
                            shouldPlayAudio = true;
                            isBuffering = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        isBuffering = false;
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openFileExplorer() {
        FileExplorer explorer = new FileExplorer(
            x + 50,
            y + 50,
            500,
            400,
            "Выбрать видео",
            camera
        );
        ServiceLocator.getAppExecutor().addApplication(explorer);
    }

    public void loadVideoFile(String filePath) {
        try {
            cleanupResources();

            currentFilePath = filePath;
            currentFileName = filePath.substring(Math.max(filePath.lastIndexOf("\\"), filePath.lastIndexOf("/")) + 1);
            String displayName = currentFileName.length() > 40 ?
                currentFileName.substring(0, 37) + "..." : currentFileName;
            fileNameLabel.setText(">> " + displayName.toUpperCase());
            fileNameLabel.setTextColor(LAIN_GREEN);

            grabber = new FFmpegFrameGrabber(filePath);
            grabber.start();

            videoDuration = (float)grabber.getLengthInTime() / 1_000_000f;
            videoWidth = grabber.getImageWidth();
            videoHeight = grabber.getImageHeight();
            frameRate = grabber.getFrameRate();

            if (videoDuration > 0) {
                totalTimeLabel.setText(formatTime(videoDuration));
            } else {
                totalTimeLabel.setText("...");
            }

            initializeAudio();

            loadInitialFrame();

            videoInfoLabel.setText("");
            currentTimeLabel.setText("0:00");
            progressSlider.setValue(0f);
            currentPosition = 0f;

            playPauseButton.setText("[>]");
            isPlaying = false;
            isPaused = false;

        } catch (Exception e) {
            fileNameLabel.setText("[ERROR] " + e.getMessage().toUpperCase());
            fileNameLabel.setTextColor(LAIN_RED);
            videoInfoLabel.setText("Failed to load video");
            e.printStackTrace();
            currentFilePath = "";
            videoDuration = 0f;
        }
    }

    private void initializeAudio() throws Exception {
        if (grabber.getAudioChannels() <= 0) {
            System.out.println("No audio track found");
            return;
        }

        int sampleRate = grabber.getSampleRate();
        int channels = grabber.getAudioChannels();

        System.out.println("Initializing audio: " + sampleRate + "Hz, " + channels + " channels");

        AudioFormat audioFormat = new AudioFormat(sampleRate, 16, channels, true, true);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        audioLine = (SourceDataLine) AudioSystem.getLine(info);
        audioLine.open(audioFormat);
    }

    private void loadInitialFrame() {
        try {
            Frame frame;
            int attempts = 0;
            while (attempts < 50) {
                frame = grabber.grab();
                if (frame == null) {
                    break;
                }

                if (frame.image != null) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    if (bufferedImage != null) {
                        if (currentFrameTexture != null) {
                            currentFrameTexture.dispose();
                        }
                        currentFrameTexture = convertBufferedImageToTexture(bufferedImage);
                        System.out.println("Initial frame loaded successfully, timestamp: " + frame.timestamp + "us");
                        break;
                    }
                }
                attempts++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPlayback() {
        stopPlayback();

        shouldDecode = true;
        shouldPlayAudio = (audioLine != null);

        decoderThread = new Thread(() -> {
            try {
                System.out.println("Decoder thread started");
                int frameCount = 0;
                int audioCount = 0;
                long startTime = System.currentTimeMillis();

                while (shouldDecode) {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        System.out.println("End of stream reached");
                        break;
                    }

                    if (frame.image != null) {
                        BufferedImage bufferedImage = converter.convert(frame);
                        if (bufferedImage != null) {
                            long frameTimestamp = frame.timestamp;

                            while (frameBuffer.size() >= BUFFER_SIZE && shouldDecode) {
                                Thread.sleep(5);
                            }

                            if (shouldDecode) {
                                frameBuffer.offer(new TimedFrame(bufferedImage, frameTimestamp));
                                frameCount++;
                                if (frameCount <= 3) {
                                    System.out.println("Video frame decoded: " + frameCount + ", timestamp: " + frameTimestamp + "us");
                                }
                            }
                        }
                    }

                    if (frame.samples != null && shouldPlayAudio) {
                        ShortBuffer samples = (ShortBuffer) frame.samples[0];
                        byte[] audioData = new byte[samples.remaining() * 2];

                        for (int i = 0; i < samples.remaining(); i++) {
                            short sample = samples.get(i);
                            audioData[i * 2] = (byte) (sample >> 8);
                            audioData[i * 2 + 1] = (byte) sample;
                        }

                        while (audioBuffer.size() >= AUDIO_BUFFER_SIZE && shouldDecode) {
                            Thread.sleep(5);
                        }
                        if (shouldDecode) {
                            audioBuffer.offer(audioData);
                            audioCount++;
                        }
                    }
                }
                System.out.println("Decoder thread stopped. Total frames: " + frameCount + ", audio: " + audioCount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        decoderThread.setDaemon(true);
        decoderThread.start();

        if (audioLine != null) {
            audioPlaybackThread = new Thread(() -> {
                try {
                    audioLine.start();
                    while (shouldPlayAudio) {
                        byte[] audioData = audioBuffer.poll();
                        if (audioData != null) {
                            audioLine.write(audioData, 0, audioData.length);
                        } else {
                            Thread.sleep(5);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            audioPlaybackThread.setDaemon(true);
            audioPlaybackThread.start();
        }
    }

    private void stopPlayback() {
        shouldDecode = false;
        shouldPlayAudio = false;

        if (decoderThread != null && decoderThread.isAlive()) {
            try {
                decoderThread.join(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (audioPlaybackThread != null && audioPlaybackThread.isAlive()) {
            try {
                audioPlaybackThread.join(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (audioLine != null && audioLine.isRunning()) {
            audioLine.stop();
            audioLine.flush();
        }

        frameBuffer.clear();
        audioBuffer.clear();
    }

    private void cleanupResources() {
        stopPlayback();

        if (audioLine != null) {
            audioLine.close();
            audioLine = null;
        }

        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            grabber = null;
        }

        if (currentFrameTexture != null) {
            currentFrameTexture.dispose();
            currentFrameTexture = null;
        }
    }

    private Texture convertBufferedImageToTexture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        ByteBuffer pixelBuf = pixmap.getPixels();

        int[] pixels;
        if (image.getType() == BufferedImage.TYPE_INT_RGB || image.getType() == BufferedImage.TYPE_INT_ARGB) {
            pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels.length; i++) {
                int pixel = pixels[i];
                pixelBuf.put((byte) ((pixel >> 16) & 0xFF)); // R
                pixelBuf.put((byte) ((pixel >> 8) & 0xFF));  // G
                pixelBuf.put((byte) (pixel & 0xFF));         // B
            }
        } else if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            byte[] pixels_byte = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels_byte.length; i += 3) {
                pixelBuf.put(pixels_byte[i + 2]); // R
                pixelBuf.put(pixels_byte[i + 1]); // G
                pixelBuf.put(pixels_byte[i]);     // B
            }
        } else {
            BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            rgbImage.getGraphics().drawImage(image, 0, 0, null);
            pixels = ((DataBufferInt) rgbImage.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels.length; i++) {
                int pixel = pixels[i];
                pixelBuf.put((byte) ((pixel >> 16) & 0xFF));
                pixelBuf.put((byte) ((pixel >> 8) & 0xFF));
                pixelBuf.put((byte) (pixel & 0xFF));
            }
        }

        pixelBuf.flip();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return texture;
    }

    public void loadVideoFromFSM(String fileName) {
        try {
            Optional<MyFile> fileOpt = fileSystemContext.getCurrentDirectory().findFile(fileName);
            if (fileOpt.isPresent()) {
                MyFile file = fileOpt.get();
                loadVideoFile(file.getPath());
            } else {
                fileNameLabel.setText("[ERROR] FILE NOT FOUND");
                fileNameLabel.setTextColor(LAIN_RED);
            }
        } catch (Exception e) {
            fileNameLabel.setText("[ERROR] " + e.getMessage().toUpperCase());
            fileNameLabel.setTextColor(LAIN_RED);
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


        if (isPlaying && !isPaused && grabber != null && videoDuration > 0) {
            if (isBuffering) {
                if (!progressSlider.isDragging()) {
                    float normalizedPos = Math.min(currentPosition / videoDuration, 1.0f);
                    progressSlider.setValue(normalizedPos);
                }
                currentTimeLabel.setText(formatTime(currentPosition) + " [...]");
            } else {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - videoStartTime;
                float targetPosition = elapsedTime / 1000.0f;

                if (targetPosition >= videoDuration) {
                    currentPosition = videoDuration;
                    isPlaying = false;
                    shouldDecode = false;
                    shouldPlayAudio = false;
                    playPauseButton.setText("[>]");
                    stopPlayback();
                } else {
                    currentPosition = targetPosition;
                    long currentTimestampMicros = (long)(currentPosition * 1_000_000);

                    TimedFrame nextFrame = frameBuffer.peek();
                    if (nextFrame != null) {
                        long frameDiff = nextFrame.timestampMicros - currentTimestampMicros;

                        if (frameDiff <= 50_000) { // 50ms tolerance
                            frameBuffer.poll(); // Remove from buffer
                            if (currentFrameTexture != null) {
                                currentFrameTexture.dispose();
                            }
                            currentFrameTexture = convertBufferedImageToTexture(nextFrame.image);
                        }
                    }

                    if (!progressSlider.isDragging()) {
                        float normalizedPos = Math.min(currentPosition / videoDuration, 1.0f);
                        progressSlider.setValue(normalizedPos);
                    }

                    currentTimeLabel.setText(formatTime(currentPosition));
                }
            }
        }

        mainPanel.render(batch, shapeRenderer);

        float videoAreaHeight = height - controlsHeight;
        float videoPanelX = x + padding;
        float videoPanelY = y + controlsHeight + padding;
        float videoPanelWidth = width - padding * 2;
        float videoPanelHeight = videoAreaHeight - padding * 2;

        if (currentFrameTexture != null) {
            float aspectRatio = (float) currentFrameTexture.getWidth() / currentFrameTexture.getHeight();
            float panelAspectRatio = videoPanelWidth / videoPanelHeight;

            float renderWidth, renderHeight;
            if (aspectRatio > panelAspectRatio) {
                renderWidth = videoPanelWidth;
                renderHeight = videoPanelWidth / aspectRatio;
            } else {
                renderHeight = videoPanelHeight;
                renderWidth = videoPanelHeight * aspectRatio;
            }

            float renderX = videoPanelX + (videoPanelWidth - renderWidth) / 2;
            float renderY = videoPanelY + (videoPanelHeight - renderHeight) / 2;

            batch.draw(currentFrameTexture, renderX, renderY, renderWidth, renderHeight);
        } else {
            videoInfoLabel.render(batch, shapeRenderer);
        }

        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float scanlineY = (System.currentTimeMillis() % 2000) / 2000f * videoPanelHeight;
        shapeRenderer.setColor(new Color(0.0f, 0.4f, 0.2f, 0.15f));
        shapeRenderer.rect(videoPanelX, videoPanelY + scanlineY, videoPanelWidth, 3);

        float scanline2Y = (System.currentTimeMillis() % 3500) / 3500f * videoPanelHeight;
        shapeRenderer.setColor(new Color(0.0f, 0.6f, 0.3f, 0.08f));
        shapeRenderer.rect(videoPanelX, videoPanelY + scanline2Y, videoPanelWidth, 2);


        shapeRenderer.end();
        batch.begin();
    }

    private void updatePositions() {
        width = (int) Math.max(width, minWidth);
        height = (int) Math.max(height, minHeight);

        mainPanel.setPosition(x, y);
        mainPanel.setSize(width, height);

        float videoAreaHeight = height - controlsHeight;
        videoPanel.setPosition(x + padding, y + controlsHeight + padding);
        videoPanel.setSize(width - padding * 2, videoAreaHeight - padding * 2);

        videoInfoLabel.setPosition(x + padding * 2, y + height / 2);
        videoInfoLabel.setSize(width - padding * 4, 60);

        float contentWidth = width - padding * 2;
        float centerX = x + width / 2;
        float currentY = y + controlsHeight - padding;

        fileNameLabel.setPosition(x + padding, currentY - 22);
        fileNameLabel.setSize(contentWidth, 30);
        currentY -= 27;

        currentTimeLabel.setPosition(x + padding, currentY - 15);
        totalTimeLabel.setPosition(x + width - 50 - padding, currentY - 15);

        progressSlider.setPosition(x + padding + 55, currentY - 15);
        progressSlider.setSize(contentWidth - 110, 15);
        currentY -= 22;

        float totalButtonWidth = buttonWidth * 3 + padding * 2;
        float buttonStartX = centerX - totalButtonWidth / 2;
        float buttonY = currentY - buttonHeight - 3;

        playPauseButton.setPosition(buttonStartX, buttonY);
        stopButton.setPosition(buttonStartX + buttonWidth + padding, buttonY);
        openButton.setPosition(buttonStartX + buttonWidth * 2 + padding * 2, buttonY);

        currentY = buttonY - 10;

        volumeLabel.setPosition(x + padding, currentY - 15);
        volumePercentLabel.setPosition(x + width - 35 - padding, currentY - 15);

        float volumeSliderWidth = buttonWidth * 3 + padding * 2;
        volumeSlider.setPosition(buttonStartX, currentY - 15);
        volumeSlider.setSize(volumeSliderWidth, 15);
    }

    @Override
    public void handleInput() {
    }

    @Override
    public void dispose() {
        cleanupResources();
        mainPanel.dispose();
        shapeRenderer.dispose();
        font.dispose();
        if (converter != null) {
            converter.close();
        }
    }
}
