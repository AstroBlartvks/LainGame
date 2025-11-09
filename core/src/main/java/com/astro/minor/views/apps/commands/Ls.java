package com.astro.minor.views.apps.commands;

import com.astro.minor.fileOS.FileSystemManager;

import java.util.Arrays;
import java.util.Objects;

public class Ls extends Command<String []> {
    private FileSystemManager fileSystemManager;

    public Ls(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            return fileSystemManager.ls();
        } else if (args[0].equals("-la")) {
            return fileSystemManager.ls(true);
        } else {
            return "Непонятные аргументы `"+ Arrays.toString(args) + "`. Будет выполнен ls\n" + fileSystemManager.ls(false);
        }
    }
}
