package com.astro.minor.views.apps.commands;

public abstract class Command {
    protected CmdStatus status;

    public CmdStatus getStatus() {
        return status;
    }

    public abstract <T> T execute(String[] args);
}
