package com.astro.minor.signal;

import com.astro.minor.system.apps.terminal.Console;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.lang.ProcessHandle;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessWatcher {
    private static ProcessWatcher instance;

    private List<ProcessTrigger> triggers;
    private Map<String, Long> lastTriggerTime;
    private Set<String> previousProcesses;
    private float timeSinceLastScan;
    private float scanInterval;
    private boolean enabled;
    private Console outputConsole;

    private ProcessWatcher() {
        triggers = new ArrayList<>();
        lastTriggerTime = new HashMap<>();
        previousProcesses = new HashSet<>();
        timeSinceLastScan = 0;
        scanInterval = 30.0f;
        enabled = true;
        loadConfig();
    }

    public static ProcessWatcher getInstance() {
        if (instance == null) {
            instance = new ProcessWatcher();
        }
        return instance;
    }

    public void setOutputConsole(Console console) {
        this.outputConsole = console;
    }

    private void loadConfig() {
        try {
            JsonValue json = new JsonReader().parse(Gdx.files.internal("data/processes.json"));
            JsonValue settings = json.get("settings");

            if (settings != null) {
                enabled = settings.getBoolean("enabled", true);
                scanInterval = settings.getFloat("scan_interval_seconds", 30.0f);
            }

            JsonValue triggersArray = json.get("triggers");
            if (triggersArray != null) {
                for (JsonValue triggerJson : triggersArray) {
                    ProcessTrigger trigger = new ProcessTrigger();
                    trigger.id = triggerJson.getString("id");
                    trigger.enabled = triggerJson.getBoolean("enabled", true);
                    trigger.ghostMode = triggerJson.getBoolean("ghost_mode", false);
                    trigger.cooldownSeconds = triggerJson.getInt("cooldown_seconds", 300);
                    trigger.priority = triggerJson.getInt("priority", 1);
                    trigger.signalType = triggerJson.getString("signal_type", "WATCHER");

                    JsonValue patterns = triggerJson.get("process_patterns");
                    if (patterns != null) {
                        for (JsonValue pattern : patterns) {
                            trigger.processPatterns.add(pattern.asString().toLowerCase());
                        }
                    }

                    JsonValue messages = triggerJson.get("messages");
                    if (messages != null) {
                        for (JsonValue msg : messages) {
                            trigger.messages.add(msg.asString());
                        }
                    }

                    triggers.add(trigger);
                }
            }

            System.out.println("[ProcessWatcher] Загружено " + triggers.size() + " триггеров");
        } catch (Exception e) {
            System.err.println("[ProcessWatcher] Ошибка загрузки: " + e.getMessage());
        }
    }

    public void update(float delta) {
        if (!enabled) return;

        timeSinceLastScan += delta;

        if (timeSinceLastScan >= scanInterval) {
            timeSinceLastScan = 0;
            scanProcesses();
        }
    }

    private void scanProcesses() {
        Set<String> currentProcesses = getCurrentProcesses();

        for (String processName : currentProcesses) {
            checkTriggers(processName);
        }

        if (!previousProcesses.isEmpty()) {
            Set<String> closedProcesses = new HashSet<>(previousProcesses);
            closedProcesses.removeAll(currentProcesses);

            for (String closed : closedProcesses) {
                checkGhostTriggers(closed);
            }
        }

        previousProcesses = currentProcesses;
    }

    private Set<String> getCurrentProcesses() {
        Set<String> processes = new HashSet<>();

        try {
            ProcessHandle.allProcesses()
                .filter(ph -> ph.info().command().isPresent())
                .forEach(ph -> {
                    String cmd = ph.info().command().get().toLowerCase();
                    String processName = extractProcessName(cmd);
                    if (processName != null && !processName.isEmpty()) {
                        processes.add(processName);
                    }
                });
        } catch (Exception e) {
            System.err.println("[ProcessWatcher] Ошибка сканирования: " + e.getMessage());
        }

        return processes;
    }

    private String extractProcessName(String command) {
        String[] parts = command.replace("\\", "/").split("/");
        String exe = parts[parts.length - 1];
        return exe.replace(".exe", "");
    }

    private void checkTriggers(String processName) {
        for (ProcessTrigger trigger : triggers) {
            if (!trigger.enabled || trigger.ghostMode) continue;

            if (matchesPattern(processName, trigger.processPatterns)) {
                if (isOnCooldown(trigger)) continue;

                generateSignal(trigger, processName);
                lastTriggerTime.put(trigger.id, System.currentTimeMillis());
            }
        }
    }

    private void checkGhostTriggers(String processName) {
        for (ProcessTrigger trigger : triggers) {
            if (!trigger.enabled || !trigger.ghostMode) continue;

            if (matchesPattern(processName, trigger.processPatterns)) {
                if (isOnCooldown(trigger)) continue;

                generateSignal(trigger, processName + " [ЗАВЕРШЁН]");
                lastTriggerTime.put(trigger.id, System.currentTimeMillis());
            }
        }
    }

    private boolean matchesPattern(String processName, List<String> patterns) {
        for (String pattern : patterns) {
            if (processName.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnCooldown(ProcessTrigger trigger) {
        if (!lastTriggerTime.containsKey(trigger.id)) return false;

        long lastTime = lastTriggerTime.get(trigger.id);
        long now = System.currentTimeMillis();
        long cooldown = trigger.cooldownSeconds * 1000L;

        return (now - lastTime) < cooldown;
    }

    private void generateSignal(ProcessTrigger trigger, String processName) {
        if (trigger.messages.isEmpty()) return;

        String message = trigger.messages.get(
            ThreadLocalRandom.current().nextInt(trigger.messages.size())
        );

        message = replaceVariables(message, processName);

        if (outputConsole != null) {
            String output = "\n[PROCESS ALERT - " + trigger.signalType + "]\n" +
                           "Процесс: " + processName + "\n" +
                           "\"" + message + "\"\n";

            Gdx.app.postRunnable(() -> {
                outputConsole.output(output);
            });
        }

        System.out.println("[ProcessWatcher] Триггер: " + trigger.id + " -> " + processName);
    }

    private String replaceVariables(String message, String processName) {
        message = message.replace("{{process_name}}", processName);

        CognitiveStabilitySystem cogSys = CognitiveStabilitySystem.getInstance();
        int cognitive = (int)(cogSys.getStability() * 100);
        message = message.replace("{{cognitive}}", String.valueOf(cognitive));

        Pattern randomPattern = Pattern.compile("\\{\\{random\\((\\d+),(\\d+)\\)\\}\\}");
        Matcher matcher = randomPattern.matcher(message);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            int min = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));
            int random = ThreadLocalRandom.current().nextInt(min, max + 1);
            matcher.appendReplacement(sb, String.valueOf(random));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private static class ProcessTrigger {
        String id;
        boolean enabled;
        boolean ghostMode;
        int cooldownSeconds;
        int priority;
        String signalType;
        List<String> processPatterns = new ArrayList<>();
        List<String> messages = new ArrayList<>();
    }
}
