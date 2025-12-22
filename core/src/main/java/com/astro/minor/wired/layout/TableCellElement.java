package com.astro.minor.wired.layout;

public class TableCellElement extends LayoutElement {
    private String align = "left";
    
    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        this.width = ctx.containerWidth;
        
        float startY = ctx.currentY;
        LayoutContext childCtx = ctx.createChild();
        childCtx.currentColor = this.textColor;
        
        for (LayoutElement child : children) {
            child.layout(childCtx);
        }
        
        if (childCtx.currentX > childCtx.containerX) {
            childCtx.currentY -= childCtx.lineHeight;
        }
        
        this.height = startY - childCtx.currentY;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
    
    public String getAlign() { return align; }
    public void setAlign(String align) { this.align = align; }
}
