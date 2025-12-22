package com.astro.minor.filesystem.network;

import com.astro.minor.filesystem.items.Directory;

public class NetworkHost {
    private String username;
    private String ipAddress;
    private String hostname;
    private String description;
    private String status;
    private String protocol;
    private String password;
    private boolean requiresAuth;
    private String rootPath;

    public NetworkHost(String username, String ipAddress, String rootPath) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.rootPath = rootPath;
        this.requiresAuth = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.requiresAuth = password != null && !password.trim().isEmpty();
    }

    public boolean requiresAuth() {
        return requiresAuth;
    }

    public boolean authenticate(String inputPassword) {
        if (!requiresAuth) {
            return true;
        }
        return password != null && password.equals(inputPassword);
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %s",
                status != null ? status : "UNKNOWN",
                ipAddress,
                hostname != null ? hostname : username,
                protocol != null ? protocol : "UNKNOWN",
                description != null ? description : "No description"
        );
    }
}
