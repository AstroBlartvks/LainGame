package com.astro.minor.wired.events;


public class OnClickEvent extends WiredPageEvent {
    private final String functionName;

    public OnClickEvent(String functionName) {
        super("onclick");
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    @Override
    public void handle() {
        System.out.println("[OnClickEvent] onclick:" + functionName);
    }
}

