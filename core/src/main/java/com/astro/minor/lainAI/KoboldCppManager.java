package com.astro.minor.lainAI;

import com.badlogic.gdx.Gdx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.astro.minor.core.config.AppConfig.*;

public class KoboldCppManager {
    private Process koboldProcess;
    private Thread outputThread;
    private boolean isRunning = false;
    private static KoboldCppManager instance;
    private Thread shutdownHook;

    private KoboldCppManager() {
        shutdownHook = new Thread(() -> {
            if (isRunning && koboldProcess != null && koboldProcess.isAlive()) {
                System.out.println("Shutdown hook: Terminating KoboldCpp...");
                koboldProcess.destroyForcibly();
                try {
                    koboldProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Shutdown hook: KoboldCpp terminated");
            }
        }, "KoboldCpp-ShutdownHook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public static synchronized KoboldCppManager getInstance() {
        if (instance == null) {
            instance = new KoboldCppManager();
        }
        return instance;
    }

    public boolean startKoboldCpp() {
        if (isRunning && koboldProcess != null && koboldProcess.isAlive()) {
            Gdx.app.log("KoboldCpp", "Already running");
            return true;
        }
        
        isRunning = false;
        koboldProcess = null;

        try {
            File koboldDir = new File(KOBOLD_CPP);
            File koboldExe = new File(koboldDir, KOBOLD_EXE);
            File modelFile = new File(koboldDir, LAIN_MODEL_NAME);

            if (!koboldExe.exists()) {
                Gdx.app.error("KoboldCpp", "Executable not found: " + koboldExe.getAbsolutePath());
                return false;
            }

            if (!modelFile.exists()) {
                Gdx.app.error("KoboldCpp", "Model not found: " + modelFile.getAbsolutePath());
                return false;
            }

            List<String> command = new ArrayList<>();
            command.add(koboldExe.getAbsolutePath());
            command.add("--model");
            command.add(modelFile.getAbsolutePath());
            command.add("--port");
            command.add("5001");
            command.add("--contextsize");
            command.add("2048");
            command.add("--gpulayers");
            command.add("-1");
            command.add("--usecublas");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(koboldDir);
            processBuilder.redirectErrorStream(true);

            Gdx.app.log("KoboldCpp", "Starting KoboldCpp...");
            Gdx.app.log("KoboldCpp", "Command: " + String.join(" ", command));

            koboldProcess = processBuilder.start();
            isRunning = true;

            outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(koboldProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final String logLine = line;
                        Gdx.app.log("KoboldCpp", logLine);
                        
                        if (logLine.contains("Ready!") || logLine.contains("Server started")) {
                            Gdx.app.log("KoboldCpp", "Server is ready to accept requests");
                        }
                    }
                } catch (IOException e) {
                    if (isRunning) {
                        Gdx.app.error("KoboldCpp", "Error reading output", e);
                    }
                }
            }, "KoboldCpp-Output");
            outputThread.setDaemon(true);
            outputThread.start();

            Gdx.app.log("KoboldCpp", "Waiting for KoboldCpp to start (this may take 15-30 seconds)...");
            Thread.sleep(20000);

            if (!koboldProcess.isAlive()) {
                Gdx.app.error("KoboldCpp", "Process terminated unexpectedly");
                isRunning = false;
                return false;
            }

            Gdx.app.log("KoboldCpp", "KoboldCpp started successfully");
            return true;

        } catch (Exception e) {
            Gdx.app.error("KoboldCpp", "Failed to start KoboldCpp", e);
            isRunning = false;
            return false;
        }
    }

    public void stopKoboldCpp() {
        if (koboldProcess != null && koboldProcess.isAlive()) {
            Gdx.app.log("KoboldCpp", "Stopping KoboldCpp...");
            
            koboldProcess.descendants().forEach(processHandle -> {
                Gdx.app.log("KoboldCpp", "Destroying child process: " + processHandle.pid());
                processHandle.destroyForcibly();
            });
            
            koboldProcess.destroy();
            
            try {
                boolean exited = koboldProcess.waitFor(3, java.util.concurrent.TimeUnit.SECONDS);
                if (!exited) {
                    Gdx.app.log("KoboldCpp", "Process did not exit gracefully, forcing...");
                    koboldProcess.destroyForcibly();
                    koboldProcess.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Gdx.app.log("KoboldCpp", "Interrupted while waiting for process to stop");
                koboldProcess.destroyForcibly();
                Thread.currentThread().interrupt();
            }
            
            isRunning = false;
            Gdx.app.log("KoboldCpp", "KoboldCpp stopped");
        } else {
            Gdx.app.log("KoboldCpp", "KoboldCpp is not running");
        }
        
        if (outputThread != null && outputThread.isAlive()) {
            try {
                outputThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isRunning() {
        return isRunning && koboldProcess != null && koboldProcess.isAlive();
    }
}
