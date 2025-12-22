package com.astro.minor.wired.layout;

import java.util.ArrayList;
import java.util.List;

public class TableRowElement extends LayoutElement {
    
    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        this.width = ctx.containerWidth;
        
        List<TableCellElement> cells = new ArrayList<>();
        for (LayoutElement child : children) {
            if (child instanceof TableCellElement) {
                cells.add((TableCellElement) child);
            }
        }
        
        if (cells.isEmpty()) return;
        
        float defaultCellWidth = ctx.containerWidth / cells.size();
        float startY = ctx.currentY;
        float cellX = ctx.containerX;
        float maxHeight = 0;
        
        for (TableCellElement cell : cells) {
            float cellWidth = cell.getWidth() > 0 ? cell.getWidth() : defaultCellWidth;
            
            LayoutContext cellCtx = ctx.createChild();
            cellCtx.containerX = cellX;
            cellCtx.containerWidth = cellWidth - 10;
            cellCtx.currentY = startY - 5;
            
            cell.layout(cellCtx);
            cell.setX(cellX);
            cell.setY(startY);
            cell.setWidth(cellWidth);
            
            maxHeight = Math.max(maxHeight, cell.getHeight() + 10);
            cellX += cellWidth;
        }
        
        for (TableCellElement cell : cells) {
            cell.setHeight(maxHeight);
        }
        
        this.height = maxHeight;
        ctx.currentY = startY - maxHeight;
        ctx.currentX = ctx.containerX;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
}
