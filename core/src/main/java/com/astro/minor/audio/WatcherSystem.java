package com.astro.minor.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class WatcherSystem implements Disposable {
    private static WatcherSystem instance;

    private boolean isActive;
    private float timer;
    private float nextEventTime;
    private float breathTimer;
    private float breathPhase;

    private Sound breathSound;
    private Sound heartbeatSound;
    private long breathSoundId = -1;
    private long heartbeatSoundId = -1;

    private boolean isBreathing;
    private boolean heartbeatActive;

    private List<WatcherEventListener> listeners;
    private List<String> connectionLogs;

    private static final float MIN_EVENT_INTERVAL = 30f;
    private static final float MAX_EVENT_INTERVAL = 120f;
    private static final float EVENT_CHANCE = 0.1f;

    private static final String[] UNKNOWN_IPS = {
        "???.???.?.???",
        "███.███.█.███",
        "192.168.?.???",
        "127.0.0.???",
        "0.0.0.0"
    };

    private WatcherSystem() {
        listeners = new ArrayList<>();
        connectionLogs = new ArrayList<>();
        isActive = false;
        timer = 0;
        breathTimer = 0;
        breathPhase = 0;
        isBreathing = false;
        heartbeatActive = false;

        scheduleNextEvent();
        loadSounds();
    }

    private void loadSounds() {
        try {
            if (Gdx.files.internal("sounds/watcher/breath.wav").exists()) {
                breathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/watcher/breath.wav"));
            }
            if (Gdx.files.internal("sounds/watcher/heartbeat.wav").exists()) {
                heartbeatSound = Gdx.audio.newSound(Gdx.files.internal("sounds/watcher/heartbeat.wav"));
            }
        } catch (Exception e) {
            System.err.println("[WatcherSystem] Звуки не найдены: " + e.getMessage());
        }
    }

    public static WatcherSystem getInstance() {
        if (instance == null) {
            instance = new WatcherSystem();
        }
        return instance;
    }

    public void start() {
        isActive = true;
    }

    public void stop() {
        isActive = false;
        stopAllSounds();
    }

    private void stopAllSounds() {
        if (breathSound != null && breathSoundId != -1) {
            breathSound.stop(breathSoundId);
            breathSoundId = -1;
        }
        if (heartbeatSound != null && heartbeatSoundId != -1) {
            heartbeatSound.stop(heartbeatSoundId);
            heartbeatSoundId = -1;
        }
        isBreathing = false;
        heartbeatActive = false;
    }

    public void update(float delta) {
        if (!isActive) return;

        timer += delta;
        breathTimer += delta;

        if (timer >= nextEventTime) {
            triggerWatcherEvent();
            scheduleNextEvent();
            timer = 0;
        }

        updateBreathing(delta);
    }

    private void scheduleNextEvent() {
        nextEventTime = MathUtils.random(MIN_EVENT_INTERVAL, MAX_EVENT_INTERVAL);
    }

    private void triggerWatcherEvent() {
        if (MathUtils.random() > EVENT_CHANCE) return;

        int eventType = MathUtils.random(0, 2);

        switch (eventType) {
            case 0:
                generateConnectionLog();
                notifyListeners();
                break;
            case 1:
                startBreathing();
                break;
            case 2:
                startHeartbeat();
                break;
        }
    }

    private void generateConnectionLog() {
        Calendar cal1998 = new GregorianCalendar(1998, Calendar.APRIL, 15);
        Calendar now = Calendar.getInstance();
        cal1998.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        cal1998.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
        cal1998.set(Calendar.SECOND, now.get(Calendar.SECOND));

        String timestamp = new SimpleDateFormat("HH:mm:ss").format(cal1998.getTime());
        String ip = UNKNOWN_IPS[MathUtils.random(0, UNKNOWN_IPS.length - 1)];

        String[] messages = {
            "[" + timestamp + "] UNKNOWN (" + ip + ") подключился к ВАМ",
            "[" + timestamp + "] UNKNOWN наблюдает...",
            "[" + timestamp + "] UNKNOWN отключился",
            "[" + timestamp + "] Обнаружена аномалия в протоколе",
            "[" + timestamp + "] Неизвестное соединение на порту 7777",
            "[" + timestamp + "] Кто-то читает ваши файлы...",
            "[" + timestamp + "] ПРЕДУПРЕЖДЕНИЕ: Обнаружен второй курсор",
            "[" + timestamp + "] Вы не одни в системе"
        };

        String log = messages[MathUtils.random(0, messages.length - 1)];
        connectionLogs.add(log);

        if (connectionLogs.size() > 50) {
            connectionLogs.remove(0);
        }

        System.out.println("[WATCHER] " + log);
    }

    private void startBreathing() {
        if (isBreathing || breathSound == null) return;

        isBreathing = true;
        breathPhase = 0;
        float volume = MathUtils.random(0.1f, 0.25f);
        breathSoundId = breathSound.loop(volume);

        Gdx.app.postRunnable(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(MathUtils.random(10000, 30000));
                    stopBreathing();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void stopBreathing() {
        if (breathSound != null && breathSoundId != -1) {
            breathSound.stop(breathSoundId);
            breathSoundId = -1;
        }
        isBreathing = false;
    }

    private void updateBreathing(float delta) {
        if (!isBreathing) return;

        breathPhase += delta;
        if (breathPhase > 4.0f) {
            breathPhase = 0;
        }
    }

    private void startHeartbeat() {
        if (heartbeatActive || heartbeatSound == null) return;

        heartbeatActive = true;
        float volume = MathUtils.random(0.15f, 0.3f);
        heartbeatSoundId = heartbeatSound.loop(volume);

        Gdx.app.postRunnable(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(MathUtils.random(5000, 15000));
                    stopHeartbeat();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void stopHeartbeat() {
        if (heartbeatSound != null && heartbeatSoundId != -1) {
            heartbeatSound.stop(heartbeatSoundId);
            heartbeatSoundId = -1;
        }
        heartbeatActive = false;
    }

    public void registerListener(WatcherEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unregisterListener(WatcherEventListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (WatcherEventListener listener : listeners) {
            listener.onWatcherEvent();
        }
    }

    public List<String> getConnectionLogs() {
        return new ArrayList<>(connectionLogs);
    }

    public String getLastConnectionLog() {
        if (connectionLogs.isEmpty()) return null;
        return connectionLogs.get(connectionLogs.size() - 1);
    }

    public boolean isBreathing() {
        return isBreathing;
    }

    public boolean isHeartbeatActive() {
        return heartbeatActive;
    }

    public void forceEvent() {
        triggerWatcherEventForced();
    }

    private void triggerWatcherEventForced() {
        int eventType = MathUtils.random(0, 2);

        switch (eventType) {
            case 0:
                generateConnectionLog();
                notifyListeners();
                break;
            case 1:
                startBreathing();
                break;
            case 2:
                startHeartbeat();
                break;
        }
    }

    @Override
    public void dispose() {
        stopAllSounds();
        if (breathSound != null) breathSound.dispose();
        if (heartbeatSound != null) heartbeatSound.dispose();
        listeners.clear();
        connectionLogs.clear();
    }

    public interface WatcherEventListener {
        void onWatcherEvent();
    }
}
