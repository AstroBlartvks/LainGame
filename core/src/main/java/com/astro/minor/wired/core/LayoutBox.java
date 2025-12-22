package com.astro.minor.wired.core;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;


public class LayoutBox {
    public enum Type {
        BLOCK,      // Takes full width, stacks vertically
        INLINE,     // Flows horizontally, wraps
        TABLE,
        TABLE_ROW,
        TABLE_CELL,
        TEXT,
        IMAGE,
        HR,
        FORM,       // Form container
        INPUT,      // Text input field
        BUTTON      // Clickable button
    }

    public Type type;
    public float x;
    public float y;
    public float width;
    public float height;

    public Color textColor;
    public Color backgroundColor;
    public Color borderColor;
    public int borderWidth;
    public String align; // "left", "center", "right"

    public String text;
    public Texture texture; // For images
    public WiredElement element; // Reference to source element

    public boolean bold = false;
    public boolean italic = false;
    public boolean underline = false;
    public float fontScale = 1.0f;

    public List<LayoutBox> children = new ArrayList<>();
    public LayoutBox parent;

    public boolean visible = true; // For blink effect
    public float marqueeOffset = 0; // For marquee effect
    public LinkInfo linkInfo; // For clickable links
    
    public String formName; // For FORM type
    public String inputName; // For INPUT type
    public String inputValue = ""; // Current input value
    public String placeholder = ""; // Placeholder text
    public int cursorPosition = 0; // Cursor position in input
    public boolean focused = false; // Is input focused
    
    public String buttonType; // "submit" or "onclick:functionName"
    public String buttonText; // Button label

    public LayoutBox(Type type) {
        this.type = type;
        this.textColor = Color.WHITE;
    }

    public void addChild(LayoutBox child) {
        children.add(child);
        child.parent = this;
    }

    @Override
    public String toString() {
        return String.format("LayoutBox[%s](%.1f, %.1f, %.1fx%.1f)",
            type, x, y, width, height);
    }
}

