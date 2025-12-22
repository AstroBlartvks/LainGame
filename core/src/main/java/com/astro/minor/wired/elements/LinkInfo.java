package com.astro.minor.wired.elements;

import com.badlogic.gdx.math.Rectangle;

public class LinkInfo {
    public final String href;
    public final Rectangle bounds;
    public final String text;

    public LinkInfo(String href, float x, float y, float width, float height, String text) {
        this.href = href;
        this.bounds = new Rectangle(x, y, width, height);
        this.text = text;
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }
}

