package com.astro.minor.wired.layout;

public class TextElement extends LayoutElement {
    private String text;
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private float fontScale = 1.0f;
    
    public TextElement(String text) {
        this.text = text;
    }
    
    @Override
    public void layout(LayoutContext ctx) {
        this.x = ctx.currentX;
        this.y = ctx.currentY;
        this.textColor = ctx.currentColor;
        this.bold = ctx.bold;
        this.italic = ctx.italic;
        this.underline = ctx.underline;
        this.fontScale = ctx.fontScale;
        this.height = ctx.lineHeight;
    }
    
    @Override
    public float getContentHeight() {
        return height;
    }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public boolean isBold() { return bold; }
    public void setBold(boolean bold) { this.bold = bold; }
    
    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }
    
    public boolean isUnderline() { return underline; }
    public void setUnderline(boolean underline) { this.underline = underline; }
    
    public float getFontScale() { return fontScale; }
    public void setFontScale(float scale) { this.fontScale = scale; }
}
