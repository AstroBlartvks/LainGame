package com.astro.minor.views.apps.commands;

import static com.astro.minor.StarField.osName;
import static com.astro.minor.StarField.version;

public class Version extends Command{
    @Override
    public String execute(String[] args) {
        return "(c) Navi. Tachibana General Laboratories.\n" + osName + " [" + version + "]";
    }
}
