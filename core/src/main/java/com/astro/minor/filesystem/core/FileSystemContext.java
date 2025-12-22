package com.astro.minor.filesystem.core;

import com.astro.minor.filesystem.items.Directory;
import com.astro.minor.filesystem.items.FileSystemItem;
import com.astro.minor.filesystem.items.MyFile;
import com.astro.minor.filesystem.observers.FileSystemObserver;

import java.util.List;
import java.util.Optional;


public class FileSystemContext implements FileSystemObserver {
    private final FileSystemManager manager;
    private Directory currentDirectory;
    private Directory virtualRoot;  // Virtual root for network connections
    private String virtualRootPath; // Path to virtual root

    public FileSystemContext() {
        this.manager = FileSystemManager.getInstance();

        Directory root = manager.getRoot();
        Directory lainDir = null;

        for (FileSystemItem item : root.getChildren()) {
            if (item.isDirectory() && item.getName().equals("Lain")) {
                lainDir = (Directory) item;
                break;
            }
        }

        if (lainDir != null) {
            this.virtualRoot = lainDir;
            this.virtualRootPath = lainDir.getPath(); // Use actual path from directory
            this.currentDirectory = lainDir;
            System.out.println("[FileSystemContext] Initialized with Lain's directory: " + lainDir.getPath());
        } else {
            this.virtualRoot = manager.getRoot();
            this.virtualRootPath = null;
            this.currentDirectory = virtualRoot;
            System.out.println("[FileSystemContext] Warning: Lain directory not found, using global root");
        }

        manager.addObserver(this);
    }

    @Override
    public void onFileSystemChanged() {
        System.out.println("[FileSystemContext] === onFileSystemChanged START ===");
        System.out.println("[FileSystemContext] Old virtualRootPath: " + virtualRootPath);
        System.out.println("[FileSystemContext] Old currentDirectory: " + currentDirectory.getPath());

        Directory temp = currentDirectory;
        java.util.List<String> pathFromVirtualRoot = new java.util.ArrayList<>();

        while (temp != null && temp != virtualRoot && temp.getParent() != null) {
            pathFromVirtualRoot.add(0, temp.getName());
            temp = temp.getParent();
        }

        System.out.println("[FileSystemContext] Path components from virtual root: " + pathFromVirtualRoot);

        if (virtualRootPath != null) {

            String normalized = virtualRootPath.replace("\\", "/");
            String[] pathParts = normalized.split("/");

            System.out.println("[FileSystemContext] Rebuilding virtualRoot from path: " + normalized);
            System.out.println("[FileSystemContext] Path parts: " + java.util.Arrays.toString(pathParts));

            Directory wiredDir = null;
            for (FileSystemItem item : manager.getRoot().getChildren()) {
                if (item.isDirectory() && item.getName().equals("The Wired")) {
                    wiredDir = (Directory) item;
                    break;
                }
            }

            if (wiredDir == null) {
                wiredDir = manager.getRoot();
                System.out.println("[FileSystemContext] manager.getRoot() IS 'The Wired'");
            }

            String username = pathParts[pathParts.length - 1];
            System.out.println("[FileSystemContext] Looking for user: " + username);

            Directory userDir = null;
            for (FileSystemItem item : wiredDir.getChildren()) {
                if (item.isDirectory() && item.getName().equals(username)) {
                    userDir = (Directory) item;
                    break;
                }
            }

            if (userDir != null) {
                virtualRoot = userDir;
                virtualRootPath = userDir.getPath(); // Update to actual path
                System.out.println("[FileSystemContext] ✓ Virtual root refreshed: " + virtualRoot.getPath());
            } else {
                System.out.println("[FileSystemContext] ✗ User directory '" + username + "' not found, keeping old reference");
            }
        } else {
            virtualRoot = manager.getRoot();
            System.out.println("[FileSystemContext] Using global root");
        }

        Directory result = virtualRoot;
        for (String dirName : pathFromVirtualRoot) {
            boolean found = false;
            for (FileSystemItem item : result.getChildren()) {
                if (item.isDirectory() && item.getName().equals(dirName)) {
                    result = (Directory) item;
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("[FileSystemContext] ✗ Directory '" + dirName + "' not found! Staying at: " + result.getPath());
                currentDirectory = result;
                System.out.println("[FileSystemContext] === onFileSystemChanged END ===");
                return;
            }
        }

        currentDirectory = result;
        System.out.println("[FileSystemContext] ✓ Successfully navigated to: " + currentDirectory.getPath());
        System.out.println("[FileSystemContext] === onFileSystemChanged END ===");
    }

    public boolean cd(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        if (path.equals("..")) {
            if (currentDirectory.getParent() != null && currentDirectory != virtualRoot) {
                currentDirectory = currentDirectory.getParent();
                return true;
            }
            return false;
        }

        if (path.equals("/") || path.equals("\\")) {
            currentDirectory = virtualRoot;
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
            Directory target = manager.findDirectoryByPath(path);
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

    public String getVirtualPath() {
        String fullPath = currentDirectory.getPath();
        String rootPath = manager.getRootPath();
        String cleanRoot = rootPath.replace("\\", "/").replaceAll("^\\./", "");
        String cleanFull = fullPath.replace("\\", "/");

        if (cleanFull.contains(cleanRoot)) {
            String virtual = cleanFull.substring(cleanFull.indexOf(cleanRoot) + cleanRoot.length());
            if (virtual.isEmpty()) {
                return "./";
            }
            if (!virtual.startsWith("/")) {
                virtual = "/" + virtual;
            }
            return "./" + cleanRoot + virtual;
        }

        return "./";
    }

    public void setCurrentDirectory(Directory currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public void refresh() {
        manager.refresh();
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

    public boolean touch(String filename) {
        return manager.touch(currentDirectory, filename);
    }

    public boolean mkdir(String dirName) {
        return manager.mkdir(currentDirectory, dirName);
    }


    public void changeRoot(String newRootPath) {
        System.out.println("[FileSystemContext] Changing root to: " + newRootPath);

        String normalized = newRootPath.replace("\\", "/");
        String[] pathParts = normalized.split("/");

        System.out.println("[FileSystemContext] Path parts: " + java.util.Arrays.toString(pathParts));

        Directory wiredDir = null;
        for (FileSystemItem item : manager.getRoot().getChildren()) {
            if (item.isDirectory() && item.getName().equals("The Wired")) {
                wiredDir = (Directory) item;
                break;
            }
        }

        if (wiredDir == null) {
            wiredDir = manager.getRoot();
            System.out.println("[FileSystemContext] manager.getRoot() IS 'The Wired'");
        }

        String username = pathParts[pathParts.length - 1];
        System.out.println("[FileSystemContext] Looking for user: " + username);

        Directory userDir = null;
        for (FileSystemItem item : wiredDir.getChildren()) {
            if (item.isDirectory() && item.getName().equals(username)) {
                userDir = (Directory) item;
                break;
            }
        }

        if (userDir == null) {
            System.out.println("[FileSystemContext] ✗ User directory '" + username + "' not found!");
            return;
        }

        this.virtualRoot = userDir;
        this.virtualRootPath = userDir.getPath(); // Store actual path from directory
        this.currentDirectory = userDir;
        System.out.println("[FileSystemContext] ✓ Root changed to: " + virtualRoot.getPath());
    }

    public void dispose() {
        manager.removeObserver(this);
    }
}
