package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.core.FileSystemContext;

import java.util.Arrays;
import java.util.Objects;

public class Ls extends Command<String []> {
    private FileSystemContext fileSystemContext;

    public Ls(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            return fileSystemContext.ls();
        } else if (args[0].equals("-la")) {
            return fileSystemContext.ls(true);
        } else {
            return "Непонятные аргументы `"+ Arrays.toString(args) + "`. Будет выполнен ls\n" + fileSystemContext.ls(false);
        }
    }
}

