package com.astro.minor.wired.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

import java.io.File;
import java.util.*;


public class WiredPageRenderer {
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final float lineHeight;

    private Map<String, Texture> imageCache = new HashMap<>();
    private String currentDocumentPath;

    private float blinkTimer = 0;
    private Map<Integer, Float> marqueeOffsets = new HashMap<>();
    
    private LayoutBox focusedInput = null;
    private LayoutBox currentLayoutRoot = null; // Store for input handling

    public WiredPageRenderer(BitmapFont font) {
        this.font = font;
        this.layout = new GlyphLayout();
        this.lineHeight = font.getLineHeight();
    }

    
    public float render(SpriteBatch batch, ShapeRenderer shapeRenderer, WiredDocument document,
                       float x, float y, float width, float viewportHeight, float scrollOffset,
                       List<LinkInfo> links, String documentPath) {

        this.currentDocumentPath = documentPath;
        links.clear();

        LayoutBox rootLayout = calculateLayout(document, x, y, width);
        this.currentLayoutRoot = rootLayout; // Store for input handling

        renderLayout(batch, shapeRenderer, rootLayout, links);

        return rootLayout.height;
    }


    private LayoutBox calculateLayout(WiredDocument document, float x, float y, float width) {
        LayoutBox root = new LayoutBox(LayoutBox.Type.BLOCK);
        root.x = x;
        root.y = y;
        root.width = width;

        LayoutContext ctx = new LayoutContext(x, y, width);
        ctx.lineHeight = this.lineHeight;
        ctx.defaultTextColor = parseColor(document.getMetadata().getOrDefault("text", "#FFFFFF"));
        ctx.currentColor = ctx.defaultTextColor;

        for (WiredElement element : document.getRootElements()) {
            LayoutBox box = layoutElement(element, ctx);
            if (box != null) {
                root.addChild(box);
            }
        }

        root.height = y - ctx.currentY;
        return root;
    }

    private LayoutBox layoutElement(WiredElement element, LayoutContext ctx) {
        String tag = element.getTagName();

        switch (tag) {
            case "#text":
                return layoutText(element, ctx);
            case "wpml":
                return layoutContainer(element, ctx);
            case "p":
                return layoutParagraph(element, ctx);
            case "h1":
                return layoutHeading(element, ctx, 2.0f);
            case "h2":
                return layoutHeading(element, ctx, 1.5f);
            case "h3":
                return layoutHeading(element, ctx, 1.2f);
            case "div":
                return layoutDiv(element, ctx);
            case "center":
                return layoutCenter(element, ctx);
            case "br":
                return layoutBreak(ctx);
            case "hr":
                return layoutHorizontalRule(element, ctx);
            case "b":
            case "i":
            case "u":
            case "font":
                return layoutInlineStyle(element, ctx);
            case "a":
                return layoutLink(element, ctx);
            case "ul":
            case "ol":
                return layoutList(element, ctx);
            case "li":
                return layoutListItem(element, ctx);
            case "table":
                return layoutTable(element, ctx);
            case "tr":
                return layoutTableRow(element, ctx);
            case "td":
                return layoutTableCell(element, ctx);
            case "img":
                return layoutImage(element, ctx);
            case "blink":
                return layoutBlink(element, ctx);
            case "marquee":
                return layoutMarquee(element, ctx);
            case "forms":
                return layoutForm(element, ctx);
            case "input":
                return layoutInput(element, ctx);
            case "button":
                return layoutButton(element, ctx);
            default:
                return layoutContainer(element, ctx);
        }
    }

    private LayoutBox layoutText(WiredElement element, LayoutContext ctx) {
        String text = element.getTextContent();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        text = text.trim();

        String[] words = text.split("\\s+");
        List<LayoutBox> textBoxes = new ArrayList<>();

        for (String word : words) {
            if (word.isEmpty()) continue;

            String wordWithSpace = word + " ";
            layout.setText(font, wordWithSpace);
            float wordWidth = layout.width;

            if (ctx.currentX + wordWidth > ctx.containerX + ctx.containerWidth && ctx.currentX > ctx.containerX) {
                ctx.currentX = ctx.containerX;
                ctx.currentY -= ctx.lineHeight;
            }

            LayoutBox box = new LayoutBox(LayoutBox.Type.TEXT);
            box.element = element;
            box.text = wordWithSpace;
            box.textColor = ctx.currentColor;
            box.bold = ctx.bold;
            box.italic = ctx.italic;
            box.underline = ctx.underline;
            box.fontScale = ctx.fontScale;
            box.x = ctx.currentX;
            box.y = ctx.currentY;
            box.width = wordWidth;
            box.height = ctx.lineHeight;

            textBoxes.add(box);
            ctx.currentX += wordWidth;
        }

        if (textBoxes.isEmpty()) return null;
        if (textBoxes.size() == 1) return textBoxes.get(0);

        LayoutBox container = new LayoutBox(LayoutBox.Type.INLINE);
        container.element = element;
        for (LayoutBox tb : textBoxes) {
            container.addChild(tb);
        }
        return container;
    }

    private LayoutBox layoutParagraph(WiredElement element, LayoutContext ctx) {
        ctx.newLine(); // Start on new line

        LayoutBox box = new LayoutBox(LayoutBox.Type.BLOCK);
        box.element = element;
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;
        String align = element.getAttribute("align", "left");
        box.align = align;
        box.textColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : ctx.currentColor;

        float startY = ctx.currentY;

        LayoutContext childCtx = ctx.createChild();
        childCtx.currentColor = box.textColor;

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        if (childCtx.currentX > childCtx.containerX) {
            childCtx.currentY -= childCtx.lineHeight;
            childCtx.currentX = childCtx.containerX;
        }

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;
        ctx.currentY -= ctx.lineHeight * 0.5f; // Paragraph spacing

        if (!align.equals("left") && !box.children.isEmpty()) {
            float contentWidth = calculateContentWidthForLayout(box);
            float alignOffset = 0;
            
            if ("center".equals(align)) {
                alignOffset = (box.width - contentWidth) / 2;
            } else if ("right".equals(align)) {
                alignOffset = box.width - contentWidth - 5; // Small margin from right edge
            }
            
            if (alignOffset > 0) {
                applyOffsetToChildren(box, alignOffset);
            }
        }

        return box;
    }

    private LayoutBox layoutHeading(WiredElement element, LayoutContext ctx, float scale) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.BLOCK);
        box.element = element;
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;
        box.textColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : ctx.currentColor;

        float startY = ctx.currentY;

        float oldScale = font.getData().scaleX;
        font.getData().setScale(scale);

        LayoutContext childCtx = ctx.createChild();
        childCtx.currentColor = box.textColor;
        childCtx.lineHeight = lineHeight * scale; // Scale line height too
        childCtx.fontScale = scale; // Store scale for rendering
        childCtx.bold = true; // Headings are bold

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        font.getData().setScale(oldScale);

        if (childCtx.currentX > childCtx.containerX) {
            childCtx.currentY -= childCtx.lineHeight;
            childCtx.currentX = childCtx.containerX;
        }

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;
        ctx.currentY -= ctx.lineHeight * 0.5f;

        return box;
    }

    private LayoutBox layoutDiv(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.BLOCK);
        box.element = element;

        float divWidth = ctx.containerWidth;
        if (element.hasAttribute("width")) {
            divWidth = parseSize(element.getAttribute("width"), ctx.containerWidth);
        }

        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = divWidth;
        box.textColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : ctx.currentColor;
        box.backgroundColor = element.hasAttribute("bgcolor") ?
            parseColor(element.getAttribute("bgcolor")) : null;

        float startY = ctx.currentY;
        float padding = 5; // Add padding for divs
        ctx.currentY -= padding;

        LayoutContext childCtx = ctx.createChild();
        childCtx.containerWidth = divWidth - padding * 2;
        childCtx.containerX += padding;
        childCtx.currentX = childCtx.containerX;
        childCtx.currentColor = box.textColor;

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        if (childCtx.currentX > childCtx.containerX) {
            childCtx.currentY -= childCtx.lineHeight;
        }

        childCtx.currentY -= padding; // Bottom padding

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;

        return box;
    }

    private LayoutBox layoutCenter(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.BLOCK);
        box.element = element;
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;
        box.align = "center";

        float startY = ctx.currentY;

        LayoutContext childCtx = ctx.createChild();

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;

        if (!box.children.isEmpty()) {
            float contentWidth = calculateContentWidthForLayout(box);
            float alignOffset = (box.width - contentWidth) / 2;
            
            if (alignOffset > 0) {
                applyOffsetToChildren(box, alignOffset);
            }
        }

        return box;
    }

    private LayoutBox layoutBreak(LayoutContext ctx) {
        ctx.newLine();
        return null;
    }

    private LayoutBox layoutHorizontalRule(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.HR);
        box.element = element;

        float hrWidth = ctx.containerWidth;
        if (element.hasAttribute("width")) {
            hrWidth = parseSize(element.getAttribute("width"), ctx.containerWidth);
        }

        float padding = 10f;
        if (element.hasAttribute("padding")) {
            String paddingStr = element.getAttribute("padding");
            if (paddingStr.endsWith("px")) {
                paddingStr = paddingStr.substring(0, paddingStr.length() - 2);
            }
            padding = Float.parseFloat(paddingStr);
        }

        ctx.currentY -= padding; // Top padding

        box.x = ctx.containerX + (ctx.containerWidth - hrWidth) / 2; // Center
        box.y = ctx.currentY;
        box.width = hrWidth;
        box.height = 2;
        box.borderColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : Color.GRAY;

        ctx.currentY -= padding; // Bottom padding

        return box;
    }

    private LayoutBox layoutInlineStyle(WiredElement element, LayoutContext ctx) {
        Color oldColor = ctx.currentColor;
        boolean oldBold = ctx.bold;
        boolean oldItalic = ctx.italic;
        boolean oldUnderline = ctx.underline;

        String tag = element.getTagName();
        if ("b".equals(tag)) {
            ctx.bold = true;
        } else if ("i".equals(tag)) {
            ctx.italic = true;
        } else if ("u".equals(tag)) {
            ctx.underline = true;
        } else if ("font".equals(tag) && element.hasAttribute("color")) {
            ctx.currentColor = parseColor(element.getAttribute("color"));
        }

        LayoutBox container = new LayoutBox(LayoutBox.Type.INLINE);
        container.element = element;

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, ctx);
            if (childBox != null) {
                container.addChild(childBox);
            }
        }

        ctx.currentColor = oldColor;
        ctx.bold = oldBold;
        ctx.italic = oldItalic;
        ctx.underline = oldUnderline;

        return container.children.isEmpty() ? null : container;
    }

    private LayoutBox layoutLink(WiredElement element, LayoutContext ctx) {
        Color oldColor = ctx.currentColor;
        Color linkColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : Color.CYAN;
        ctx.currentColor = linkColor;

        LayoutBox container = new LayoutBox(LayoutBox.Type.INLINE);
        container.element = element;

        String href = element.getAttribute("href", "");
        container.linkInfo = new LinkInfo(href, 0f, 0f, 0f, 0f, "");

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, ctx);
            if (childBox != null) {
                container.addChild(childBox);
                if (childBox.type == LayoutBox.Type.TEXT) {
                    childBox.textColor = linkColor;
                }
            }
        }

        ctx.currentColor = oldColor;

        return container.children.isEmpty() ? null : container;
    }

    private LayoutBox layoutList(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.BLOCK);
        box.element = element;
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;

        float startY = ctx.currentY;

        LayoutContext childCtx = ctx.createChild();
        float listIndent = 30; // Increased from 20 for better visibility
        childCtx.containerX += listIndent;
        childCtx.currentX = childCtx.containerX; // FIX: Update currentX to match new containerX
        childCtx.containerWidth -= listIndent;
        childCtx.listDepth++;
        childCtx.isOrderedList = "ol".equals(element.getTagName());
        childCtx.listCounter = 1;

        for (WiredElement child : element.getChildren()) {
            if ("li".equals(child.getTagName())) {
                LayoutBox childBox = layoutElement(child, childCtx);
                if (childBox != null) {
                    box.addChild(childBox);
                }
                if (childCtx.isOrderedList) {
                    childCtx.listCounter++;
                }
            }
        }

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;

        return box;
    }

    private LayoutBox layoutListItem(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.BLOCK);
        box.element = element;
        box.x = ctx.containerX - 20; // Include space for bullet
        box.y = ctx.currentY;
        box.width = ctx.containerWidth + 20;

        float startY = ctx.currentY;

        String bullet = ctx.isOrderedList ? (ctx.listCounter + ". ") : "• ";
        layout.setText(font, bullet);
        float bulletWidth = layout.width;

        LayoutBox bulletBox = new LayoutBox(LayoutBox.Type.TEXT);
        bulletBox.text = bullet;
        bulletBox.x = ctx.containerX - bulletWidth - 5; // 5px margin
        bulletBox.y = ctx.currentY;
        bulletBox.width = bulletWidth;
        bulletBox.height = lineHeight;
        bulletBox.textColor = ctx.currentColor;
        bulletBox.bold = ctx.bold;
        bulletBox.italic = ctx.italic;
        bulletBox.underline = ctx.underline;
        bulletBox.fontScale = ctx.fontScale;
        box.addChild(bulletBox);

        LayoutContext childCtx = ctx.createChild();

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        if (childCtx.currentX > childCtx.containerX) {
            childCtx.currentY -= childCtx.lineHeight;
            childCtx.currentX = childCtx.containerX;
        }

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;

        return box;
    }

    private LayoutBox layoutTable(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.TABLE);
        box.element = element;

        float tableWidth = ctx.containerWidth;
        if (element.hasAttribute("width")) {
            tableWidth = parseSize(element.getAttribute("width"), ctx.containerWidth);
        }

        box.x = ctx.containerX;
        if (element.hasAttribute("align") && "center".equals(element.getAttribute("align"))) {
            box.x = ctx.containerX + (ctx.containerWidth - tableWidth) / 2;
        }

        box.y = ctx.currentY;
        box.width = tableWidth;
        box.borderWidth = element.hasAttribute("border") ?
            Integer.parseInt(element.getAttribute("border", "0")) : 0;
        box.borderColor = element.hasAttribute("bordercolor") ?
            parseColor(element.getAttribute("bordercolor")) : Color.WHITE;

        float startY = ctx.currentY;

        LayoutContext childCtx = ctx.createChild();
        childCtx.containerX = box.x;
        childCtx.containerWidth = tableWidth;

        for (WiredElement child : element.getChildren()) {
            if ("tr".equals(child.getTagName())) {
                LayoutBox childBox = layoutElement(child, childCtx);
                if (childBox != null) {
                    box.addChild(childBox);
                }
            }
        }

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;
        ctx.currentY -= ctx.lineHeight * 0.5f; // Add spacing after table

        return box;
    }

    private LayoutBox layoutTableRow(WiredElement element, LayoutContext ctx) {
        LayoutBox box = new LayoutBox(LayoutBox.Type.TABLE_ROW);
        box.element = element;
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;

        int cellCount = 0;
        for (WiredElement child : element.getChildren()) {
            if ("td".equals(child.getTagName())) {
                cellCount++;
            }
        }

        if (cellCount == 0) return null;

        float defaultCellWidth = ctx.containerWidth / cellCount;
        float startY = ctx.currentY;
        float cellX = ctx.containerX;

        List<Float> cellHeights = new ArrayList<>();
        float maxHeight = 0;

        for (WiredElement child : element.getChildren()) {
            if ("td".equals(child.getTagName())) {
                float cellWidth = child.hasAttribute("width") ?
                    parseSize(child.getAttribute("width"), ctx.containerWidth) : defaultCellWidth;

                LayoutContext cellCtx = ctx.createChild();
                cellCtx.containerX = cellX;
                cellCtx.containerWidth = cellWidth - 10; // Padding
                cellCtx.currentY = startY - 5; // Top padding

                LayoutBox cellBox = layoutTableCell(child, cellCtx);

                float cellHeight = cellBox != null ? cellBox.height + 10 : 10; // Add padding
                cellHeights.add(cellHeight);
                maxHeight = Math.max(maxHeight, cellHeight);

                cellX += cellWidth;
            }
        }

        cellX = ctx.containerX;
        int cellIndex = 0;

        for (WiredElement child : element.getChildren()) {
            if ("td".equals(child.getTagName())) {
                float cellWidth = child.hasAttribute("width") ?
                    parseSize(child.getAttribute("width"), ctx.containerWidth) : defaultCellWidth;

                LayoutContext cellCtx = ctx.createChild();
                cellCtx.containerX = cellX;
                cellCtx.containerWidth = cellWidth - 10;
                cellCtx.currentY = startY - 5;

                LayoutBox cellBox = layoutTableCell(child, cellCtx);
                if (cellBox != null) {
                    cellBox.x = cellX;
                    cellBox.y = startY;
                    cellBox.width = cellWidth;
                    cellBox.height = maxHeight;
                    box.addChild(cellBox);
                }

                cellX += cellWidth;
                cellIndex++;
            }
        }

        box.height = maxHeight;
        ctx.currentY = startY - maxHeight;
        ctx.currentX = ctx.containerX;

        return box;
    }

    private LayoutBox layoutTableCell(WiredElement element, LayoutContext ctx) {
        LayoutBox box = new LayoutBox(LayoutBox.Type.TABLE_CELL);
        box.element = element;
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;
        box.align = element.getAttribute("align", "left");
        box.textColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : ctx.currentColor;
        box.backgroundColor = element.hasAttribute("bgcolor") ?
            parseColor(element.getAttribute("bgcolor")) : null;

        float startY = ctx.currentY;

        LayoutContext childCtx = ctx.createChild();
        childCtx.currentColor = box.textColor;

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        if (childCtx.currentX > childCtx.containerX) {
            childCtx.currentY -= childCtx.lineHeight;
        }

        box.height = startY - childCtx.currentY;

        return box;
    }

    private LayoutBox layoutImage(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        String src = element.getAttribute("src", "");
        if (src.isEmpty()) return null;

        String imagePath = resolveImagePath(src);
        Texture texture = loadTexture(imagePath);
        if (texture == null) return null;

        LayoutBox box = new LayoutBox(LayoutBox.Type.IMAGE);
        box.element = element;
        box.texture = texture;

        float imgWidth = texture.getWidth();
        float imgHeight = texture.getHeight();

        if (element.hasAttribute("width")) {
            imgWidth = parseSize(element.getAttribute("width"), ctx.containerWidth);
            imgHeight = imgWidth * texture.getHeight() / texture.getWidth(); // Maintain aspect ratio
        }
        if (element.hasAttribute("height")) {
            imgHeight = Float.parseFloat(element.getAttribute("height"));
        }

        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = imgWidth;
        box.height = imgHeight;
        box.borderWidth = element.hasAttribute("border") ?
            Integer.parseInt(element.getAttribute("border", "0")) : 0;

        ctx.currentY -= imgHeight + 5; // Spacing

        return box;
    }

    private LayoutBox layoutBlink(WiredElement element, LayoutContext ctx) {
        LayoutBox container = new LayoutBox(LayoutBox.Type.INLINE);
        container.element = element;
        container.visible = (int)(blinkTimer * 2) % 2 == 0;

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, ctx);
            if (childBox != null) {
                childBox.visible = container.visible;
                container.addChild(childBox);
            }
        }

        return container.children.isEmpty() ? null : container;
    }

    private LayoutBox layoutMarquee(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.BLOCK);
        box.element = element;
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;

        int marqueeId = element.hashCode();
        box.marqueeOffset = marqueeOffsets.getOrDefault(marqueeId, 0f);

        LayoutContext childCtx = ctx.createChild();
        float contentStartX = childCtx.currentX;

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        float contentWidth = childCtx.currentX - contentStartX;

        box.marqueeOffset += Gdx.graphics.getDeltaTime() * 30;
        if (box.marqueeOffset > contentWidth + ctx.containerWidth) {
            box.marqueeOffset = 0;
        }
        marqueeOffsets.put(marqueeId, box.marqueeOffset);


        box.height = lineHeight;
        ctx.currentY -= lineHeight;
        ctx.currentX = ctx.containerX;

        return box;
    }

    private LayoutBox layoutForm(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.FORM);
        box.element = element;
        box.formName = element.getAttribute("name", "");
        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = ctx.containerWidth;



        float startY = ctx.currentY;

        LayoutContext childCtx = ctx.createChild();

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, childCtx);
            if (childBox != null) {
                box.addChild(childBox);
            }
        }

        box.height = startY - childCtx.currentY;
        ctx.currentY = childCtx.currentY;
        ctx.currentX = ctx.containerX;



        return box;
    }

    private LayoutBox layoutInput(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.INPUT);
        box.element = element;
        box.inputName = element.getAttribute("name", "");
        box.inputValue = element.getAttribute("value", "");
        box.placeholder = element.getAttribute("placeholder", "");
        box.cursorPosition = box.inputValue.length();



        float inputWidth = parseSize(element.getAttribute("width", "200"), ctx.containerWidth);
        float inputHeight = Float.parseFloat(element.getAttribute("height", "25"));

        box.textColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : Color.BLACK;
        box.backgroundColor = element.hasAttribute("bgcolor") ?
            parseColor(element.getAttribute("bgcolor")) : Color.WHITE;

        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = inputWidth;
        box.height = inputHeight;

        ctx.currentY -= inputHeight + 5; // Spacing
        ctx.currentX = ctx.containerX;



        return box;
    }

    private LayoutBox layoutButton(WiredElement element, LayoutContext ctx) {
        ctx.newLine();

        LayoutBox box = new LayoutBox(LayoutBox.Type.BUTTON);
        box.element = element;
        box.buttonType = element.getAttribute("type", "onclick:defaultFunction");
        
        StringBuilder buttonText = new StringBuilder();
        for (WiredElement child : element.getChildren()) {
            if ("#text".equals(child.getTagName())) {
                buttonText.append(child.getTextContent());
            }
        }
        box.buttonText = buttonText.toString().trim();



        box.textColor = element.hasAttribute("color") ?
            parseColor(element.getAttribute("color")) : Color.WHITE;
        box.backgroundColor = element.hasAttribute("bgcolor") ?
            parseColor(element.getAttribute("bgcolor")) : new Color(0.3f, 0.3f, 0.3f, 1f);

        layout.setText(font, box.buttonText);
        float buttonWidth = layout.width + 20; // Padding
        float buttonHeight = 30;

        box.x = ctx.containerX;
        box.y = ctx.currentY;
        box.width = buttonWidth;
        box.height = buttonHeight;

        ctx.currentY -= buttonHeight + 5; // Spacing
        ctx.currentX = ctx.containerX;



        return box;
    }

    private LayoutBox layoutContainer(WiredElement element, LayoutContext ctx) {
        LayoutBox container = new LayoutBox(LayoutBox.Type.BLOCK);
        container.element = element;

        for (WiredElement child : element.getChildren()) {
            LayoutBox childBox = layoutElement(child, ctx);
            if (childBox != null) {
                container.addChild(childBox);
            }
        }

        return container.children.isEmpty() ? null : container;
    }

    private float calculateContentWidthForLayout(LayoutBox box) {
        if (box.children.isEmpty()) return 0;

        float[] bounds = new float[]{Float.MAX_VALUE, Float.MIN_VALUE};
        
        collectTextBounds(box, bounds);
        float minX = bounds[0];
        float maxX = bounds[1];

        if (minX == Float.MAX_VALUE) return 0;
        
        float width = maxX - minX;
        return Math.max(0, Math.min(width, box.width));
    }

    private void collectTextBounds(LayoutBox box, float[] bounds) {
        if (box.type == LayoutBox.Type.TEXT) {
            bounds[0] = Math.min(bounds[0], box.x);
            bounds[1] = Math.max(bounds[1], box.x + box.width);
        }
        
        for (LayoutBox child : box.children) {
            collectTextBounds(child, bounds);
        }
    }

    private void applyOffsetToChildren(LayoutBox box, float offset) {
        for (LayoutBox child : box.children) {
            offsetXRecursive(child, offset);
        }
    }


    private void renderLayout(SpriteBatch batch, ShapeRenderer shapeRenderer,
                             LayoutBox layout, List<LinkInfo> links) {
        renderBox(batch, shapeRenderer, layout, links);
    }

    private void renderBox(SpriteBatch batch, ShapeRenderer shapeRenderer,
                          LayoutBox box, List<LinkInfo> links) {
        if (!box.visible) return;

        switch (box.type) {
            case BLOCK:
                renderBlock(batch, shapeRenderer, box, links);
                break;
            case TEXT:
                renderText(batch, box, links);
                break;
            case IMAGE:
                renderImage(batch, box);
                break;
            case HR:
                renderHR(batch, shapeRenderer, box);
                break;
            case TABLE:
                renderTable(batch, shapeRenderer, box, links);
                break;
            case TABLE_ROW:
                renderTableRow(batch, shapeRenderer, box, links);
                break;
            case TABLE_CELL:
                renderTableCell(batch, shapeRenderer, box, links);
                break;
            case INLINE:
                renderInline(batch, shapeRenderer, box, links);
                break;
            case FORM:
                renderForm(batch, shapeRenderer, box, links);
                break;
            case INPUT:
                renderInput(batch, shapeRenderer, box);
                break;
            case BUTTON:
                renderButton(batch, shapeRenderer, box);
                break;
        }
    }

    private void renderBlock(SpriteBatch batch, ShapeRenderer shapeRenderer,
                            LayoutBox box, List<LinkInfo> links) {
        if (box.backgroundColor != null) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(box.backgroundColor);
            shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
            shapeRenderer.end();
            batch.begin();
        }

        if (box.marqueeOffset > 0) {
            float offsetAmount = box.width - box.marqueeOffset;

            for (LayoutBox child : box.children) {
                offsetXRecursive(child, offsetAmount);
            }

            for (LayoutBox child : box.children) {
                renderBox(batch, shapeRenderer, child, links);
            }
        } else {
            for (LayoutBox child : box.children) {
                renderBox(batch, shapeRenderer, child, links);
            }
        }
    }

    private void offsetXRecursive(LayoutBox box, float offsetAmount) {
        box.x += offsetAmount;
        for (LayoutBox child : box.children) {
            offsetXRecursive(child, offsetAmount);
        }
    }

    private void renderText(SpriteBatch batch, LayoutBox box, List<LinkInfo> links) {
        if (box.text == null || box.text.isEmpty()) return;

        float oldScale = font.getData().scaleX;
        if (box.fontScale != 1.0f) {
            font.getData().setScale(box.fontScale);
        }

        font.setColor(box.textColor);

        if (box.bold) {
            font.draw(batch, box.text, box.x, box.y);
            font.draw(batch, box.text, box.x + 0.5f, box.y);
            font.draw(batch, box.text, box.x, box.y + 0.5f);
        } else {
            font.draw(batch, box.text, box.x, box.y);
        }

        if (box.underline) {
            layout.setText(font, box.text);
            batch.end();
            batch.begin();
        }

        if (box.fontScale != 1.0f) {
            font.getData().setScale(oldScale);
        }

    }

    private void renderImage(SpriteBatch batch, LayoutBox box) {
        if (box.texture == null) return;

        batch.draw(box.texture, box.x, box.y - box.height, box.width, box.height);

        if (box.borderWidth > 0) {
        }
    }

    private void renderHR(SpriteBatch batch, ShapeRenderer shapeRenderer, LayoutBox box) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(box.borderColor != null ? box.borderColor : Color.GRAY);
        shapeRenderer.rect(box.x, box.y - 2, box.width, 2);
        shapeRenderer.end();
        batch.begin();
    }

    private void renderTable(SpriteBatch batch, ShapeRenderer shapeRenderer,
                            LayoutBox box, List<LinkInfo> links) {
        if (box.borderWidth > 0) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(box.borderColor);
            shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
            shapeRenderer.end();
            batch.begin();
        }

        for (LayoutBox child : box.children) {
            renderBox(batch, shapeRenderer, child, links);
        }
    }

    private void renderTableRow(SpriteBatch batch, ShapeRenderer shapeRenderer,
                               LayoutBox box, List<LinkInfo> links) {
        for (LayoutBox child : box.children) {
            renderBox(batch, shapeRenderer, child, links);
        }
    }

    private void renderTableCell(SpriteBatch batch, ShapeRenderer shapeRenderer,
                                LayoutBox box, List<LinkInfo> links) {
        if (box.backgroundColor != null) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(box.backgroundColor);
            shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
            shapeRenderer.end();
            batch.begin();
        }

        for (LayoutBox child : box.children) {
            renderBox(batch, shapeRenderer, child, links);
        }

        if (box.parent != null && box.parent.parent != null &&
            box.parent.parent.borderWidth > 0) {
            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(box.parent.parent.borderColor);
            shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
            shapeRenderer.end();
            batch.begin();
        }
    }

    private void renderForm(SpriteBatch batch, ShapeRenderer shapeRenderer,
                           LayoutBox box, List<LinkInfo> links) {
        for (LayoutBox child : box.children) {
            renderBox(batch, shapeRenderer, child, links);
        }
    }

    private void renderInput(SpriteBatch batch, ShapeRenderer shapeRenderer, LayoutBox box) {

        
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(box.backgroundColor);
        shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (box.focused) {
            Gdx.gl.glLineWidth(2);
            shapeRenderer.setColor(Color.CYAN);
        } else {
            Gdx.gl.glLineWidth(1);
            shapeRenderer.setColor(Color.GRAY);
        }
        shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
        Gdx.gl.glLineWidth(1);
        shapeRenderer.end();

        batch.begin();

        String displayText = box.inputValue.isEmpty() ? box.placeholder : box.inputValue;
        Color displayColor = box.inputValue.isEmpty() ? Color.GRAY : box.textColor;
        
        font.setColor(displayColor);
        
        layout.setText(font, displayText);
        float textX = box.x + 5;
        float textY = box.y - box.height / 2 + layout.height / 2;
        
        font.draw(batch, displayText, textX, textY);

        if (box.focused) {
            String beforeCursor = box.inputValue.substring(0, Math.min(box.cursorPosition, box.inputValue.length()));
            layout.setText(font, beforeCursor);
            float cursorX = textX + layout.width;
            float cursorY1 = box.y - box.height + 5;
            float cursorY2 = box.y - 5;

            batch.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(box.textColor);
            shapeRenderer.rectLine(cursorX, cursorY1, cursorX, cursorY2, 2);
            shapeRenderer.end();
            batch.begin();
        }
    }

    private void renderButton(SpriteBatch batch, ShapeRenderer shapeRenderer, LayoutBox box) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(box.backgroundColor);
        shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(box.x, box.y - box.height, box.width, box.height);
        shapeRenderer.end();

        batch.begin();

        font.setColor(box.textColor);
        layout.setText(font, box.buttonText);
        float textX = box.x + (box.width - layout.width) / 2;
        float textY = box.y - box.height / 2 + layout.height / 2;
        font.draw(batch, box.buttonText, textX, textY);
    }

    private void renderInline(SpriteBatch batch, ShapeRenderer shapeRenderer,
                             LayoutBox box, List<LinkInfo> links) {
        for (LayoutBox child : box.children) {
            renderBox(batch, shapeRenderer, child, links);
        }

        if (box.linkInfo != null) {
            float[] bounds = new float[]{Float.MAX_VALUE, Float.MIN_VALUE, Float.MAX_VALUE, Float.MIN_VALUE};
            collectTextBoundsForLink(box, bounds);
            
            float minX = bounds[0];
            float maxX = bounds[1];
            float minY = bounds[2];
            float maxY = bounds[3];
            
            if (minX != Float.MAX_VALUE) {
                box.linkInfo.bounds.set(minX, minY, maxX - minX, maxY - minY);
                links.add(box.linkInfo);
            }
        }
    }

    private void collectTextBoundsForLink(LayoutBox box, float[] bounds) {
        if (box.type == LayoutBox.Type.TEXT) {
            bounds[0] = Math.min(bounds[0], box.x);
            bounds[1] = Math.max(bounds[1], box.x + box.width);
            bounds[2] = Math.min(bounds[2], box.y - box.height);
            bounds[3] = Math.max(bounds[3], box.y);
        }
        
        for (LayoutBox child : box.children) {
            collectTextBoundsForLink(child, bounds);
        }
    }


    private String resolveImagePath(String src) {
        if (new File(src).isAbsolute()) {
            return src;
        }

        if (currentDocumentPath != null) {
            File docFile = new File(currentDocumentPath);
            File parentDir = docFile.getParentFile();
            if (parentDir != null) {
                return new File(parentDir, src).getAbsolutePath();
            }
        }

        return src;
    }

    private Texture loadTexture(String path) {
        if (imageCache.containsKey(path)) {
            return imageCache.get(path);
        }

        try {
            com.badlogic.gdx.files.FileHandle fileHandle = Gdx.files.absolute(path);
            if (fileHandle.exists()) {
                Texture texture = new Texture(fileHandle);
                imageCache.put(path, texture);
                return texture;
            }
        } catch (Exception e) {
            System.err.println("[WiredPageRenderer] Failed to load image: " + path);
        }

        return null;
    }

    private Color parseColor(String colorStr) {
        if (colorStr == null) return Color.WHITE;

        try {
            if (colorStr.startsWith("#")) {
                colorStr = colorStr.substring(1);
            }
            int r = Integer.parseInt(colorStr.substring(0, 2), 16);
            int g = Integer.parseInt(colorStr.substring(2, 4), 16);
            int b = Integer.parseInt(colorStr.substring(4, 6), 16);
            return new Color(r / 255f, g / 255f, b / 255f, 1f);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    private float parseSize(String size, float containerSize) {
        if (size.endsWith("%")) {
            float percent = Float.parseFloat(size.substring(0, size.length() - 1));
            return containerSize * percent / 100f;
        }
        return Float.parseFloat(size);
    }

    public void update(float delta) {
        blinkTimer += delta;
    }
    
    
    public LayoutBox findInputAt(float x, float y) {
        if (currentLayoutRoot == null) return null;
        System.out.println("[Renderer] findInputAt(" + x + ", " + y + ")");
        return findInputInBox(currentLayoutRoot, x, y);
    }
    
    private LayoutBox findInputInBox(LayoutBox box, float x, float y) {
        if (box.type == LayoutBox.Type.INPUT) {
            boolean contains = x >= box.x && x <= box.x + box.width &&
                y >= box.y - box.height && y <= box.y;
            System.out.println("[Renderer]   Checking INPUT at (" + box.x + ", " + box.y + ") size=" + box.width + "x" + box.height + " contains=" + contains);
            if (contains) {
                return box;
            }
        }
        
        for (LayoutBox child : box.children) {
            LayoutBox found = findInputInBox(child, x, y);
            if (found != null) return found;
        }
        
        return null;
    }
    
    public LayoutBox findButtonAt(float x, float y) {
        if (currentLayoutRoot == null) return null;
        return findButtonInBox(currentLayoutRoot, x, y);
    }
    
    private LayoutBox findButtonInBox(LayoutBox box, float x, float y) {
        if (box.type == LayoutBox.Type.BUTTON) {
            if (x >= box.x && x <= box.x + box.width &&
                y >= box.y - box.height && y <= box.y) {
                return box;
            }
        }
        
        for (LayoutBox child : box.children) {
            LayoutBox found = findButtonInBox(child, x, y);
            if (found != null) return found;
        }
        
        return null;
    }
    
    public void setFocusedInput(LayoutBox input) {
        if (focusedInput != null) {
            focusedInput.focused = false;
        }
        focusedInput = input;
        if (focusedInput != null) {
            focusedInput.focused = true;
        }
    }
    
    public LayoutBox getFocusedInput() {
        return focusedInput;
    }
    
    public void handleKeyInput(char character) {
        if (focusedInput == null) return;
        
        if (focusedInput.inputValue.length() < 512) {
            String before = focusedInput.inputValue.substring(0, focusedInput.cursorPosition);
            String after = focusedInput.inputValue.substring(focusedInput.cursorPosition);
            focusedInput.inputValue = before + character + after;
            focusedInput.cursorPosition++;
        }
    }
    
    public void handleBackspace() {
        if (focusedInput == null || focusedInput.cursorPosition == 0) return;
        
        String before = focusedInput.inputValue.substring(0, focusedInput.cursorPosition - 1);
        String after = focusedInput.inputValue.substring(focusedInput.cursorPosition);
        focusedInput.inputValue = before + after;
        focusedInput.cursorPosition--;
    }
    
    public void handleLeftArrow() {
        if (focusedInput == null) return;
        if (focusedInput.cursorPosition > 0) {
            focusedInput.cursorPosition--;
        }
    }
    
    public void handleRightArrow() {
        if (focusedInput == null) return;
        if (focusedInput.cursorPosition < focusedInput.inputValue.length()) {
            focusedInput.cursorPosition++;
        }
    }
    
    public LayoutBox findFormForButton(LayoutBox button) {
        LayoutBox current = button.parent;
        while (current != null) {
            if (current.type == LayoutBox.Type.FORM) {
                return current;
            }
            current = current.parent;
        }
        return null;
    }
    
    public Map<String, String> collectFormData(LayoutBox form) {
        Map<String, String> data = new HashMap<>();
        collectFormDataRecursive(form, data);
        return data;
    }
    
    public void handleButtonClick(LayoutBox button) {
        if (button == null || button.type != LayoutBox.Type.BUTTON) return;
        
        String buttonType = button.buttonType;
        Gdx.app.log("Button", "Clicked: type=" + buttonType + ", text=" + button.buttonText);
        
        if ("submit".equals(buttonType)) {
            LayoutBox form = findFormForButton(button);
            if (form != null && form.formName != null) {
                Map<String, String> formData = collectFormData(form);
                Gdx.app.log("Form", "SUBMIT: form=" + form.formName + ", data=" + formData);
                
            }
        } else if (buttonType != null && buttonType.startsWith("onclick:")) {
            String functionName = buttonType.substring(8); // Remove "onclick:" prefix
            Gdx.app.log("Button", "ONCLICK: function=" + functionName);
            
        }
    }
    
    private void collectFormDataRecursive(LayoutBox box, Map<String, String> data) {
        if (box.type == LayoutBox.Type.INPUT && box.inputName != null && !box.inputName.isEmpty()) {
            data.put(box.inputName, box.inputValue);
        }
        
        for (LayoutBox child : box.children) {
            collectFormDataRecursive(child, data);
        }
    }

    public void dispose() {
        for (Texture texture : imageCache.values()) {
            texture.dispose();
        }
        imageCache.clear();
    }


    private static class LayoutContext {
        float containerX;
        float containerY;
        float containerWidth;
        float currentX;
        float currentY;

        Color defaultTextColor = Color.WHITE;
        Color currentColor = Color.WHITE;

        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        float fontScale = 1.0f;

        int listDepth = 0;
        boolean isOrderedList = false;
        int listCounter = 1;

        LayoutContext(float x, float y, float width) {
            this.containerX = x;
            this.containerY = y;
            this.containerWidth = width;
            this.currentX = x;
            this.currentY = y;
            this.currentColor = defaultTextColor;
        }

        LayoutContext createChild() {
            LayoutContext child = new LayoutContext(containerX, currentY, containerWidth);
            child.lineHeight = this.lineHeight;
            child.defaultTextColor = this.defaultTextColor;
            child.currentColor = this.currentColor;
            child.bold = this.bold;
            child.italic = this.italic;
            child.underline = this.underline;
            child.fontScale = this.fontScale;
            child.listDepth = this.listDepth;
            child.isOrderedList = this.isOrderedList;
            child.listCounter = this.listCounter;
            return child;
        }

        void newLine() {
            if (currentX > containerX) {
                currentY -= lineHeight;
                currentX = containerX;
            }
        }

        float lineHeight = 20; // Will be set from renderer
    }
}

