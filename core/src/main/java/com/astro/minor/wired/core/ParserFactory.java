package com.astro.minor.wired.core;

import java.util.HashMap;
import java.util.Map;

public class ParserFactory {
    private final Map<String, ElementParser> parsers = new HashMap<>();
    private final ElementParser defaultParser;

    public ParserFactory() {
        this.defaultParser = new DefaultElementParser();
        registerParsers();
    }

    private void registerParsers() {
        parsers.put("wpml", new WpmlElementParser());
    }

    public ElementParser getParser(String tagName) {
        return parsers.getOrDefault(tagName, defaultParser);
    }
}

