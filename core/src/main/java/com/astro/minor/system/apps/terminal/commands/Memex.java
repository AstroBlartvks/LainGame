package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.core.ServiceLocator;
import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.system.apps.editor.MemexEditor;

public class Memex extends Command<String[]> {
    private final FileSystemContext fileSystemContext;

    public Memex(FileSystemContext fileSystemContext) {
        this.fileSystemContext = fileSystemContext;
    }

    @Override
    public String execute(String[] args) {
        String filePath = null;
        String workingDir = fileSystemContext.getVirtualPath();

        if (!workingDir.endsWith("/")) {
            workingDir += "/";
        }

        if (args.length > 0) {
            filePath = args[0];

            if (!filePath.startsWith("/") && !filePath.startsWith("./") && !filePath.contains(":\\")) {
                filePath = workingDir + filePath;
            }
        }

        MemexEditor editor = new MemexEditor(
            100, 100, 800, 600,
            "MEMEX EDITOR",
            ServiceLocator.getUiCamera(),
            workingDir,
            fileSystemContext
        );

        if (filePath != null) {
            editor.loadFile(filePath);
        }

        ServiceLocator.getAppExecutor().addApplication(editor);

        this.status = CmdStatus.OK;

        if (filePath != null) {
            return "[MEMEX EDITOR] Открыт: " + filePath;
        } else {
            return "[MEMEX EDITOR] Новый файл (рабочая директория: " + workingDir + ")";
        }
    }
}

