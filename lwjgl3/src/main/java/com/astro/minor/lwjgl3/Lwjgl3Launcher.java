package com.astro.minor.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter;
import com.astro.minor.StarField;

import javax.swing.*;

public class Lwjgl3Launcher {
    private static StarField game;
    
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        game = new StarField();
        return new Lwjgl3Application(game, getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("LainProject");
        configuration.useVsync(true);
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        configuration.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public boolean closeRequested() {
                int result = JOptionPane.showConfirmDialog(
                    null,
                    "Вы уверены, что хотите выйти?\n\n" +
                    "Все активные процессы будут завершены,\n" +
                    "включая KoboldCpp сервер (если запущен).",
                    "Подтверждение выхода",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                
                if (result == JOptionPane.OK_OPTION) {
                    if (game != null) {
                        game.safeShutdown();
                    }
                    return true;
                }
                return false;
            }
        });

        return configuration;
    }
}
