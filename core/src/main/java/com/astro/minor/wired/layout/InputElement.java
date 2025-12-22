package com.astro.minor.wired.layout;

public class InputElement extends LayoutElement {
    private String name;
    private String value = "";
    private String placeholder = "";
    private int cursorPosition = 0;
    private boolean focused = false;
    
    public InputElement(String name) {
        this.name = name;
    }
    
    @Override
    public void layout(LayoutContext ctx) {
        ctx.newLine();
        
        this.width = width > 0 ? width : 200;
        this.height = height > 0 ? height : 25;
        
        this.x = ctx.containerX;
        this.y = ctx.currentY;
        
        ctx.currentY -= this.height + 5;
        ctx.currentX = ctx.containerX;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
    
    public String getName() { return name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    
    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    
    public int getCursorPosition() { return cursorPosition; }
    public void setCursorPosition(int pos) { this.cursorPosition = pos; }
    
    public boolean isFocused() { return focused; }
    public void setFocused(boolean focused) { this.focused = focused; }
    
    public void insertChar(char c) {
        if (value.length() < 512) {
            String before = value.substring(0, cursorPosition);
            String after = value.substring(cursorPosition);
            value = before + c + after;
            cursorPosition++;
        }
    }
    
    public void backspace() {
        if (cursorPosition > 0) {
            String before = value.substring(0, cursorPosition - 1);
            String after = value.substring(cursorPosition);
            value = before + after;
            cursorPosition--;
        }
    }
    
    public void moveCursorLeft() {
        if (cursorPosition > 0) cursorPosition--;
    }
    
    public void moveCursorRight() {
        if (cursorPosition < value.length()) cursorPosition++;
    }
}
