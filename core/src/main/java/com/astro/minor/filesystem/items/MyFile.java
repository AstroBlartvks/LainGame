package com.astro.minor.filesystem.items;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyFile extends FileSystemItem {
    private String extension;
    private FileType fileType;

    public MyFile(String name, String path) {
        super(name, path);
        this.extension = extractExtension(name);
        this.fileType = determineFileType(extension);
        calculateSize();
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex + 1).toLowerCase() : "";
    }

    private FileType determineFileType(String ext) {
        switch (ext) {
            case "txt": case "md": case "log":
                return FileType.TEXT;
            case "java": case "py": case "js": case "cpp": case "c": case "html": case "css":
                return FileType.CODE;
            case "jpg": case "jpeg": case "png": case "gif": case "bmp":
                return FileType.IMAGE;
            case "mp3": case "wav": case "flac":
                return FileType.AUDIO;
            case "mp4": case "avi": case "mkv": case "mov":
                return FileType.VIDEO;
            case "wpml":
                return FileType.WPML;
            case "pdf": case "doc": case "docx":
                return FileType.DOCUMENT;
            case "zip": case "rar": case "tar": case "gz":
                return FileType.ARCHIVE;
            case "exe": case "jar": case "bat": case "sh":
                return FileType.EXECUTABLE;
            case "json":
                return FileType.JSON;
            default:
                return FileType.UNKNOWN;
        }
    }

    private void calculateSize() {
        try {
            Path filePath = Paths.get(this.path);
            if (Files.exists(filePath)) {
                this.size = Files.size(filePath);
            }
        } catch (Exception e) {
            this.size = 0;
        }
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getType() {
        return extension.isEmpty() ? "no-ext" : extension;
    }

    @Override
    public String execute() {
        return "Запуск файла: " + name + "\n" + fileType.execute(this);
    }

    public String getExtension() {
        return extension;
    }

    public FileType getFileType() {
        return fileType;
    }

    @Override
    public String printInfo() {

        return "=== Информация о файле ===\n" +
            super.printInfo() +
            "Расширение: " + (extension.isEmpty() ? "нет" : extension) + "\n" +
            "Категория: " + fileType.getDescription();
    }
}
