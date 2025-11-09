package com.astro.minor.views.apps.commands;

import com.astro.minor.fileOS.FileSystemManager;

public class Exec extends Command<String []>{
    private final FileSystemManager fileSystemManager;

    public Exec(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }

    @Override
    public String execute(String[] args) {
        if (args.length==0 || args[0].isEmpty() || args[0].isBlank()){
            return "Не указано имя файла!";
        }

        return fileSystemManager.execute(args[0].trim());
    }
}
