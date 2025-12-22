package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.core.FileSystemContext;

import java.util.Arrays;

public class Exec extends Command<String []>{
    private final FileSystemContext fileSystemContext;

    public Exec(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        if (args.length==0 || args[0].isEmpty() || args[0].isBlank()){
            return "Не указано имя файла!";
        }

        return fileSystemContext.execute(String.join(" ", args));
    }
}

