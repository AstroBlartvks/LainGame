package com.astro.minor.views.apps.commands;

import com.astro.minor.fileOS.FileSystemManager;

public class Pwd extends Command <String []>{
    private final FileSystemManager fileSystemManager;

    public Pwd(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }

    @Override
    public String execute(String[] args) {
        return "Ты сейчас в директории:" + fileSystemManager.getCurrentDirectory().getPath();
    }
}
