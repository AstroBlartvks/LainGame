package com.astro.minor.wired.core;

import java.util.*;

public class WiredDocument {
    private final Map<String, String> metadata;
    private final List<WiredElement> rootElements;
    private final String originalSource;

    public WiredDocument(Map<String, String> metadata, List<WiredElement> rootElements) {
        this(metadata, rootElements, "");
    }

    public WiredDocument(Map<String, String> metadata, List<WiredElement> rootElements, String originalSource) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.rootElements = rootElements != null ? List.copyOf(rootElements) : Collections.emptyList();
        this.originalSource = originalSource != null ? originalSource : "";
    }

    public List<WiredElement> findElementsByTag(String tagName) {
        List<WiredElement> results = new ArrayList<>();
        for (WiredElement root : rootElements) {
            results.addAll(root.findElementsByTag(tagName));
        }
        return results;
    }

    public String getTitle() {
        return metadata.get("title");
    }

    public String getBackgroundColor() {
        return metadata.get("bg");
    }

    public String getTextColor() {
        return metadata.get("text");
    }

    public String getLinkColor() {
        return metadata.get("link");
    }

    public boolean isValid() {
        if (rootElements.isEmpty()) {
            return false;
        }

        boolean hasWpmlRoot = rootElements.stream()
            .anyMatch(element -> "wpml".equals(element.getTagName()));

        return hasWpmlRoot;
    }

    public String getStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append("Wired Document Structure:\n");
        sb.append("Metadata: ").append(metadata).append("\n");
        sb.append("Root elements: ").append(rootElements.size()).append("\n");

        for (WiredElement root : rootElements) {
            appendElementStructure(sb, root, 0);
        }

        return sb.toString();
    }

    private void appendElementStructure(StringBuilder sb, WiredElement element, int depth) {
        String indent = "  ".repeat(depth);
        sb.append(indent)
            .append("<").append(element.getTagName());

        if (!element.getAttributes().isEmpty()) {
            element.getAttributes().forEach((key, value) ->
                sb.append(" ").append(key).append("=\"").append(value).append("\""));
        }
        sb.append(">");

        if (!element.getTextContent().trim().isEmpty()) {
            String text = element.getTextContent().trim();
            if (text.length() > 30) {
                text = text.substring(0, 30) + "...";
            }
            sb.append(" \"").append(text).append("\"");
        }

        sb.append("\n");

        for (WiredElement child : element.getChildren()) {
            appendElementStructure(sb, child, depth + 1);
        }

        sb.append(indent).append("</").append(element.getTagName()).append(">\n");
    }

    public Map<String, String> getMetadata() { return Collections.unmodifiableMap(metadata); }
    public List<WiredElement> getRootElements() { return Collections.unmodifiableList(rootElements); }
    public String getOriginalSource() { return originalSource; }

    @Override
    public String toString() {
        return String.format("WiredDocument{metadata=%s, rootElements=%d, valid=%s}",
            metadata, rootElements.size(), isValid());
    }
}

