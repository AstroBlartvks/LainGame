package com.astro.minor.signal;

public class SignalData {
    private String id;
    private SignalType type;
    private String message;
    private String timestamp;
    private String source;
    private boolean read;
    
    public SignalData(String id, SignalType type, String message) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.timestamp = getCurrentTimestamp();
        this.source = "Неизвестный пользователь";
        this.read = false;
    }
    
    public SignalData(SignalType type, String message) {
        this(generateId(), type, message);
    }
    
    private static String generateId() {
        return "0x" + Integer.toHexString((int)(Math.random() * 0xFFFF)).toUpperCase();
    }
    
    private String getCurrentTimestamp() {
        return "1998.04.15 " + String.format("%02d:%02d:%02d",
            (int)(Math.random() * 24),
            (int)(Math.random() * 60),
            (int)(Math.random() * 60)
        );
    }
    
    public String getId() { return id; }
    public SignalType getType() { return type; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getSource() { return source; }
    public boolean isRead() { return read; }
    
    public void setRead(boolean read) { this.read = read; }
    public void setSource(String source) { this.source = source; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getTypeRussian() {
        switch (type) {
            case DISTRESS: return "БЕДСТВИЕ";
            case MEMORY: return "ПАМЯТЬ";
            case SYSTEM: return "СИСТЕМА";
            case CORRUPTED: return "ПОВРЕЖДЕНО";
            case WATCHER: return "НАБЛЮДАТЕЛЬ";
            case SELF: return "САМОСТЬ";
            default: return "НЕИЗВЕСТНО";
        }
    }
    
    public String formatForFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ФРАГМЕНТ СИГНАЛА ").append(id).append("]\n");
        sb.append("Тип: ").append(getTypeRussian()).append("\n");
        sb.append("Время: ").append(timestamp).append("\n");
        sb.append("Источник: ").append(source).append("\n");
        sb.append("Когнитивная подпись: [ДАННЫЕ ПОВРЕЖДЕНЫ]\n");
        sb.append("\n");
        sb.append("Сообщение:\n");
        sb.append("\"").append(message).append("\"\n");
        sb.append("\n");
        sb.append("[КОНЕЦ ФРАГМЕНТА]\n");
        return sb.toString();
    }
}
