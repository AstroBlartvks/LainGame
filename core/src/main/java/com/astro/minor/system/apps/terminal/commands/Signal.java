package com.astro.minor.system.apps.terminal.commands;

import com.astro.minor.signal.CognitiveStabilitySystem;
import com.astro.minor.signal.SignalData;
import com.astro.minor.signal.SignalGenerator;
import com.astro.minor.signal.SignalSaver;

public class Signal extends Command<String[]> {
    private SignalData lastSignal;
    private Psyche psycheCheck = new Psyche();
    
    @Override
    public String execute(String[] args) {
        if (!psycheCheck.isPsycheChipDetected()) {
            this.status = CmdStatus.ERROR;
            return "ERROR: PSYCHE CHIP не обнаружен.\n" +
                   "Для работы Signal Scanner необходим PSYCHE CHIP.\n" +
                   "Проверьте установку: psyche status";
        }
        
        if (args.length == 0) {
            this.status = CmdStatus.ERROR;
            return "Использование: signal [scan|status|save]\n" +
                   "Введите 'help' для справки.";
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "scan":
                return scan();
            case "status":
                return status();
            case "save":
                return save();
            default:
                this.status = CmdStatus.ERROR;
                return "Неизвестная подкоманда: " + subcommand + "\n" +
                       "Доступно: scan, status, save";
        }
    }
    
    private String scan() {
        this.status = CmdStatus.OK;
        
        CognitiveStabilitySystem cogSys = CognitiveStabilitySystem.getInstance();
        SignalGenerator gen = SignalGenerator.getInstance();
        
        float progress = cogSys.getProgress();
        float stability = cogSys.getStability();
        
        int count = (int)(Math.random() * 3) + 1;
        StringBuilder result = new StringBuilder();
        result.append("[Сканирование частот...]\n");
        
        for (int i = 0; i < count; i++) {
            SignalData signal = gen.generateRandom(progress, stability);
            lastSignal = signal;
            
            result.append("[").append(signal.getTimestamp().substring(11)).append("] ");
            result.append(signal.getTypeRussian()).append(" >>> \"");
            
            String msg = signal.getMessage();
            if (msg.length() > 60) {
                msg = msg.substring(0, 60) + "...";
            }
            
            result.append(msg).append("\"\n");
        }
        
        result.append("\nНайдено ").append(count).append(" сигнал");
        if (count > 1) result.append("а");
        result.append(". Используйте 'signal save' для сохранения.");
        
        return result.toString();
    }
    
    private String status() {
        this.status = CmdStatus.OK;
        CognitiveStabilitySystem cogSys = CognitiveStabilitySystem.getInstance();
        
        StringBuilder result = new StringBuilder();
        result.append("╔═══════════════════════════════════════════════╗\n");
        result.append("║        SIGNAL SCANNER - СТАТУС               ║\n");
        result.append("╠═══════════════════════════════════════════════╣\n");
        result.append("║                                               ║\n");
        result.append("║  Когнитивная стабильность: ")
              .append(String.format("%3.0f%%", cogSys.getStability() * 100))
              .append("             ║\n");
        result.append("║  Стадия: ").append(String.format("%-35s", cogSys.getStage())).append("║\n");
        result.append("║  Время в системе: ")
              .append(String.format("%3.0f мин", cogSys.getPlayTimeMinutes()))
              .append("                  ║\n");
        result.append("║                                               ║\n");
        result.append("╚═══════════════════════════════════════════════╝\n");
        
        return result.toString();
    }
    
    private String save() {
        if (lastSignal == null) {
            this.status = CmdStatus.ERROR;
            return "Нет сигнала для сохранения. Используйте 'signal scan' сначала.";
        }
        
        String path = SignalSaver.save(lastSignal);
        
        if (path != null) {
            this.status = CmdStatus.OK;
            return "Сигнал сохранён: " + path;
        } else {
            this.status = CmdStatus.ERROR;
            return "Ошибка сохранения сигнала.";
        }
    }
}

