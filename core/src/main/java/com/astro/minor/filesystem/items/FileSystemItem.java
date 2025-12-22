package com.astro.minor.filesystem.items;

import java.time.LocalDateTime;

public abstract class FileSystemItem {
    protected String name;
    protected String path;
    protected LocalDateTime created;
    protected LocalDateTime modified;
    protected long size;
    protected String permissions;

    public FileSystemItem(String name, String path) {
        this.name = name;
        this.path = path;
        this.created = LocalDateTime.now();
        this.modified = LocalDateTime.now();
        this.permissions = "rw-r--r--";
    }

    public abstract boolean isDirectory();
    public abstract String getType();
    public abstract String execute();

    public String getName() { return name; }
    public String getPath() { return path; }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getModified() { return modified; }
    public long getSize() { return size; }
    public String getPermissions() { return permissions; }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String printInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Имя: ").append(name).append("\n")
            .append("Путь: ").append(path).append("\n")
            .append("Тип: ").append(getType()).append("\n")
            .append("Размер: ").append(formatSize(size)).append("\n")
            .append("Права: ").append(permissions).append("\n")
            .append("Создан: ").append(created).append("\n")
            .append("Изменен: ").append(modified);
        return sb.toString();
    }

    protected String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    @Override
    public String toString() {
        return String.format("%s [%s] %s",
                isDirectory() ? "DIR" : "FILE",
                getType(),
                name);
    }
}
