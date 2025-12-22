package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.core.FileSystemContext;

public class Mkdir extends Command<String[]> {
    private final FileSystemContext fileSystemContext;

    public Mkdir(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            this.status = CmdStatus.ERROR;
            return "Использование: mkdir <имя_папки>";
        }

        String dirName = args[0];

        if (fileSystemContext.mkdir(dirName)) {
            this.status = CmdStatus.OK;
            return "Директория создана: " + dirName;
        } else {
            this.status = CmdStatus.ERROR;
            return "Ошибка создания директории: " + dirName;
        }
    }
}

