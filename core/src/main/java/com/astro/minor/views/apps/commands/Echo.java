package com.astro.minor.views.apps.commands;

public class Echo extends Command{
    @Override
    public String execute(String[] args) {
        return String.join(" ", args);
    }
}
