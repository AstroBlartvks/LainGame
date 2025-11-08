package com.astro.minor.views.apps.commands;

import java.util.Date;

public class Time extends Command{
    @Override
    public String execute(String[] args) {
        this.status = CmdStatus.OK;
        return "Сейчас: " + new Date();
    }
}
