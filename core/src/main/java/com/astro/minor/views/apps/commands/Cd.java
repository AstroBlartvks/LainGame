package com.astro.minor.views.apps.commands;

import com.astro.minor.fileOS.FileSystemManager;

public class Cd extends Command<String []>{
    private final FileSystemManager fileSystemManager;

    public Cd(FileSystemManager fileSystemManager) {
        this.fileSystemManager = fileSystemManager;
    }

    @Override
    public String execute(String[] args) {
        if (args.length != 1) {
            return "Количество аргументов у cd = 1! Например: cd root или cd root/lain";
        }
        boolean result = fileSystemManager.cd(args[0]);
        if (result)
            return "Вы перешли в директорию: " + fileSystemManager.getCurrentDirectory().getPath();
        return "Директория не найдена: " + args[0];
    }
}
