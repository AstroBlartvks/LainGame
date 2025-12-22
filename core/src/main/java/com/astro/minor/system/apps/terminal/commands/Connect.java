package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.core.FileSystemContext;
import com.astro.minor.filesystem.network.NetworkHost;
import com.astro.minor.filesystem.network.NetworkManager;
import com.astro.minor.system.apps.terminal.Console;

public class Connect extends Command<String[]> {
    private NetworkManager networkManager;
    private FileSystemContext fileSystemContext;
    private Console console;

    public Connect(NetworkManager networkManager, FileSystemContext fileSystemContext) {
        this.networkManager = networkManager;
        this.fileSystemContext = fileSystemContext;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    @Override
    public String execute(String[] args) {
        if (args.length == 0) {
            return "Использование: connect <ip_адрес> [пароль]\nПример: connect 76.97.105.110";
        }

        String ipAddress = args[0];
        String password = args.length > 1 ? args[1] : null;

        NetworkHost targetHost = networkManager.getHostByIp(ipAddress);
        if (targetHost == null) {
            return "Ошибка: Хост не найден: " + ipAddress + "\nИспользуйте 'nmap' для сканирования доступных хостов.";
        }

        if (targetHost.requiresAuth()) {
            if (password == null) {
                return "Ошибка: Данный хост требует аутентификации.\nИспользование: connect " + ipAddress + " <пароль>";
            }

            if (!targetHost.authenticate(password)) {
                return "Ошибка: Аутентификация не удалась. Неверный пароль.";
            }
        }

        boolean success = networkManager.connectToHost(ipAddress, password);
        if (success) {
            fileSystemContext.changeRoot(targetHost.getRootPath());
            networkManager.setCurrentHost(targetHost);

            if (console != null) {
                console.updatePrompt();
            }

            return String.format("Подключено к %s (%s)\nHostname: %s\nПротокол: %s\n",
                    targetHost.getUsername(),
                    targetHost.getIpAddress(),
                    targetHost.getHostname() != null ? targetHost.getHostname() : "N/A",
                    targetHost.getProtocol() != null ? targetHost.getProtocol() : "НЕИЗВЕСТНО"
            );
        }

        return "Ошибка: Не удалось подключиться.";
    }
}

