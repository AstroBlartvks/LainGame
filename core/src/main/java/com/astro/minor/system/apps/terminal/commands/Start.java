package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.core.ServiceLocator;
import com.astro.minor.system.apps.base.AppExecutor;
import com.astro.minor.system.apps.terminal.Console;
import com.astro.minor.system.apps.explorer.FileExplorer;
import com.astro.minor.system.apps.media.MusicPlayer;
import com.astro.minor.system.apps.media.MyVideoPlayer;
import com.astro.minor.system.apps.browser.Browser;

public class Start extends Command<String []>{
    @Override
    public String execute(String[] args) {
        AppExecutor appExecutor = ServiceLocator.getAppExecutor();

        if (args.length > 0) {
            switch (args[0]) {
                case "console":
                    appExecutor.addApplication(new Console(300, 400, 400, 400, "console_" + appExecutor.getNewPID(), ServiceLocator.getUiCamera()));
                    return "Консоль запущена!";
                case "explorer":
                    appExecutor.addApplication(new FileExplorer(300, 400, 400, 400, "explorer_" + appExecutor.getNewPID(), ServiceLocator.getUiCamera()));
                    return "Проводник запущен!";
                case "music":
                    appExecutor.addApplication(new MusicPlayer(300, 400, 480, 120, "music_" + appExecutor.getNewPID(), ServiceLocator.getUiCamera(), ""));
                    return "Аудиоплеер запущен!";
                case "video":
                case "VideoPlayer":
                    appExecutor.addApplication(new MyVideoPlayer(300, 400, 800, 600, "video_" + appExecutor.getNewPID(), ServiceLocator.getUiCamera(), ""));
                    return "Видеоплеер запущен!";
                case "browser":
                    appExecutor.addApplication(new Browser(100, 100, 800, 600, "browser_" + appExecutor.getNewPID(), ServiceLocator.getUiCamera(), ""));
                    return "Браузер запущен!";
            }

            return "Не найдено приложения с именем: " + args[0] + "!";
        }
        return "Использование: start [console|explorer|music|video|browser]";
    }
}
