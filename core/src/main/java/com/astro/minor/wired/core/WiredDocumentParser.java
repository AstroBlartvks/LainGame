package com.astro.minor.wired.core;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiredDocumentParser {
    private static final Pattern METADATA_PATTERN =
        Pattern.compile("^@(\\w+)\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern COMMENT_PATTERN =
        Pattern.compile("//[^\\n]*", Pattern.MULTILINE);
    private static final Pattern TAG_PATTERN =
        Pattern.compile("\\[(/?)(\\w+)([^\\]]*?)\\]");

    private static final Set<String> SELF_CLOSING_TAGS = Set.of("br", "hr", "img", "input");

    private final ParserFactory parserFactory;

    public WiredDocumentParser() {
        this.parserFactory = new ParserFactory();
    }

    public WiredDocument parse(String document) {
        ParseContext context = new ParseContext();

        String contentWithoutMetadata = parseMetadata(document, context);

        contentWithoutMetadata = removeComments(contentWithoutMetadata);

        parseTags(contentWithoutMetadata, context);

        return new WiredDocument(context.getMetadata(), context.getRootElements(), document);
    }

    private String parseMetadata(String document, ParseContext context) {
        StringBuilder contentBuilder = new StringBuilder();
        String[] lines = document.split("\n", -1);  // Keep empty lines

        for (String line : lines) {
            Matcher matcher = METADATA_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                String key = matcher.group(1);
                String value = matcher.group(2).trim();
                context.addMetadata(key, value);
            } else {
                contentBuilder.append(line).append("\n");
            }
        }

        return contentBuilder.toString();
    }

    private String removeComments(String content) {
        return COMMENT_PATTERN.matcher(content).replaceAll("");
    }

    private void parseTags(String content, ParseContext context) {
        Matcher matcher = TAG_PATTERN.matcher(content);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textBefore = content.substring(lastEnd, matcher.start());
                addTextContent(textBefore, context);
            }

            boolean isClosing = !matcher.group(1).isEmpty();
            String tagName = matcher.group(2).toLowerCase();
            String attributesText = matcher.group(3);

            if (isClosing) {
                handleClosingTag(tagName, context);
            } else {
                handleOpeningTag(tagName, attributesText, context);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < content.length()) {
            String textAfter = content.substring(lastEnd);
            addTextContent(textAfter, context);
        }
    }

    private void handleOpeningTag(String tagName, String attributesText, ParseContext context) {
        Map<String, String> attributes = AttributeParser.parseAttributes(attributesText);

        WiredElement element = new WiredElement(tagName);
        element.setAttributes(attributes);

        if (!context.getElementStack().isEmpty()) {
            context.getElementStack().peek().addChild(element);
        } else {
            context.getRootElements().add(element);
        }

        if (!SELF_CLOSING_TAGS.contains(tagName)) {
            context.getElementStack().push(element);
        }
    }

    private void handleClosingTag(String tagName, ParseContext context) {
        if (!context.getElementStack().isEmpty()) {
            WiredElement current = context.getElementStack().peek();
            if (current.getTagName().equals(tagName)) {
                context.getElementStack().pop();
            }
        }
    }

    private void addTextContent(String text, ParseContext context) {
        text = text.replaceAll("\\n+", " ");


        if (!text.isEmpty() && !text.matches("^\\s*$")) {
            if (!context.getElementStack().isEmpty()) {
                WiredElement textNode = new WiredElement("#text");
                textNode.setTextContent(text);
                context.getElementStack().peek().addChild(textNode);
            } else {
                WiredElement textNode = new WiredElement("#text");
                textNode.setTextContent(text);
                context.getRootElements().add(textNode);
            }
        }
    }
}

