package com.astro.minor.wired.layout;

public class ButtonElement extends LayoutElement {
    private String buttonText;
    private String actionType;
    private String actionTarget;
    
    public ButtonElement(String text, String type) {
        this.buttonText = text;
        parseActionType(type);
    }
    
    private void parseActionType(String type) {
        if ("submit".equals(type)) {
            this.actionType = "submit";
        } else if (type != null && type.startsWith("onclick:")) {
            this.actionType = "onclick";
            this.actionTarget = type.substring(8);
        } else {
            this.actionType = "onclick";
            this.actionTarget = "defaultFunction";
        }
    }
    
    @Override
    public void layout(LayoutContext ctx) {
        ctx.newLine();
        
        if (width == 0) width = 100;
        if (height == 0) height = 30;
        
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        
        ctx.currentY -= this.height + 5;
        ctx.currentX = ctx.containerX;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
    
    public String getButtonText() { return buttonText; }
    public String getActionType() { return actionType; }
    public String getActionTarget() { return actionTarget; }
    
    public boolean isSubmitButton() {
        return "submit".equals(actionType);
    }
    
    public FormElement findParentForm() {
        LayoutElement current = parent;
        while (current != null) {
            if (current instanceof FormElement) {
                return (FormElement) current;
            }
            current = current.parent;
        }
        return null;
    }
}
