package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.core.ServiceLocator;

import com.astro.minor.StarField;
import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.system.apps.editor.MemexEditor;

public class Edit extends Command<String[]> {
    private final FileSystemContext fileSystemContext;

    public Edit(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            this.status = CmdStatus.ERROR;
            return "Использование: edit <файл>";
        }

        String filePath = args[0];
        String workingDir = fileSystemContext.getVirtualPath();

        if (!workingDir.endsWith("/")) {
            workingDir += "/";
        }

        if (!filePath.startsWith("/") && !filePath.startsWith("./") && !filePath.contains(":\\")) {
            filePath = workingDir + filePath;
        }

        MemexEditor editor = new MemexEditor(
            100, 100, 800, 600,
            "MEMEX EDITOR",
            ServiceLocator.getUiCamera(),
            workingDir,
            fileSystemContext
        );

        editor.loadFile(filePath);

        ServiceLocator.getAppExecutor().addApplication(editor);

        this.status = CmdStatus.OK;
        return "[MEMEX EDITOR] Открыт: " + filePath;
    }
}

