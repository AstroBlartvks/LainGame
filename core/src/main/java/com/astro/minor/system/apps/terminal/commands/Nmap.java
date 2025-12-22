package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.filesystem.network.NetworkManager;

public class Nmap extends Command<String[]> {
    private NetworkManager networkManager;

    public Nmap(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public String execute(String[] args) {
        if (args.length > 0 && args[0].equals("--rescan")) {
            networkManager.scanNetwork();
            return "Сеть пересканирована.\n\n" + networkManager.generateNmapOutput();
        }
        return networkManager.generateNmapOutput();
    }
}

