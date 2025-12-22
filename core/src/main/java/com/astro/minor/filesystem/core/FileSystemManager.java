package com.astro.minor.filesystem.core;

import com.astro.minor.filesystem.items.Directory;
import com.astro.minor.filesystem.items.FileSystemItem;
import com.astro.minor.filesystem.items.MyFile;
import com.astro.minor.filesystem.observers.FileSystemObserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class FileSystemManager {
    private static FileSystemManager instance;

    private Directory root;
    private String rootPath;
    private List<FileSystemObserver> observers;

    private FileSystemManager(String rootPath) {
        this.rootPath = rootPath;
        this.root = buildFileTree(rootPath);
        this.observers = new ArrayList<>();
    }

    public static synchronized FileSystemManager getInstance() {
        if (instance == null) {
            instance = new FileSystemManager("./The Wired");
        }
        return instance;
    }

    public void addObserver(FileSystemObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(FileSystemObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (FileSystemObserver observer : observers) {
            observer.onFileSystemChanged();
        }
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


    public Directory findDirectoryByPath(String path) {
        String normalizedTarget = Paths.get(path).toAbsolutePath().normalize().toString();
        return findDirectoryRecursive(root, normalizedTarget);
    }

    private Directory findDirectoryRecursive(Directory dir, String targetPath) {
        String normalizedCurrent = Paths.get(dir.getPath()).toAbsolutePath().normalize().toString();

        if (normalizedCurrent.equals(targetPath)) {
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


    public void refresh() {
        this.root = buildFileTree(rootPath);
        notifyObservers();
    }


    public Directory getRoot() {
        return root;
    }

    public String getRootPath() {
        return rootPath;
    }

    
    public boolean touch(Directory directory, String filename) {
        if (directory == null || filename == null || filename.isEmpty()) {
            return false;
        }

        try {
            String dirPath = directory.getPath();
            if (!dirPath.endsWith("/") && !dirPath.endsWith("\\")) {
                dirPath += File.separator;
            }

            Path filePath = Paths.get(dirPath + filename);
            Files.createFile(filePath);

            refresh();
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка создания файла: " + e.getMessage());
            return false;
        }
    }

    
    public boolean mkdir(Directory parentDirectory, String dirName) {
        if (parentDirectory == null || dirName == null || dirName.isEmpty()) {
            return false;
        }

        try {
            String parentPath = parentDirectory.getPath();
            if (!parentPath.endsWith("/") && !parentPath.endsWith("\\")) {
                parentPath += File.separator;
            }

            Path newDirPath = Paths.get(parentPath + dirName);
            Files.createDirectory(newDirPath);

            refresh();
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка создания директории: " + e.getMessage());
            return false;
        }
    }

}
