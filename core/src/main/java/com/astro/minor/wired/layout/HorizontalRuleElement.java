package com.astro.minor.wired.layout;

public class HorizontalRuleElement extends LayoutElement {
    private float padding = 10f;
    
    public HorizontalRuleElement() {
        this.height = 2;
    }
    
    @Override
    public void layout(LayoutContext ctx) {
        ctx.newLine();
        ctx.currentY -= padding;
        
        if (width == 0) width = ctx.containerWidth;
        
        this.x = ctx.containerX + (ctx.containerWidth - width) / 2;
        this.y = ctx.currentY;
        
        ctx.currentY -= padding;
    }
    
    @Override
    public float getContentHeight() {
        return height + padding * 2;
    }
    
    public float getPadding() { return padding; }
    public void setPadding(float padding) { this.padding = padding; }
}
