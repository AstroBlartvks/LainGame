package com.astro.minor.system.apps.terminal.commands;

public abstract class Command<K> {
    protected CmdStatus status;

    public CmdStatus getStatus() {
        return status;
    }

    public abstract <T> T execute(K args);
}

