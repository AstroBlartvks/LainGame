package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.core.FileSystemContext;

public class Touch extends Command<String[]> {
    private final FileSystemContext fileSystemContext;

    public Touch(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            this.status = CmdStatus.ERROR;
            return "Использование: touch <имя_файла>";
        }

        String filename = args[0];

        if (fileSystemContext.touch(filename)) {
            this.status = CmdStatus.OK;
            return "Файл создан: " + filename;
        } else {
            this.status = CmdStatus.ERROR;
            return "Ошибка создания файла: " + filename;
        }
    }
}

