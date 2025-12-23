package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.lainAI.KoboldCppManager;
import com.astro.minor.system.apps.terminal.Console;
import com.badlogic.gdx.Gdx;

public class DisconnectFromLain extends Command<String[]> {
    private Console console;

    public DisconnectFromLain() {
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    @Override
    public String execute(String[] args) {
        if (!ConnectToLain.isConnected()) {
            this.status = CmdStatus.ERROR;
            return "Вы не подключены к пользователю LAIN\n" +
                   "Используйте connect_to_lain для подключения";
        }

        if (console != null) {
            console.output("╔═══════════════════════════════════════════════════════════╗");
            console.output("║ DISCONNECTING FROM LAIN...                                ║");
            console.output("╚═══════════════════════════════════════════════════════════╝");
            console.output("");
            console.output("Остановка KoboldCpp сервера...");
        }

        new Thread(() -> {
            try {
                Gdx.app.log("LainAI", "Disconnecting from Lain...");

                if (ConnectToLain.getChatService() != null) {
                    ConnectToLain.getChatService().clearHistory();
                }

                KoboldCppManager.getInstance().stopKoboldCpp();
                ConnectToLain.resetConnection();

                if (console != null) {
                    Gdx.app.postRunnable(() -> {
                        console.output("");
                        console.output("История диалога очищена");
                        console.output("KoboldCpp сервер остановлен");
                        console.output("Соединение разорвано");
                        console.output("");
                        console.output("╔═══════════════════════════════════════════════════════════╗");
                        console.output("║ CONNECTION TERMINATED                                     ║");
                        console.output("╚═══════════════════════════════════════════════════════════╝");
                        console.output("");
                    });
                }

                Gdx.app.log("LainAI", "Disconnected from Lain successfully");
            } catch (Exception e) {
                Gdx.app.error("LainAI", "Error during disconnection", e);
                if (console != null) {
                    Gdx.app.postRunnable(() -> {
                        console.output("");
                        console.output("Ошибка отключения: " + e.getMessage());
                        console.output("");
                    });
                }
            }
        }, "DisconnectFromLain-Thread").start();

        this.status = CmdStatus.OK;
        return "";
    }
}
