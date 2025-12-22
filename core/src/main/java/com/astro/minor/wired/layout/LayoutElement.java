package com.astro.minor.wired.layout;

import com.astro.minor.wired.core.WiredElement;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public abstract class LayoutElement {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    
    protected Color textColor = Color.WHITE;
    protected Color backgroundColor;
    protected Color borderColor;
    protected int borderWidth;
    
    protected List<LayoutElement> children = new ArrayList<>();
    protected LayoutElement parent;
    protected WiredElement sourceElement;
    
    protected boolean visible = true;
    
    public abstract void layout(LayoutContext ctx);
    public abstract float getContentHeight();
    
    public void addChild(LayoutElement child) {
        children.add(child);
        child.parent = this;
    }
    
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    
    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }
    
    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }
    
    public Color getTextColor() { return textColor; }
    public void setTextColor(Color color) { this.textColor = color; }
    
    public Color getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(Color color) { this.backgroundColor = color; }
    
    public Color getBorderColor() { return borderColor; }
    public void setBorderColor(Color color) { this.borderColor = color; }
    
    public int getBorderWidth() { return borderWidth; }
    public void setBorderWidth(int width) { this.borderWidth = width; }
    
    public List<LayoutElement> getChildren() { return children; }
    public LayoutElement getParent() { return parent; }
    
    public WiredElement getSourceElement() { return sourceElement; }
    public void setSourceElement(WiredElement element) { this.sourceElement = element; }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
