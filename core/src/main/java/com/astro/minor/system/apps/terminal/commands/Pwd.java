package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.core.FileSystemContext;

public class Pwd extends Command <String []>{
    private final FileSystemContext fileSystemContext;

    public Pwd(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        return "Ты сейчас в директории:" + fileSystemContext.getCurrentDirectory().getPath();
    }
}

