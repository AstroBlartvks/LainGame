package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.audio.WatcherSystem;
import java.util.List;

public class Watcher extends Command<String[]> {

    @Override
    public <T> T execute(String[] args) {
        this.status = CmdStatus.OK;
        String result;

        if (args.length == 0) {
            result = getStatusInfo();
        } else {
            String subcommand = args[0].toLowerCase();

            switch (subcommand) {
                case "--status":
                case "-s":
                    result = getStatusInfo();
                    break;

                case "--force":
                case "-f":
                    WatcherSystem.getInstance().forceEvent();
                    result = "Событие Watcher принудительно активировано...";
                    break;

                case "--test":
                case "-t":
                    result = testAllEvents();
                    break;

                case "--logs":
                case "-l":
                    result = getLogs();
                    break;

                case "--help":
                case "-h":
                    result = getHelp();
                    break;

                default:
                    result = "Неизвестная команда. Используйте 'watcher --help'";
                    break;
            }
        }

        return (T) result;
    }

    public String getStatusInfo() {
        WatcherSystem ws = WatcherSystem.getInstance();
        StringBuilder sb = new StringBuilder();

        sb.append("╔═══════════════════════════════════════════╗\n");
        sb.append("║        WATCHER SYSTEM STATUS              ║\n");
        sb.append("╠═══════════════════════════════════════════╣\n");
        sb.append("║                                           ║\n");

        String breathing = ws.isBreathing() ? "[АКТИВНО]" : "[ТИХО]";
        String heartbeat = ws.isHeartbeatActive() ? "[АКТИВНО]" : "[ТИХО]";

        sb.append("║  Дыхание:         ").append(String.format("%-20s", breathing)).append("║\n");
        sb.append("║  Сердцебиение:    ").append(String.format("%-20s", heartbeat)).append("║\n");
        sb.append("║                                           ║\n");
        sb.append("║  Обнаружено событий: ").append(String.format("%-17s", ws.getConnectionLogs().size())).append("║\n");
        sb.append("║                                           ║\n");
        sb.append("╚═══════════════════════════════════════════╝\n");
        sb.append("\n");
        sb.append("Используйте 'watcher --logs' для просмотра логов\n");
        sb.append("Используйте 'watcher --force' для принудительной активации");

        return sb.toString();
    }

    private String getLogs() {
        WatcherSystem ws = WatcherSystem.getInstance();
        List<String> logs = ws.getConnectionLogs();

        if (logs.isEmpty()) {
            return "Логов подключений пока нет.\nWatcher ещё не проявлял активности...";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════════\n");
        sb.append("          ЖУРНАЛ СОБЫТИЙ WATCHER\n");
        sb.append("═══════════════════════════════════════════════════\n\n");

        int displayCount = Math.min(10, logs.size());
        int startIndex = Math.max(0, logs.size() - displayCount);

        for (int i = startIndex; i < logs.size(); i++) {
            sb.append(logs.get(i)).append("\n");
        }

        if (logs.size() > 10) {
            sb.append("\n(Показано последние 10 из ").append(logs.size()).append(" событий)");
        }

        return sb.toString();
    }

    private String testAllEvents() {
        StringBuilder sb = new StringBuilder();
        sb.append("Тестирование всех событий Watcher...\n\n");

        sb.append("1. Проверка звуковых файлов...\n");

        boolean breathExists = com.badlogic.gdx.Gdx.files.internal("sounds/watcher/breath.wav").exists();
        if (breathExists) {
            sb.append("   ✓ breath.wav найден\n");
        } else {
            sb.append("   ✗ breath.wav НЕ найден (должен быть в assets/sounds/watcher/)\n");
        }

        boolean heartbeatExists = com.badlogic.gdx.Gdx.files.internal("sounds/watcher/heartbeat.wav").exists();
        if (heartbeatExists) {
            sb.append("   ✓ heartbeat.wav найден\n");
        } else {
            sb.append("   ✗ heartbeat.wav НЕ найден (должен быть в assets/sounds/watcher/)\n");
        }

        sb.append("\n2. Генерация лога подключения...\n");
        WatcherSystem.getInstance().forceEvent();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String lastLog = WatcherSystem.getInstance().getLastConnectionLog();
        if (lastLog != null) {
            sb.append("   ✓ Лог создан успешно\n");
            sb.append("   Последний лог: ").append(lastLog).append("\n");
        } else {
            sb.append("   ✗ Лог не создан (проверьте, запущена ли WatcherSystem)\n");
        }

        sb.append("\n3. Статус звуков (в данный момент)...\n");
        if (WatcherSystem.getInstance().isBreathing()) {
            sb.append("   ✓ Дыхание активно прямо сейчас\n");
        } else {
            sb.append("   - Дыхание не активно (появится случайно или через --force)\n");
        }

        if (WatcherSystem.getInstance().isHeartbeatActive()) {
            sb.append("   ✓ Сердцебиение активно прямо сейчас\n");
        } else {
            sb.append("   - Сердцебиение не активно (появится случайно или через --force)\n");
        }

        sb.append("\n4. Статистика системы...\n");
        sb.append("   Всего событий: ").append(WatcherSystem.getInstance().getConnectionLogs().size()).append("\n");

        sb.append("\n═══════════════════════════════════════════════════\n");
        sb.append("Используйте 'watcher --force' несколько раз для генерации событий.\n");
        sb.append("Используйте 'watcher --logs' для просмотра всех логов.");

        return sb.toString();
    }

    private String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("WATCHER - Система мониторинга неизвестных подключений\n\n");
        sb.append("Использование: watcher [опции]\n\n");
        sb.append("Опции:\n");
        sb.append("  --status, -s    Показать текущий статус системы\n");
        sb.append("  --logs, -l      Показать журнал событий\n");
        sb.append("  --force, -f     Принудительно активировать событие\n");
        sb.append("  --test, -t      Протестировать все компоненты системы\n");
        sb.append("  --help, -h      Показать эту справку\n\n");
        sb.append("Примеры:\n");
        sb.append("  watcher              # Показать статус\n");
        sb.append("  watcher --logs       # Просмотреть логи\n");
        sb.append("  watcher --force      # Активировать Watcher\n");
        sb.append("  watcher --test       # Протестировать систему\n\n");
        sb.append("ПРЕДУПРЕЖДЕНИЕ: Watcher всегда наблюдает.\n");
        sb.append("Даже когда вы не видите его присутствия.");

        return sb.toString();
    }
}

