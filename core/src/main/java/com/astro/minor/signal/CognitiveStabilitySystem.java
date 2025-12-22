package com.astro.minor.signal;

import com.astro.minor.system.graphics.CRTShaderSystemVHS;
import com.astro.minor.system.apps.terminal.commands.Psyche;

public class CognitiveStabilitySystem {
    private static CognitiveStabilitySystem instance;

    private float stability;
    private float playTimeMinutes;
    private boolean vhsLowActivated;
    private boolean vhsHighActivated;
    private Psyche psycheChipDetectedCommand = new Psyche();

    private CognitiveStabilitySystem() {
        this.stability = 1.0f;
        this.playTimeMinutes = 0;
        this.vhsLowActivated = false;
        this.vhsHighActivated = false;
    }

    public static CognitiveStabilitySystem getInstance() {
        if (instance == null) {
            instance = new CognitiveStabilitySystem();
        }
        return instance;
    }

    public void update(float delta) {

        if (!psycheChipDetectedCommand.isPsycheChipDetected()) {
            return;
        }

        playTimeMinutes += delta / 60.0f;

        float targetStability = 1.0f - (playTimeMinutes / 180.0f);
        targetStability = Math.max(0.0f, Math.min(1.0f, targetStability));

        stability += (targetStability - stability) * delta * 0.2f;

        if (stability <= 0.4f && !vhsLowActivated) {
            vhsLowActivated = true;
            activateVHSLow();
        } else if (stability <= 0.2f && !vhsHighActivated) {
            vhsHighActivated = true;
            activateVHSHigh();
        }
    }

    private void activateVHSLow() {
        CRTShaderSystemVHS vhs = CRTShaderSystemVHS.getInstance();
        if (vhs != null) {
            vhs.setVhsLowEnabled(true);
        }
    }

    private void activateVHSHigh() {
        CRTShaderSystemVHS vhs = CRTShaderSystemVHS.getInstance();
        if (vhs != null) {
            vhs.setVhsHighEnabled(true);
        }
    }

    public float getStability() {
        return stability;
    }

    public float getPlayTimeMinutes() {
        return playTimeMinutes;
    }

    public float getProgress() {
        return 1.0f - stability;
    }

    public String getStage() {
        float progress = getProgress();

        if (progress < 0.3f) return "ИССЛЕДОВАНИЕ";
        if (progress < 0.6f) return "ПОДОЗРЕНИЕ";
        if (progress < 0.9f) return "ОСОЗНАНИЕ";
        return "СЛИЯНИЕ";
    }

    public void decreaseStability(float amount) {
        stability = Math.max(0.0f, stability - amount);
    }
}
