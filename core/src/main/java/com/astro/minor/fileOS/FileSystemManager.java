package com.astro.minor.fileOS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileSystemManager {
    private Directory currentDirectory;
    private Directory root;

    public FileSystemManager(String rootPath) {
        this.root = buildFileTree(rootPath);
        this.currentDirectory = root;
    }

    private Directory buildFileTree(String path) {
        return buildDirectory(Paths.get(path), null);
    }

    private Directory buildDirectory(Path dirPath, Directory parent) {
        String dirName = dirPath.getFileName() != null ?
                dirPath.getFileName().toString() : dirPath.toString();

        Directory directory = new Directory(dirName, dirPath.toString());
        directory.setParent(parent);

        try {
            List<Path> items = Files.list(dirPath)
                    .collect(Collectors.toList());

            for (Path item : items) {
                if (Files.isDirectory(item)) {
                    Directory subDir = buildDirectory(item, directory);
                    directory.addChild(subDir);
                } else {
                    MyFile file = new MyFile(
                            item.getFileName().toString(),
                            item.toString()
                    );
                    directory.addChild(file);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка чтения директории: " + dirPath);
        }

        return directory;
    }

    public boolean cd(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        if (path.equals("..")) {
            if (currentDirectory.getParent() != null) {
                currentDirectory = currentDirectory.getParent();
                return true;
            }
            return false;
        }

        if (path.equals("/") || path.equals("\\")) {
            currentDirectory = root;
            return true;
        }

        if (path.contains("/") || path.contains("\\")) {
            return cdCompoundPath(path);
        }

        for (FileSystemItem item : currentDirectory.getChildren()) {
            if (item.isDirectory() && item.getName().equals(path)) {
                currentDirectory = (Directory) item;
                return true;
            }
        }

        if (path.startsWith("/") || path.contains(":")) {
            Directory target = findDirectoryByPath(path);
            if (target != null) {
                currentDirectory = target;
                return true;
            }
        }

        return false;
    }

    private boolean cdCompoundPath(String compoundPath) {
        String normalizedPath = compoundPath.replace("\\", "/");

        String[] pathComponents = normalizedPath.split("/");

        Directory originalDirectory = currentDirectory;

        for (String component : pathComponents) {
            if (component.isEmpty() || component.equals(".")) {
                continue;
            }

            if (component.equals("..")) {
                if (currentDirectory.getParent() != null) {
                    currentDirectory = currentDirectory.getParent();
                } else {
                    currentDirectory = originalDirectory;
                    return false;
                }
            } else {
                boolean found = false;
                for (FileSystemItem item : currentDirectory.getChildren()) {
                    if (item.isDirectory() && item.getName().equals(component)) {
                        currentDirectory = (Directory) item;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    currentDirectory = originalDirectory;
                    return false;
                }
            }
        }

        return true;
    }

    private Directory findDirectoryByPath(String path) {
        return findDirectoryRecursive(root, Paths.get(path).normalize().toString());
    }

    private Directory findDirectoryRecursive(Directory dir, String targetPath) {
        if (dir.getPath().equals(targetPath)) {
            return dir;
        }

        for (FileSystemItem item : dir.getChildren()) {
            if (item.isDirectory()) {
                Directory found = findDirectoryRecursive((Directory) item, targetPath);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    public String ls() {
        return ls(false);
    }

    public String ls(boolean showDetails) {
        StringBuilder result = new StringBuilder();

        result.append("Текущая директория: ").append(currentDirectory.getPath()).append("\n");
        result.append("Содержимое:\n");

        List<FileSystemItem> dirs = currentDirectory.getDirectories();
        List<FileSystemItem> files = currentDirectory.getFiles();

        if (showDetails) {
            result.append("\nПапки (").append(dirs.size()).append("):\n");
            for (FileSystemItem dir : dirs) {
                result.append("  [DIR] ").append(dir.getName()).append("\n");
            }

            result.append("\nФайлы (").append(files.size()).append("):");
            for (FileSystemItem file : files) {
                result.append("  [FILE] ")
                    .append(file.getName())
                    .append(" (")
                    .append(String.format("%.2f", file.getSize() / 1024.0))
                    .append(" KB)\n");
            }
        } else {
            for (FileSystemItem dir : dirs) {
                result.append("[DIR]  ").append(dir.getName()).append("\n");
            }
            for (FileSystemItem file : files) {
                result.append("[FILE] ").append(file.getName()).append("\n");
            }
        }
        return result.toString();
    }

    public void pwd() {
        System.out.println("Текущий путь: " + currentDirectory.getPath());
    }

    public Directory getCurrentDirectory() {
        return currentDirectory;
    }

    public void refresh() {
        this.root = buildFileTree(root.getPath());
        Directory refreshedCurrent = findDirectoryByPath(currentDirectory.getPath());
        if (refreshedCurrent != null) {
            currentDirectory = refreshedCurrent;
        }
    }

    public String execute(String filename) {
        Optional<MyFile> file = currentDirectory.findFile(filename);
        if (file.isPresent()) {
            return file.get().execute();
        }
        return "Файл не найден!";
    }

    public String info(String name) {
        Optional<MyFile> file = currentDirectory.findFile(name);
        if (file.isPresent()) {
            return file.get().printInfo();
        }

        Optional<Directory> dir = currentDirectory.findDirectory(name);
        if (dir.isPresent()) {
            return dir.get().printInfo();
        }

        if (name.equals(".")) {
            return currentDirectory.printInfo();
        }

        return ("Элемент не найден: " + name);
    }

    public String find(String filename) {
        List<MyFile> foundFiles = findFilesRecursive(currentDirectory, filename);
        if (foundFiles.isEmpty()) {
            return ("Файлы не найдены: " + filename);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Найдено файлов: ").append(foundFiles.size()).append("\n");
            for (MyFile file : foundFiles) {
                sb.append("  ").append(file.getPath()).append("\n");
            }
            return sb.toString();
        }
    }

    private List<MyFile> findFilesRecursive(Directory dir, String filename) {
        List<MyFile> found = dir.getFiles().stream()
                .map(item -> (MyFile) item)
                .filter(file -> file.getName().equals(filename))
                .collect(Collectors.toList());

        for (FileSystemItem subdir : dir.getDirectories()) {
            found.addAll(findFilesRecursive((Directory) subdir, filename));
        }

        return found;
    }

}
