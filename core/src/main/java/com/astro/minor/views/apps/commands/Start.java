package com.astro.minor.views.apps.commands;

import com.astro.minor.views.apps.AppExecutor;
import com.astro.minor.views.apps.Console;

import static com.astro.minor.StarField.getAppExecutor;
import static com.astro.minor.StarField.getUiCamera;

public class Start extends Command<String []>{
    @Override
    public String execute(String[] args) {
        AppExecutor appExecutor = getAppExecutor();

        if (args.length > 0) {
            switch (args[0]) {
                case "console":
                    appExecutor.addApplication(new Console(300, 400, 400, 400, "console_" + appExecutor.getNewPID(), getUiCamera()));
                    return "Консоль запущена!!";
            }

            return "Не найдено приложения с именем: " + args[0] + "!";
        }
        return "Не указано приложение!";
    }
}
