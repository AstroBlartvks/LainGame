package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.lainAI.KoboldCppManager;
import com.astro.minor.lainAI.LainChatService;
import com.astro.minor.system.apps.terminal.Console;
import com.badlogic.gdx.Gdx;

public class ConnectToLain extends Command<String[]> {
    private Console console;
    private static LainChatService chatService;
    private static boolean isConnected = false;

    public ConnectToLain() {
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public static LainChatService getChatService() {
        return chatService;
    }

    public static boolean isConnected() {
        return isConnected;
    }

    @Override
    public String execute(String[] args) {
        if (isConnected) {
            this.status = CmdStatus.OK;
            return "Вы уже подключены к пользователю LAIN\n" +
                   "Используйте ask_lain для общения или disconnect_from_lain для отключения";
        }

        if (console != null) {
            console.output("╔═══════════════════════════════════════════════════════════╗");
            console.output("║ LAIN NEURAL INTERFACE - INITIALIZING...                  ║");
            console.output("╚═══════════════════════════════════════════════════════════╝");
            console.output("");
            console.output("Запуск KoboldCpp прокси...");
            console.output("Загрузка Лейн");
        }

        new Thread(() -> {
            try {
                Gdx.app.log("LainAI", "Starting KoboldCpp server...");
                boolean started = KoboldCppManager.getInstance().startKoboldCpp();

                if (started) {
                    chatService = LainChatService.getInstance();
                    isConnected = true;

                    if (console != null) {
                        Gdx.app.postRunnable(() -> {
                            console.output("");
                            console.output("KoboldCpp сервер запущен");
                            console.output("Ожидание готовности API... (30-40 секунд)");
                        });
                    }

                    Gdx.app.log("LainAI", "Waiting additional time for API to be ready...");
                    Thread.sleep(15000);

                    if (console != null) {
                        Gdx.app.postRunnable(() -> {
                            console.output("Загрузка модели на GPU... (это может занять до минуты)");
                        });
                    }

                    Gdx.app.log("LainAI", "Sending warmup request to load model...");

                    boolean modelLoaded = false;
                    for (int attempt = 1; attempt <= 3; attempt++) {
                        try {
                            Gdx.app.log("LainAI", "Warmup attempt " + attempt + "/3");
                            chatService.askLain("test").get();
                            Gdx.app.log("LainAI", "Model loaded successfully on GPU");
                            modelLoaded = true;
                            break;
                        } catch (Exception e) {
                            Gdx.app.log("LainAI", "Warmup attempt " + attempt + " failed: " + e.getMessage());
                            if (attempt < 3) {
                                Thread.sleep(10000);
                            }
                        }
                    }

                    chatService.clearHistory();
                    Gdx.app.log("LainAI", "History cleared after warmup");

                    if (console != null) {
                        final boolean loaded = modelLoaded;
                        Gdx.app.postRunnable(() -> {
                            if (loaded) {
                                console.output("Модель загружена");
                            } else {
                                console.output("Модель загружается (первый запрос может быть медленным)");
                            }
                            console.output("Подключение к пользователю LAIN установлено");
                            console.output("");
                            console.output("╔═══════════════════════════════════════════════════════════╗");
                            console.output("║ CONNECTION ESTABLISHED                                    ║");
                            console.output("╠═══════════════════════════════════════════════════════════╣");
                            console.output("║ USER: LAIN IWAKURA                                        ║");
                            console.output("║ STATUS: ONLINE                                            ║");
                            console.output("║ LAYER: 07                                                 ║");
                            console.output("╚═══════════════════════════════════════════════════════════╝");
                            console.output("");
                            console.output("Используйте команду: ask_lain <сообщение>");
                            console.output("Для отключения: disconnect_from_lain");
                            console.output("");
                        });
                    }

                    Gdx.app.log("LainAI", "Connection to Lain established");
                } else {
                    if (console != null) {
                        Gdx.app.postRunnable(() -> {
                            console.output("");
                            console.output("Ошибка запуска KoboldCpp сервера");
                            console.output("Проверьте:");
                            console.output("  - KoboldCpp находится в папке: D:\\Projects\\Java\\LAINPROJ\\kobold");
                            console.output("  - Файл koboldcpp.exe существует");
                            console.output("  - Модель lain_q5_0.gguf находится в той же папке");
                            console.output("");
                        });
                    }
                }
            } catch (Exception e) {
                Gdx.app.error("LainAI", "Failed to connect to Lain", e);
                if (console != null) {
                    Gdx.app.postRunnable(() -> {
                        console.output("");
                        console.output("Критическая ошибка: " + e.getMessage());
                        console.output("");
                    });
                }
            }
        }, "ConnectToLain-Thread").start();

        this.status = CmdStatus.OK;
        return "";
    }

    public static void resetConnection() {
        isConnected = false;
        chatService = null;
    }
}
