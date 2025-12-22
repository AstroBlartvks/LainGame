package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.core.config.AppConfig;

public class Version extends Command<String []>{
    @Override
    public String execute(String[] args) {
        status = CmdStatus.OK;
        return "(c) Navi. Tachibana General Laboratories.\n" + AppConfig.OS_NAME + " [" + AppConfig.VERSION + "]";
    }
}

