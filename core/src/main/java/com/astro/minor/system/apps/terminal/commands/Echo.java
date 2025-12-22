package com.astro.minor.system.apps.terminal.commands;

public class Echo extends Command<String []>{
    @Override
    public String execute(String[] args) {
        status = CmdStatus.OK;
        return String.join(" ", args);
    }
}

