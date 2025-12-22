package com.astro.minor.wired.layout;

import com.badlogic.gdx.graphics.Texture;

public class ImageElement extends LayoutElement {
    private Texture texture;
    private String src;
    
    public ImageElement(String src, Texture texture) {
        this.src = src;
        this.texture = texture;
    }
    
    @Override
    public void layout(LayoutContext ctx) {
        ctx.newLine();
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        
        if (texture != null) {
            this.width = texture.getWidth();
            this.height = texture.getHeight();
        }
        
        ctx.currentY -= this.height + 5;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
    
    public Texture getTexture() { return texture; }
    public String getSrc() { return src; }
}
