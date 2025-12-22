package com.astro.minor.wired.layout;

public class BlockElement extends LayoutElement {
    private String align = "left";
    
    @Override
    public void layout(LayoutContext ctx) {
        ctx.newLine();
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        this.width = ctx.containerWidth;
        
        float startY = ctx.currentY;
        LayoutContext childCtx = ctx.createChild();
        childCtx.currentColor = this.textColor;
        
        for (LayoutElement child : children) {
            child.layout(childCtx);
        }
        
        this.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
    
    public String getAlign() { return align; }
    public void setAlign(String align) { this.align = align; }
}
