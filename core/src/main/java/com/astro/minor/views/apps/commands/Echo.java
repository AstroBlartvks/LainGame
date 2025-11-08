package com.astro.minor.views.apps.commands;

public class Echo extends Command{
    @Override
    public String execute(String[] args) {
        status = CmdStatus.OK;
        return String.join(" ", args);
    }
}
