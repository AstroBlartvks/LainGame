package com.astro.minor.system.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class StartupVideoPlayer {
    private FFmpegFrameGrabber grabber;
    private Java2DFrameConverter converter;
    private Texture currentFrame;
    private String videoPath;
    private boolean finished = false;
    private float elapsedTime = 0f;
    private static final float VIDEO_DURATION = 4.0f;
    private boolean isPlaying = false;
    private boolean isReady = false;
    private Thread loadThread;
    private double frameDelay;
    private double nextFrameTime = 0;
    
    private SourceDataLine audioLine;
    private Thread audioThread;
    private volatile boolean shouldPlayAudio = false;
    private BlockingQueue<byte[]> audioBuffer;
    private static final int AUDIO_BUFFER_SIZE = 30;

    public StartupVideoPlayer(String videoPath) {
        this.videoPath = videoPath;
        this.converter = new Java2DFrameConverter();
        this.audioBuffer = new LinkedBlockingQueue<>(AUDIO_BUFFER_SIZE);
    }

    public void play() {
        loadThread = new Thread(() -> {
            try {
                String fullPath = Gdx.files.internal(videoPath).file().getAbsolutePath();
                
                grabber = new FFmpegFrameGrabber(fullPath);
                grabber.start();
                
                frameDelay = 1.0 / grabber.getFrameRate();
                
                initializeAudio();
                
                Frame testFrame = null;
                int attempts = 0;
                while (testFrame == null && attempts < 10) {
                    testFrame = grabber.grab();
                    if (testFrame == null) {
                        Thread.sleep(50);
                        attempts++;
                    }
                }
                
                if (testFrame != null) {
                    grabber.setFrameNumber(0);
                    isReady = true;
                    isPlaying = true;
                    shouldPlayAudio = true;
                    
                    startAudioPlayback();
                } else {
                    finished = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                finished = true;
            }
        });
        loadThread.start();
    }

    private void initializeAudio() {
        try {
            if (grabber.getAudioChannels() <= 0) {
                return;
            }

            int sampleRate = grabber.getSampleRate();
            int channels = grabber.getAudioChannels();

            AudioFormat audioFormat = new AudioFormat(sampleRate, 16, channels, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(audioFormat);
        } catch (Exception e) {
            System.err.println("Failed to initialize audio: " + e.getMessage());
        }
    }

    private void startAudioPlayback() {
        if (audioLine == null) {
            return;
        }

        audioThread = new Thread(() -> {
            try {
                audioLine.start();
                while (shouldPlayAudio && !finished) {
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
        audioThread.setDaemon(true);
        audioThread.start();
    }

    public void render(SpriteBatch batch) {
        if (!isReady || !isPlaying || finished || grabber == null) {
            return;
        }

        elapsedTime += Gdx.graphics.getDeltaTime();
        
        if (elapsedTime >= VIDEO_DURATION) {
            finished = true;
            return;
        }

        try {
            boolean frameUpdated = false;
            
            while (!frameUpdated && !finished) {
                if (elapsedTime < nextFrameTime) {
                    break;
                }
                
                Frame frame = grabber.grab();
                
                if (frame == null) {
                    finished = true;
                    return;
                }
                
                if (frame.image != null) {
                    BufferedImage image = converter.convert(frame);
                    
                    if (image != null) {
                        if (currentFrame != null) {
                            currentFrame.dispose();
                        }
                        
                        currentFrame = convertToTexture(image);
                        nextFrameTime += frameDelay;
                        frameUpdated = true;
                    }
                } else if (frame.samples != null && shouldPlayAudio) {
                    ShortBuffer samples = (ShortBuffer) frame.samples[0];
                    byte[] audioData = new byte[samples.remaining() * 2];

                    for (int i = 0; i < samples.remaining(); i++) {
                        short sample = samples.get(i);
                        audioData[i * 2] = (byte) (sample >> 8);
                        audioData[i * 2 + 1] = (byte) sample;
                    }

                    if (audioBuffer.size() < AUDIO_BUFFER_SIZE) {
                        audioBuffer.offer(audioData);
                    }
                }
            }
            
            if (currentFrame != null) {
                drawCurrentFrame(batch);
            }
        } catch (Exception e) {
            finished = true;
        }
    }

    private void drawCurrentFrame(SpriteBatch batch) {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float frameWidth = currentFrame.getWidth();
        float frameHeight = currentFrame.getHeight();
        
        float scaleX = screenWidth / frameWidth;
        float scaleY = screenHeight / frameHeight;
        float scale = Math.max(scaleX, scaleY);
        
        float renderWidth = frameWidth * scale;
        float renderHeight = frameHeight * scale;
        float x = (screenWidth - renderWidth) / 2;
        float y = (screenHeight - renderHeight) / 2;
        
        batch.draw(currentFrame, x, y, renderWidth, renderHeight);
    }

    private Texture convertToTexture(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        ByteBuffer pixelBuf = pixmap.getPixels();

        int[] pixels;
        if (image.getType() == BufferedImage.TYPE_INT_RGB || image.getType() == BufferedImage.TYPE_INT_ARGB) {
            pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels.length; i++) {
                int pixel = pixels[i];
                pixelBuf.put((byte) ((pixel >> 16) & 0xFF));
                pixelBuf.put((byte) ((pixel >> 8) & 0xFF));
                pixelBuf.put((byte) (pixel & 0xFF));
            }
        } else if (image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            byte[] pixels_byte = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            for (int i = 0; i < pixels_byte.length; i += 3) {
                pixelBuf.put(pixels_byte[i + 2]);
                pixelBuf.put(pixels_byte[i + 1]);
                pixelBuf.put(pixels_byte[i]);
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

    public boolean isFinished() {
        return finished;
    }

    public void stop() {
        finished = true;
        isPlaying = false;
        shouldPlayAudio = false;
        cleanup();
    }

    private void cleanup() {
        if (loadThread != null && loadThread.isAlive()) {
            try {
                loadThread.join(1000);
            } catch (Exception e) {
            }
        }
        
        shouldPlayAudio = false;
        
        if (audioThread != null && audioThread.isAlive()) {
            try {
                audioThread.join(200);
            } catch (Exception e) {
            }
        }
        
        if (audioLine != null) {
            if (audioLine.isRunning()) {
                audioLine.stop();
                audioLine.flush();
            }
            audioLine.close();
            audioLine = null;
        }
        
        if (currentFrame != null) {
            try {
                currentFrame.dispose();
            } catch (Exception e) {
            }
            currentFrame = null;
        }
        
        if (grabber != null) {
            try {
                grabber.release();
            } catch (Exception e) {
            } finally {
                grabber = null;
            }
        }
        
        if (converter != null) {
            try {
                converter.close();
            } catch (Exception e) {
            }
            converter = null;
        }
    }

    public void dispose() {
        isPlaying = false;
        finished = true;
        cleanup();
    }
}
