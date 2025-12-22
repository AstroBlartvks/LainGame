package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.core.FileSystemContext;

public class Cd extends Command<String []>{
    private final FileSystemContext fileSystemContext;

    public Cd(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        if (args.length != 1) {
            return "Количество аргументов у cd = 1! Например: cd root или cd root/lain";
        }
        boolean result = fileSystemContext.cd(args[0]);
        if (result)
            return "Вы перешли в директорию: " + fileSystemContext.getCurrentDirectory().getPath();
        return "Директория не найдена: " + args[0];
    }
}

