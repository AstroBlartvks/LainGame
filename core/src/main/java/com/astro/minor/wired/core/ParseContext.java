package com.astro.minor.wired.core;

import java.util.*;

public class ParseContext {
    private final Map<String, String> metadata = new HashMap<>();
    private final Stack<WiredElement> elementStack = new Stack<>();
    private final List<WiredElement> rootElements = new ArrayList<>();

    public void pushElement(WiredElement element) {
        if (!elementStack.isEmpty()) {
            elementStack.peek().addChild(element);
        } else {
            rootElements.add(element);
        }
        elementStack.push(element);
    }

    public WiredElement popElement() {
        return elementStack.pop();
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public List<WiredElement> getRootElements() {
        return rootElements;
    }

    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public Stack<WiredElement> getElementStack() {
        return elementStack;
    }
}


