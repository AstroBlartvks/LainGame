package com.astro.minor.wired.layout;

import com.badlogic.gdx.graphics.Color;

public class LayoutContext {
    public float containerX;
    public float containerY;
    public float containerWidth;
    public float currentX;
    public float currentY;
    
    public Color defaultTextColor = Color.WHITE;
    public Color currentColor = Color.WHITE;
    
    public boolean bold = false;
    public boolean italic = false;
    public boolean underline = false;
    public float fontScale = 1.0f;
    
    public float lineHeight = 20;
    
    public int listDepth = 0;
    public boolean isOrderedList = false;
    public int listCounter = 1;
    
    public LayoutContext(float x, float y, float width) {
        this.containerX = x;
        this.containerY = y;
        this.containerWidth = width;
        this.currentX = x;
        this.currentY = y;
        this.currentColor = defaultTextColor;
    }
    
    public LayoutContext createChild() {
        LayoutContext child = new LayoutContext(containerX, currentY, containerWidth);
        child.lineHeight = this.lineHeight;
        child.defaultTextColor = this.defaultTextColor;
        child.currentColor = this.currentColor;
        child.bold = this.bold;
        child.italic = this.italic;
        child.underline = this.underline;
        child.fontScale = this.fontScale;
        child.listDepth = this.listDepth;
        child.isOrderedList = this.isOrderedList;
        child.listCounter = this.listCounter;
        return child;
    }
    
    public void newLine() {
        if (currentX > containerX) {
            currentY -= lineHeight;
            currentX = containerX;
        }
    }
}
