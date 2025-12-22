package com.astro.minor.core;

import com.astro.minor.filesystem.core.FileSystemManager;
import com.astro.minor.system.apps.base.AppExecutor;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class ServiceLocator {
    private static FileSystemManager fileSystemManager;
    private static AppExecutor appExecutor;
    private static OrthographicCamera uiCamera;
    
    private ServiceLocator() {
    }
    
    public static void setFileSystemManager(FileSystemManager manager) {
        fileSystemManager = manager;
    }
    
    public static FileSystemManager getFileSystemManager() {
        return fileSystemManager;
    }
    
    public static void setAppExecutor(AppExecutor executor) {
        appExecutor = executor;
    }
    
    public static AppExecutor getAppExecutor() {
        return appExecutor;
    }
    
    public static void setUiCamera(OrthographicCamera camera) {
        uiCamera = camera;
    }
    
    public static OrthographicCamera getUiCamera() {
        return uiCamera;
    }
}
