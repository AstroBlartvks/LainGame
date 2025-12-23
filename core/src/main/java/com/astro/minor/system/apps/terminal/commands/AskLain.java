package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.lainAI.LainChatService;
import com.astro.minor.system.apps.terminal.Console;
import com.badlogic.gdx.Gdx;

public class AskLain extends Command<String[]> {
    private Console console;

    public AskLain() {
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    @Override
    public String execute(String[] args) {
        if (!ConnectToLain.isConnected()) {
            this.status = CmdStatus.ERROR;
            return "Соединение с пользователем LAIN не установлено\n" +
                   "\n" +
                   "Для подключения используйте команду:\n" +
                   "  connect_to_lain\n" +
                   "\n" +
                   "После подключения вы сможете общаться с Лэйн";
        }

        if (args.length == 0) {
            this.status = CmdStatus.ERROR;
            return "Usage: ask_lain <ваш вопрос>\n" +
                   "Пример: ask_lain Привет, Лэйн";
        }

        String userMessage = String.join(" ", args);
        LainChatService chatService = ConnectToLain.getChatService();

        if (chatService == null) {
            this.status = CmdStatus.ERROR;
            return "Ошибка: сервис не инициализирован\n" +
                   "Переподключитесь командой connect_to_lain";
        }

        if (console != null) {
            console.output("Лэйн думает...");
        }

        new Thread(() -> {
            try {
                Gdx.app.log("LainAI", "Sending message: " + userMessage);
                String response = chatService.askLain(userMessage).get();
                Gdx.app.log("LainAI", "Received response: " + response);

                if (console != null) {
                    Gdx.app.postRunnable(() -> {
                        console.output("Лейн: " + response);
                        console.output("");
                    });
                }
            } catch (Exception e) {
                Gdx.app.error("LainAI", "Error communicating with Lain", e);
                if (console != null) {
                    Gdx.app.postRunnable(() -> {
                        console.output("Ошибка связи с Лэйн: " + e.getMessage());
                        console.output("Возможные причины:");
                        console.output("  - Связь через прокси KoboldCpp не возможна");
                        console.output("  - Лейн не в сети, превышено время ожидания");
                        console.output("Попробуйте переподключиться:");
                        console.output("  disconnect_from_lain");
                        console.output("  connect_to_lain");
                        console.output("");
                    });
                }
            }
        }, "LainAI-Thread").start();

        this.status = CmdStatus.OK;
        return "";
    }
}
