package com.astro.minor.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

import java.util.ArrayList;
import java.util.List;

public class ComputerAmbience implements Disposable {
    private List<Sound> hddSounds;
    private Sound fanSound;
    private Sound fanHarderSound;
    private List<Sound> noiseSounds;

    private long fanSoundId;
    private boolean isRunning;

    private float systemLoad = 0.0f;
    private float loadTimer = 0;
    private float fanAccelerationTimer = 0;
    private long fanHarderSoundId;
    private boolean isFanAccelerated = false;

    public ComputerAmbience() {
        hddSounds = new ArrayList<>();
        noiseSounds = new ArrayList<>();

        hddSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/hdd/shelchok.wav")));
        hddSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/hdd/skrezhet.wav")));

        fanSound = Gdx.audio.newSound(Gdx.files.internal("sounds/fan/fan.wav"));
        fanHarderSound = Gdx.audio.newSound(Gdx.files.internal("sounds/fan/fan-harder.wav"));

        noiseSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/noise/noise_small.wav")));
        noiseSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/noise/noise_mid_1.wav")));
        noiseSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/noise/noise_mid_2.wav")));
        noiseSounds.add(Gdx.audio.newSound(Gdx.files.internal("sounds/noise/noise_long.wav")));
    }

    public void start() {
        isRunning = true;
        fanSoundId = fanSound.loop(0.2f);
    }

    public void stop() {
        isRunning = false;
        fanSound.stop(fanSoundId);
        if (isFanAccelerated) {
            fanHarderSound.stop();
        }
    }

    public void update(float delta) {
        if (!isRunning) return;

        updateSystemLoad(delta);
        updateFanAcceleration(delta);
        adaptToSystemLoad();
        playRandomEvents(delta);
    }

    private void updateSystemLoad(float delta) {
        loadTimer += delta;

        if (loadTimer > 8.0f) {
            loadTimer = 0;

            if (MathUtils.randomBoolean(0.3f)) {
                systemLoad = MathUtils.random(0.7f, 0.9f);
            } else {
                systemLoad = MathUtils.random(0.1f, 0.4f);
            }
        }
    }

    private void updateFanAcceleration(float delta) {
        if (isFanAccelerated) {
            fanAccelerationTimer += delta;

            if (fanAccelerationTimer > MathUtils.random(2.0f, 3.0f)) {
                stopFanAcceleration();
            }
        } else if (MathUtils.random() < 0.1f * delta && systemLoad > 0.5f) {
            startFanAcceleration();
        }
    }

    private void startFanAcceleration() {
        if (isFanAccelerated) return;

        isFanAccelerated = true;
        fanAccelerationTimer = 0;

        fanSound.setVolume(fanSoundId, 0.1f);

        float volume = 0.3f + (systemLoad * 0.3f);
        fanHarderSoundId = fanHarderSound.loop(volume);
    }

    private void stopFanAcceleration() {
        if (!isFanAccelerated) return;

        isFanAccelerated = false;
        fanHarderSound.stop(fanHarderSoundId);

        adaptToSystemLoad();
    }

    public void setVolume(float volume) {
        float mainVolume = volume * 0.3f;
        fanSound.setVolume(fanSoundId, mainVolume);

        if (isFanAccelerated) {
            float harderVolume = volume * 0.5f;
            fanHarderSound.setVolume(fanHarderSoundId, harderVolume);
        }
    }

    private void adaptToSystemLoad() {
        float fanVolume = 0.2f + (systemLoad * 0.3f);
        float fanPitch = 0.9f + (systemLoad * 0.4f);

        fanSound.setVolume(fanSoundId, fanVolume);
        fanSound.setPitch(fanSoundId, fanPitch);
    }

    private void playRandomEvents(float delta) {
        float hddChance = 0.03f + (systemLoad * 0.2f);
        if (MathUtils.random() < hddChance * delta) {
            playHddSound();
        }

        if (MathUtils.random() < 0.008f * delta) {
            playNoiseSound();
        }
    }

    private void playHddSound() {
        Sound hddSound = hddSounds.get(MathUtils.random(hddSounds.size() - 1));
        float volume = MathUtils.random(0.08f, 0.18f);
        float pitch = MathUtils.random(0.8f, 1.2f);

        if (hddSounds.indexOf(hddSound) == 1) {
            volume = MathUtils.random(0.12f, 0.22f);
        }

        hddSound.play(volume, pitch, 0);
    }

    private void playNoiseSound() {
        int noiseIndex;
        float random = MathUtils.random();

        if (random < 0.5f) {
            noiseIndex = 0;
        } else if (random < 0.8f) {
            noiseIndex = MathUtils.random(1, 2);
        } else {
            noiseIndex = 3;
        }

        Sound noiseSound = noiseSounds.get(noiseIndex);
        float volume = MathUtils.random(0.05f, 0.12f);

        if (noiseIndex >= 2) {
            volume = MathUtils.random(0.08f, 0.15f);
        }

        noiseSound.play(volume);
    }

    public void setSystemLoad(float load) {
        this.systemLoad = MathUtils.clamp(load, 0, 1);
    }
    
    public float getSystemLoad() {
        return systemLoad;
    }

    @Override
    public void dispose() {
        stop();

        for (Sound sound : hddSounds) {
            sound.dispose();
        }
        for (Sound sound : noiseSounds) {
            sound.dispose();
        }
        fanSound.dispose();
        fanHarderSound.dispose();
    }
}
