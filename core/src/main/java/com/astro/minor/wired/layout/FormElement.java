package com.astro.minor.wired.layout;

import java.util.HashMap;
import java.util.Map;

public class FormElement extends LayoutElement {
    private String formName;
    
    public FormElement(String name) {
        this.formName = name;
    }
    
    @Override
    public void layout(LayoutContext ctx) {
        ctx.newLine();
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        this.width = ctx.containerWidth;
        
        float startY = ctx.currentY;
        LayoutContext childCtx = ctx.createChild();
        
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
    
    public String getFormName() { return formName; }
    
    public Map<String, String> collectFormData() {
        Map<String, String> data = new HashMap<>();
        collectInputs(this, data);
        return data;
    }
    
    private void collectInputs(LayoutElement element, Map<String, String> data) {
        if (element instanceof InputElement) {
            InputElement input = (InputElement) element;
            if (input.getName() != null && !input.getName().isEmpty()) {
                data.put(input.getName(), input.getValue());
            }
        }
        for (LayoutElement child : element.getChildren()) {
            collectInputs(child, data);
        }
    }
}
