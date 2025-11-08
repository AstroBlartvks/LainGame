package com.astro.minor.views.apps.commands;

public abstract class Command<K> {
    protected CmdStatus status;

    public CmdStatus getStatus() {
        return status;
    }

    public abstract <T> T execute(K args);
}
