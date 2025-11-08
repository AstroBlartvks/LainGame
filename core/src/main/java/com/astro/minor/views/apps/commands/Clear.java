package com.astro.minor.views.apps.commands;

public class Clear extends Command<String []>{
    @Override
    public String execute(String[] args) {
        return "#clear";
    }

}
