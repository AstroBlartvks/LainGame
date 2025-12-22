package com.astro.minor.filesystem.items;

import com.astro.minor.core.ServiceLocator;
import com.astro.minor.system.apps.base.AppExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public enum FileType {
    TEXT("Текстовый файл") {
        @Override
        public String execute(MyFile file) {
            try {
                return (Files.readString(Path.of(file.getPath())));
            }  catch (IOException e) {
                return ("Невозможно открыть текстовый файл: " + file.getName() + " ! " + e.getMessage());
            }
        }
    },
    CODE("Исходный код") {
        @Override
        public String execute(MyFile file) {
           return "Открываю файл с кодом: " + file.getName();
        }
    },
    JSON("Объектная Нотация") {
        @Override
        public String execute(MyFile file) {
            try {
                return (Files.readString(Path.of(file.getPath())));
            }  catch (IOException e) {
                return ("Невозможно открыть файл объектной нотации JavaScript: " + file.getName() + " ! " + e.getMessage());
            }
        }
    },
    IMAGE("Изображение") {
        @Override
        public String execute(MyFile file) {
            return ("Показываю изображение: " + file.getName());
        }
    },
    AUDIO("Аудио файл") {
        @Override
        public String execute(MyFile file) {
            try {
                com.astro.minor.system.apps.media.MusicPlayer player = new com.astro.minor.system.apps.media.MusicPlayer(
                    300, 300, 480, 120,
                    "NAVI_AUDIO: " + file.getName(),
                    ServiceLocator.getUiCamera(),
                    file.getPath()
                );
                ServiceLocator.getAppExecutor().addApplication(player);
                return ("Открываю аудиоплеер: " + file.getName());
            } catch (Exception e) {
                return ("Ошибка открытия аудиоплеера: " + e.getMessage());
            }
        }
    },
    VIDEO("Видео файл") {
        @Override
        public String execute(MyFile file) {
            try {
                String extension = file.getExtension().toLowerCase();

                if (!extension.equals("mp4")) {
                    return ("Формат " + extension.toUpperCase() + " не поддерживается. Используйте mp4.");
                }

                com.astro.minor.system.apps.media.MyVideoPlayer player = new com.astro.minor.system.apps.media.MyVideoPlayer(
                    300, 300, 800, 600,
                    "NAVI_VIDEO: " + file.getName(),
                    ServiceLocator.getUiCamera(),
                    file.getPath()
                );
                ServiceLocator.getAppExecutor().addApplication(player);
                return ("Открываю видеоплеер: " + file.getName());
            } catch (Exception e) {
                return ("Ошибка открытия видеоплеера: " + e.getMessage());
            }
        }
    },
    WPML("Wired Page") {
        @Override
        public String execute(MyFile file) {
            try {
                com.astro.minor.system.apps.browser.Browser browser = new com.astro.minor.system.apps.browser.Browser(
                    100, 100, 800, 600,
                    "NAVI_BROWSER: " + file.getName(),
                    ServiceLocator.getUiCamera(),
                    file.getPath()
                );
                ServiceLocator.getAppExecutor().addApplication(browser);
                return ("Открываю в браузере: " + file.getName());
            } catch (Exception e) {
                return ("Ошибка открытия браузера: " + e.getMessage());
            }
        }
    },
    DOCUMENT("Документ") {
        @Override
        public String execute(MyFile file) {
            return ("Открываю документ: " + file.getName());
        }
    },
    ARCHIVE("Архив") {
        @Override
        public String execute(MyFile file) {
            return ("Распаковываю архив: " + file.getName());
        }
    },
    EXECUTABLE("Исполняемый файл") {
        @Override
        public String execute(MyFile file) {
            return ("Запускаю программу: " + file.getName());
        }
    },
    UNKNOWN("Неизвестный тип") {
        @Override
        public String execute(MyFile file) {
            return ("Не могу определить как открыть: " + file.getName());
        }
    };

    private final String description;

    FileType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public abstract String execute(MyFile file);
}
