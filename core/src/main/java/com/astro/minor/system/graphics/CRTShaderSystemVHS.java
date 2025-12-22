package com.astro.minor.system.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.astro.minor.audio.WatcherSystem;
import com.astro.minor.audio.ComputerAmbience;

public class CRTShaderSystemVHS implements Disposable {
    private static CRTShaderSystemVHS instance;

    private ShaderProgram crtShader;
    private ShaderProgram vhsShader;
    private FrameBuffer frameBuffer;
    private SpriteBatch batch;

    private boolean enabled;
    private boolean colorCorrectionEnabled;
    private boolean vhsEnabled;

    private float time;

    private float scanlineIntensity = 0.15f;
    private float vignetteIntensity = 0.5f;
    private float bloomIntensity = 0.3f;
    private Vector2 curvature = new Vector2(0.0f, 0.0f);
    private float baseAberration = 0.002f;
    private float baseFlickerIntensity = 0.03f;
    private float maskIntensity = 0.05f;

    private float vhsIntensity = 0.7f;

    private float brightness = 1.0f;
    private float contrast = 1.05f;
    private float saturation = 0.9f;
    private Vector2 warmth = new Vector2(1.05f, 0.95f);

    private ComputerAmbience computerAmbience;

    private CRTShaderSystemVHS() {
        enabled = true;
        colorCorrectionEnabled = true;
        vhsEnabled = false;
        time = 0;

        loadShaders();
        createFrameBuffer();

        batch = new SpriteBatch();
        batch.setShader(crtShader);
    }

    public static CRTShaderSystemVHS getInstance() {
        if (instance == null) {
            instance = new CRTShaderSystemVHS();
        }
        return instance;
    }

    private void loadShaders() {
        ShaderProgram.pedantic = false;

        crtShader = new ShaderProgram(
            Gdx.files.internal("shaders/crt.vert"),
            Gdx.files.internal("shaders/crt.frag")
        );

        if (!crtShader.isCompiled()) {
            System.err.println("[CRTShader] Compilation failed!");
            System.err.println(crtShader.getLog());
            enabled = false;
        } else {
            System.out.println("[CRTShader] CRT shader compiled successfully!");
        }

        vhsShader = new ShaderProgram(
            Gdx.files.internal("shaders/crt.vert"),
            Gdx.files.internal("shaders/crt_vhs.frag")
        );

        if (!vhsShader.isCompiled()) {
            System.err.println("[VHSShader] Compilation failed!");
            System.err.println(vhsShader.getLog());
        } else {
            System.out.println("[VHSShader] VHS shader compiled successfully!");
        }
    }

    private void createFrameBuffer() {
        if (frameBuffer != null) {
            frameBuffer.dispose();
        }

        int w = Math.max(1, Gdx.graphics.getWidth());
        int h = Math.max(1, Gdx.graphics.getHeight());

        frameBuffer = new FrameBuffer(
            Pixmap.Format.RGBA8888,
            w,
            h,
            false
        );
    }

    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            createFrameBuffer();
        }
    }

    public void setComputerAmbience(ComputerAmbience ambience) {
        this.computerAmbience = ambience;
    }

    public void begin() {
        if (!enabled) return;
        frameBuffer.begin();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public void end(OrthographicCamera camera) {
        if (!enabled) return;

        frameBuffer.end();

        updateShaderParameters();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(
            frameBuffer.getColorBufferTexture(),
            0, 0,
            Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
            0, 0, 1, 1
        );
        batch.end();
    }

    public void update(float delta) {
        if (!enabled) return;

        time += delta;
    }

    private void updateShaderParameters() {
        ShaderProgram activeShader = vhsEnabled ? vhsShader : crtShader;
        activeShader.bind();

        activeShader.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        activeShader.setUniformf("u_time", time);

        if (vhsEnabled) {
            activeShader.setUniformf("u_vhsEnabled", 1.0f);
            activeShader.setUniformf("u_vhsIntensity", vhsIntensity);

            activeShader.setUniformf("u_scanlineIntensity", 0.0f);
            activeShader.setUniformf("u_vignetteIntensity", 0.0f);
            activeShader.setUniformf("u_bloomIntensity", 0.0f);
            activeShader.setUniformf("u_curvature", 0.0f, 0.0f);
            activeShader.setUniformf("u_aberration", 0.0f);
            activeShader.setUniformf("u_flickerIntensity", 0.0f);
            activeShader.setUniformf("u_maskIntensity", 0.0f);
            activeShader.setUniformf("u_colorEnabled", 0.0f);
            activeShader.setUniformf("u_brightness", 1.0f);
            activeShader.setUniformf("u_contrast", 1.0f);
            activeShader.setUniformf("u_saturation", 1.0f);
            activeShader.setUniformf("u_warmth", 1.0f, 1.0f, 1.0f);
        } else {
            activeShader.setUniformf("u_vhsEnabled", 0.0f);

            activeShader.setUniformf("u_scanlineIntensity", scanlineIntensity);
            activeShader.setUniformf("u_vignetteIntensity", vignetteIntensity);
            activeShader.setUniformf("u_bloomIntensity", bloomIntensity);
            activeShader.setUniformf("u_curvature", curvature.x, curvature.y);
            activeShader.setUniformf("u_maskIntensity", maskIntensity);

            float currentAberration = baseAberration;
            float currentFlicker = baseFlickerIntensity;

            if (WatcherSystem.getInstance().isBreathing() ||
                WatcherSystem.getInstance().isHeartbeatActive()) {
                currentAberration *= 2.5f;
            }

            if (computerAmbience != null) {
                float systemLoad = computerAmbience.getSystemLoad();
                if (systemLoad > 0.7f) {
                    currentFlicker *= (1.0f + systemLoad);
                }
            }

            activeShader.setUniformf("u_aberration", currentAberration);
            activeShader.setUniformf("u_flickerIntensity", currentFlicker);

            activeShader.setUniformf("u_colorEnabled", colorCorrectionEnabled ? 1.0f : 0.0f);
            activeShader.setUniformf("u_brightness", brightness);
            activeShader.setUniformf("u_contrast", contrast);
            activeShader.setUniformf("u_saturation", saturation);
            activeShader.setUniformf("u_warmth", warmth.x, 1.0f, warmth.y);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isColorCorrectionEnabled() {
        return colorCorrectionEnabled;
    }

    public void setColorCorrectionEnabled(boolean enabled) {
        this.colorCorrectionEnabled = enabled;
    }

    public boolean isVhsEnabled() {
        return vhsEnabled;
    }

    public void setVhsEnabled(boolean enabled) {
        this.vhsEnabled = enabled;
        batch.setShader(enabled ? vhsShader : crtShader);
    }

    public void setVhsLowEnabled(boolean enabled) {
        this.vhsEnabled = enabled;
        setVhsIntensity(0.2f);
        batch.setShader(enabled ? vhsShader : crtShader);
    }

    public void setVhsHighEnabled(boolean enabled) {
        this.vhsEnabled = enabled;
        setVhsIntensity(1.0f);
        batch.setShader(enabled ? vhsShader : crtShader);
    }

    public void setVhsIntensity(float intensity) {
        this.vhsIntensity = MathUtils.clamp(intensity, 0.0f, 1.0f);
    }

    @Override
    public void dispose() {
        if (crtShader != null) crtShader.dispose();
        if (vhsShader != null) vhsShader.dispose();
        if (frameBuffer != null) frameBuffer.dispose();
        if (batch != null) batch.dispose();
    }
}
