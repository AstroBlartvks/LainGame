package com.astro.minor.filesystem.items;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Directory extends FileSystemItem {
    private List<FileSystemItem> children;
    private Directory parent;

    public Directory(String name, String path) {
        super(name, path);
        this.children = new ArrayList<>();
        this.size = 0;
    }

    public void addChild(FileSystemItem item) {
        if (item instanceof Directory) {
            ((Directory) item).setParent(this);
        }
        children.add(item);
        updateModifiedTime();
    }

    public void removeChild(FileSystemItem item) {
        children.remove(item);
        updateModifiedTime();
    }

    public List<FileSystemItem> getChildren() {
        return new ArrayList<>(children);
    }

    public List<FileSystemItem> getDirectories() {
        return children.stream()
                .filter(FileSystemItem::isDirectory)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<FileSystemItem> getFiles() {
        return children.stream()
                .filter(item -> !item.isDirectory())
                .collect(java.util.stream.Collectors.toList());
    }

    public Optional<MyFile> findFile(String filename) {
        return children.stream()
                .filter(item -> !item.isDirectory() && item.getName().equals(filename))
                .map(item -> (MyFile) item)
                .findFirst();
    }

    public Optional<Directory> findDirectory(String dirname) {
        return children.stream()
                .filter(item -> item.isDirectory() && item.getName().equals(dirname))
                .map(item -> (Directory) item)
                .findFirst();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public String getType() {
        return "dir";
    }

    @Override
    public String execute() {
        return ("Открываю директорию: " + name + "\n") + printInfo();
    }

    public Directory getParent() {
        return parent;
    }

    public void setParent(Directory parent) {
        this.parent = parent;
    }

    private void updateModifiedTime() {
        this.modified = LocalDateTime.now();
    }

    public long calculateTotalSize() {
        return children.stream()
                .mapToLong(item -> item.isDirectory() ?
                        ((Directory) item).calculateTotalSize() : item.getSize())
                .sum();
    }

    @Override
    public String printInfo() {
        return "=== Информация о директории ===\n" +
            super.printInfo() + "\n" +
            "Количество элементов: " + children.size() + "\n" +
            "Поддиректории: " + getDirectories().size() + "\n" +
            "Файлы: " + getFiles().size() + "\n" +
            "Общий размер: " + formatSize(calculateTotalSize());
    }
}
