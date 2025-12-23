package com.astro.minor;

import com.astro.minor.system.apps.base.Application;

import com.astro.minor.audio.ComputerAmbience;
import com.astro.minor.audio.WatcherSystem;
import com.astro.minor.core.ServiceLocator;
import com.astro.minor.core.config.AppConfig;
import com.astro.minor.filesystem.core.FileSystemManager;
import com.astro.minor.lainAI.KoboldCppManager;
import com.astro.minor.signal.CognitiveStabilitySystem;
import com.astro.minor.signal.ProcessWatcher;
import com.astro.minor.signal.SignalGenerator;
import com.astro.minor.system.apps.base.AppExecutor;
import com.astro.minor.system.desktop.DesktopSystemOS;
import com.astro.minor.system.graphics.CRTShaderSystemVHS;
import com.astro.minor.system.graphics.StartupVideoPlayer;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class StarField extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera uiCamera;
    private AppExecutor appExecutor;
    private FileSystemManager systemFileManager;
    private DesktopSystemOS desktopSystemOS;
    private ComputerAmbience computerAmbience;
    private CRTShaderSystemVHS crtShader;
    private StartupVideoPlayer videoPlayer;
    private boolean videoFinished = false;

    @Override
    public void create() {
        batch = new SpriteBatch();

        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        computerAmbience = new ComputerAmbience();
        computerAmbience.start();

        if (!AppConfig.DEBUG_MODE) {
            videoPlayer = new StartupVideoPlayer("startmenu.mp4");
            videoPlayer.play();
        } else {
            videoFinished = true;
            initializeGame();
        }

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (appExecutor != null) {
                    appExecutor.handleScroll(amountX, amountY);
                }
                return true;
            }
            
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE && videoPlayer != null && !videoFinished) {
                    videoPlayer.stop();
                    videoFinished = true;
                    initializeGame();
                    return true;
                }
                return false;
            }
        });
    }

    private void initializeGame() {
        appExecutor = new AppExecutor(batch);
        systemFileManager = FileSystemManager.getInstance();
        Gdx.app.log("StarField", "Working Directory = " + System.getProperty("user.dir"));
        desktopSystemOS = new DesktopSystemOS(batch, uiCamera);
        
        ServiceLocator.setAppExecutor(appExecutor);
        ServiceLocator.setFileSystemManager(systemFileManager);
        ServiceLocator.setUiCamera(uiCamera);
        
        crtShader = CRTShaderSystemVHS.getInstance();
        crtShader.setComputerAmbience(computerAmbience);
        
        WatcherSystem.getInstance().start();
        SignalGenerator.getInstance();
        ProcessWatcher.getInstance();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (computerAmbience != null) {
            computerAmbience.update(Gdx.graphics.getDeltaTime());
        }
        
        WatcherSystem.getInstance().update(Gdx.graphics.getDeltaTime());
        CognitiveStabilitySystem.getInstance().update(Gdx.graphics.getDeltaTime());
        ProcessWatcher.getInstance().update(Gdx.graphics.getDeltaTime());
        
        if (crtShader != null) {
            crtShader.update(Gdx.graphics.getDeltaTime());
        }

        if (!videoFinished && videoPlayer != null) {
            batch.begin();
            videoPlayer.render(batch);
            batch.end();
            
            if (videoPlayer.isFinished()) {
                videoFinished = true;
                videoPlayer.dispose();
                videoPlayer = null;
                initializeGame();
            }
            return;
        }

        handleInput();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        uiCamera.update();
        
        if (crtShader != null) {
            crtShader.begin();
        }
        
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        desktopSystemOS.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        desktopSystemOS.run();
        batch.end();

        uiCamera.update();
        batch.begin();
        batch.setProjectionMatrix(uiCamera.combined);
        appExecutor.run();
        batch.end();
        
        if (crtShader != null) {
            crtShader.end(uiCamera);
        }
    }

    public void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
                Gdx.app.log("StarField", "Alt+F4 blocked - use window close button");
                return;
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(AppConfig.DEFAULT_WINDOW_WIDTH, AppConfig.DEFAULT_WINDOW_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            computerAmbience.setSystemLoad(AppConfig.HIGH_SYSTEM_LOAD);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            computerAmbience.setSystemLoad(AppConfig.DEFAULT_SYSTEM_LOAD);
        }
    }

    @Override
    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, width, height);
        if (crtShader != null) {
            crtShader.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        safeShutdown();
    }
    
    public void safeShutdown() {
        Gdx.app.log("StarField", "=== Safe Shutdown Initiated ===");
        
        Gdx.app.log("LainAI", "Forcing KoboldCpp shutdown...");
        try {
            KoboldCppManager manager = KoboldCppManager.getInstance();
            if (manager.isRunning()) {
                manager.stopKoboldCpp();
                Thread.sleep(2000);
            }
            Gdx.app.log("LainAI", "KoboldCpp shutdown complete");
        } catch (Exception e) {
            Gdx.app.error("LainAI", "Error stopping KoboldCpp", e);
        }
        
        try {
            if (batch != null) {
                batch.dispose();
                Gdx.app.log("StarField", "Batch disposed");
            }
        } catch (Exception e) {
            Gdx.app.error("StarField", "Error disposing batch", e);
        }
        
        try {
            if (desktopSystemOS != null) {
                desktopSystemOS.dispose();
                Gdx.app.log("StarField", "DesktopOS disposed");
            }
        } catch (Exception e) {
            Gdx.app.error("StarField", "Error disposing desktopSystemOS", e);
        }
        
        try {
            if (computerAmbience != null) {
                computerAmbience.dispose();
                Gdx.app.log("StarField", "ComputerAmbience disposed");
            }
        } catch (Exception e) {
            Gdx.app.error("StarField", "Error disposing computerAmbience", e);
        }
        
        try {
            if (videoPlayer != null) {
                videoPlayer.dispose();
                Gdx.app.log("StarField", "VideoPlayer disposed");
            }
        } catch (Exception e) {
            Gdx.app.error("StarField", "Error disposing videoPlayer", e);
        }
        
        try {
            if (appExecutor != null) {
                appExecutor.dispose();
                Gdx.app.log("StarField", "AppExecutor disposed");
            }
        } catch (Exception e) {
            Gdx.app.error("StarField", "Error disposing appExecutor", e);
        }
        
        try {
            if (crtShader != null) {
                crtShader.dispose();
                Gdx.app.log("StarField", "CRT Shader disposed");
            }
        } catch (Exception e) {
            Gdx.app.error("StarField", "Error disposing crtShader", e);
        }
        
        try {
            WatcherSystem.getInstance().dispose();
            Gdx.app.log("StarField", "WatcherSystem disposed");
        } catch (Exception e) {
            Gdx.app.error("StarField", "Error disposing WatcherSystem", e);
        }
        
        Gdx.app.log("StarField", "=== Safe Shutdown Complete ===");
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
