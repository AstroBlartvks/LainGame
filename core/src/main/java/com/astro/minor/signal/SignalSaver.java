package com.astro.minor.signal;

import com.astro.minor.system.apps.terminal.commands.Psyche;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SignalSaver {
    private static final String FALLBACK_PATH = "./The Wired/Lain/Signals/frequency/";
    private static final String PSYCHE_PATH = "P:\\signals\\frequency\\";
    
    public static String save(SignalData signal) {
        Psyche psycheCmd = new Psyche();
        String basePath = psycheCmd.isPsycheChipDetected() ? PSYCHE_PATH : FALLBACK_PATH;
        
        File directory = new File(basePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String filename = signal.getType().name().toLowerCase() + "_" + signal.getId() + ".txt";
        File file = new File(directory, filename);
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(signal.formatForFile());
            return file.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("[SignalSaver] Ошибка сохранения: " + e.getMessage());
            return null;
        }
    }
}
