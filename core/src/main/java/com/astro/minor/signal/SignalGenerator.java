package com.astro.minor.signal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SignalGenerator {
    private static SignalGenerator instance;
    private Map<SignalType, List<String>> signalDatabase;
    
    private SignalGenerator() {
        signalDatabase = new HashMap<>();
        loadSignals();
    }
    
    public static SignalGenerator getInstance() {
        if (instance == null) {
            instance = new SignalGenerator();
        }
        return instance;
    }
    
    private void loadSignals() {
        try {
            JsonValue json = new JsonReader().parse(Gdx.files.internal("data/signals.json"));
            JsonValue signals = json.get("signals");
            
            for (SignalType type : SignalType.values()) {
                JsonValue array = signals.get(type.name());
                List<String> messages = new ArrayList<>();
                
                if (array != null) {
                    for (JsonValue msg : array) {
                        messages.add(msg.asString());
                    }
                }
                
                signalDatabase.put(type, messages);
            }
        } catch (Exception e) {
            System.err.println("[SignalGenerator] Ошибка загрузки signals.json: " + e.getMessage());
        }
    }
    
    public SignalData generateRandom(float progress, float cognitiveStability) {
        SignalType type = getRandomSignalType(progress, cognitiveStability);
        String message = getRandomMessage(type);
        return new SignalData(type, message);
    }
    
    public SignalData generateByType(SignalType type) {
        String message = getRandomMessage(type);
        return new SignalData(type, message);
    }
    
    private SignalType getRandomSignalType(float progress, float cognitiveStability) {
        float[] weights;
        
        if (progress < 0.3f) {
            weights = new float[]{0.40f, 0.30f, 0.20f, 0.05f, 0.05f, 0.00f};
        } else if (progress < 0.6f) {
            weights = new float[]{0.25f, 0.25f, 0.15f, 0.15f, 0.15f, 0.05f};
        } else if (progress < 0.9f) {
            weights = new float[]{0.10f, 0.15f, 0.10f, 0.25f, 0.25f, 0.15f};
        } else {
            weights = new float[]{0.05f, 0.05f, 0.05f, 0.30f, 0.30f, 0.25f};
        }
        
        if (cognitiveStability < 0.3f) {
            weights[3] *= 2;
            weights[5] *= 2;
            normalizeWeights(weights);
        }
        
        return weightedRandom(weights, SignalType.values());
    }
    
    private void normalizeWeights(float[] weights) {
        float sum = 0;
        for (float w : weights) sum += w;
        for (int i = 0; i < weights.length; i++) {
            weights[i] /= sum;
        }
    }
    
    private SignalType weightedRandom(float[] weights, SignalType[] types) {
        float random = ThreadLocalRandom.current().nextFloat();
        float cumulative = 0;
        
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (random <= cumulative) {
                return types[i];
            }
        }
        
        return types[types.length - 1];
    }
    
    private String getRandomMessage(SignalType type) {
        List<String> messages = signalDatabase.get(type);
        if (messages == null || messages.isEmpty()) {
            return "СИГНАЛ ПОТЕРЯН";
        }
        
        int index = ThreadLocalRandom.current().nextInt(messages.size());
        return messages.get(index);
    }
}
