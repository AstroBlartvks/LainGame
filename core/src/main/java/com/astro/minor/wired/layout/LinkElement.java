package com.astro.minor.wired.layout;

import com.badlogic.gdx.math.Rectangle;

public class LinkElement extends LayoutElement {
    private String href;
    private Rectangle bounds;
    
    public LinkElement(String href) {
        this.href = href;
        this.bounds = new Rectangle();
    }
    
    @Override
    public void layout(LayoutContext ctx) {
        for (LayoutElement child : children) {
            child.layout(ctx);
        }
        updateBounds();
    }
    
    @Override
    public float getContentHeight() {
        float maxHeight = 0;
        for (LayoutElement child : children) {
            maxHeight = Math.max(maxHeight, child.getContentHeight());
        }
        return maxHeight;
    }
    
    private void updateBounds() {
        if (children.isEmpty()) return;
        
        float minX = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;
        
        for (LayoutElement child : children) {
            minX = Math.min(minX, child.getX());
            maxX = Math.max(maxX, child.getX() + child.getWidth());
            minY = Math.min(minY, child.getY() - child.getHeight());
            maxY = Math.max(maxY, child.getY());
        }
        
        if (minX != Float.MAX_VALUE) {
            bounds.set(minX, minY, maxX - minX, maxY - minY);
        }
    }
    
    public String getHref() { return href; }
    public Rectangle getBounds() { return bounds; }
    
    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }
}
