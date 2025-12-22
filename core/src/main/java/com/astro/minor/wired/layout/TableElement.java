package com.astro.minor.wired.layout;

public class TableElement extends LayoutElement {
    
    @Override
    public void layout(LayoutContext ctx) {
        ctx.newLine();
        
        this.width = width > 0 ? width : ctx.containerWidth;
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        
        float startY = ctx.currentY;
        LayoutContext childCtx = ctx.createChild();
        childCtx.containerX = this.x;
        childCtx.containerWidth = this.width;
        
        for (LayoutElement child : children) {
            child.layout(childCtx);
        }
        
        this.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;
        ctx.currentY -= ctx.lineHeight * 0.5f;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
}
