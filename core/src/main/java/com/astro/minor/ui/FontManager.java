package com.astro.minor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.HashMap;
import java.util.Map;


public class FontManager {
    private static FontManager instance;
    private Map<String, BitmapFont> fonts;
    private FreeTypeFontGenerator generator;

    private FontManager() {
        fonts = new HashMap<>();
        try {
            generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        } catch (Exception e) {
            System.err.println("Ошибка загрузки шрифта arial.ttf: " + e.getMessage());
            generator = null;
        }
    }

    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }

    
    public BitmapFont getFont(int size) {
        String key = "arial_" + size;
        if (!fonts.containsKey(key)) {
            fonts.put(key, createFont(size));
        }
        return fonts.get(key);
    }

    
    private BitmapFont createFont(int size) {
        if (generator == null) {
            return new BitmapFont();
        }

        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS;
        parameter.characters += "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;
        parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;

        return generator.generateFont(parameter);
    }

    
    public BitmapFont createCustomFont(int size, FreeTypeFontParameter parameter) {
        if (generator == null) {
            return new BitmapFont();
        }

        if (parameter == null) {
            return createFont(size);
        }

        parameter.size = size;
        if (!parameter.characters.contains("А")) {
            parameter.characters += "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя";
        }

        return generator.generateFont(parameter);
    }

    
    public void dispose() {
        for (BitmapFont font : fonts.values()) {
            font.dispose();
        }
        fonts.clear();
        if (generator != null) {
            generator.dispose();
        }
    }
}