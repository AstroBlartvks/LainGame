package com.astro.minor.wired.events;


public abstract class WiredPageEvent {
    private final String type;
    private final long timestamp;

    protected WiredPageEvent(String type) {
        this.type = type;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public abstract void handle();
}

