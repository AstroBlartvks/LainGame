package com.astro.minor.fileOS;

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
            return ("Воспроизвожу аудио: " + file.getName());
        }
    },
    VIDEO("Видео файл") {
        @Override
        public String execute(MyFile file) {
            return ("Воспроизвожу видео: " + file.getName());
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
