package com.astro.minor.filesystem.network;

import com.astro.minor.filesystem.items.Directory;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkManager {
    private static final String THE_WIRED_PATH = "./The Wired";
    private Map<String, NetworkHost> hostsByIp;
    private Map<String, NetworkHost> hostsByUsername;
    private NetworkHost currentHost;

    public NetworkManager() {
        this.hostsByIp = new HashMap<>();
        this.hostsByUsername = new HashMap<>();
        scanNetwork();
    }

    public void scanNetwork() {
        hostsByIp.clear();
        hostsByUsername.clear();

        FileHandle wiredDir = Gdx.files.local(THE_WIRED_PATH);
        if (!wiredDir.exists() || !wiredDir.isDirectory()) {
            System.err.println("[NetworkManager] The Wired directory not found!");
            return;
        }

        for (FileHandle userDir : wiredDir.list()) {
            if (userDir.isDirectory()) {
                NetworkHost host = loadHost(userDir);
                if (host != null) {
                    hostsByIp.put(host.getIpAddress(), host);
                    hostsByUsername.put(host.getUsername().toLowerCase(), host);
                }
            }
        }

        if (currentHost == null && hostsByUsername.containsKey("lain")) {
            currentHost = hostsByUsername.get("lain");
        }
    }

    private NetworkHost loadHost(FileHandle userDir) {
        String username = userDir.name();
        String rootPath = THE_WIRED_PATH + "/" + username;

        FileHandle systemDir = userDir.child("System");
        if (!systemDir.exists() || !systemDir.isDirectory()) {
            return null;
        }

        FileHandle hostFile = systemDir.child("host");
        if (!hostFile.exists()) {
            return null;
        }

        String ipAddress = parseHostFile(hostFile);
        if (ipAddress == null) {
            return null;
        }

        NetworkHost host = new NetworkHost(username, ipAddress, rootPath);

        FileHandle infoFile = systemDir.child("info");
        if (infoFile.exists()) {
            parseInfoFile(infoFile, host);
        }

        FileHandle passFile = systemDir.child("pass");
        if (passFile.exists()) {
            String password = passFile.readString().trim();
            host.setPassword(password);
        }

        return host;
    }

    private String parseHostFile(FileHandle hostFile) {
        try {
            String content = hostFile.readString().trim();
            if (content.contains(".")) {
                String[] parts = content.split("\\s+");
                for (String part : parts) {
                    if (part.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                        return part;
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void parseInfoFile(FileHandle infoFile, NetworkHost host) {
        try {
            String content = infoFile.readString();
            BufferedReader reader = new BufferedReader(new StringReader(content));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || !line.contains("=")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key.toLowerCase()) {
                    case "hostname":
                        host.setHostname(value);
                        break;
                    case "description":
                        host.setDescription(value);
                        break;
                    case "status":
                        host.setStatus(value);
                        break;
                    case "protocol":
                        host.setProtocol(value);
                        break;
                }
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("[NetworkManager] Error parsing info file: " + e.getMessage());
        }
    }

    public List<NetworkHost> getAllHosts() {
        return new ArrayList<>(hostsByIp.values());
    }

    public NetworkHost getHostByIp(String ip) {
        return hostsByIp.get(ip);
    }

    public NetworkHost getHostByUsername(String username) {
        return hostsByUsername.get(username.toLowerCase());
    }

    public NetworkHost getCurrentHost() {
        return currentHost;
    }

    public void setCurrentHost(NetworkHost host) {
        this.currentHost = host;
    }

    public boolean connectToHost(String ipAddress, String password) {
        NetworkHost host = hostsByIp.get(ipAddress);
        if (host == null) {
            return false;
        }

        if (host.requiresAuth()) {
            if (password == null || !host.authenticate(password)) {
                return false;
            }
        }

        currentHost = host;
        return true;
    }

    public String generateNmapOutput() {
        StringBuilder sb = new StringBuilder();
        sb.append("Сканирование The Wired...\n");
        sb.append("Найдено хостов: ").append(hostsByIp.size()).append("\n\n");

        int index = 1;
        for (NetworkHost host : hostsByIp.values()) {
            boolean isCurrent = host.equals(currentHost);
            String currentMark = isCurrent ? " [ВЫ ЗДЕСЬ]" : "";
            String statusRu = host.getStatus() != null ? host.getStatus() : "НЕИЗВЕСТНО";
            String accessRu = host.requiresAuth() ? "ТРЕБУЕТСЯ ПАРОЛЬ" : "ОТКРЫТ";
            
            sb.append("[").append(index).append("] ")
              .append(host.getIpAddress())
              .append(" - ")
              .append(host.getUsername())
              .append(" [").append(statusRu).append("]")
              .append(currentMark)
              .append("\n");
            
            if (host.getHostname() != null) {
                sb.append("    Hostname: ").append(host.getHostname()).append("\n");
            }
            
            if (host.getDescription() != null && !host.getDescription().isEmpty()) {
                sb.append("    Описание: ").append(host.getDescription()).append("\n");
            }
            
            if (host.getProtocol() != null) {
                sb.append("    Протокол: ").append(host.getProtocol()).append("\n");
            }
            
            sb.append("    Доступ: ").append(accessRu).append("\n");
            sb.append("\n");
            
            index++;
        }

        return sb.toString();
    }
}
