package com.astro.minor.wired.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WiredElement {
    protected String tagName;
    protected Map<String, String> attributes;
    protected List<WiredElement> children;
    protected String textContent;
    protected WiredElement parent;

    public WiredElement() {
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.textContent = "";
    }

    public WiredElement(String tagName) {
        this();
        this.tagName = tagName;
    }

    public void addChild(WiredElement child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addAttribute(String name, String value) {
        this.attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String getAttribute(String name, String defaultValue) {
        return this.attributes.getOrDefault(name, defaultValue);
    }

    public boolean hasAttribute(String name) {
        return this.attributes.containsKey(name);
    }

    public List<WiredElement> findElementsByTag(String tagName) {
        List<WiredElement> results = new ArrayList<>();
        if (this.tagName.equals(tagName)) {
            results.add(this);
        }
        for (WiredElement child : children) {
            results.addAll(child.findElementsByTag(tagName));
        }
        return results;
    }

    public boolean isValid() {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    public int getChildCount()  {
        return this.children.size();
    }

    public String getTagName() { return tagName; }
    public void setTagName(String tagName) { this.tagName = tagName; }

    public Map<String, String> getAttributes() { return attributes; }
    public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }

    public List<WiredElement> getChildren() { return children; }
    public void setChildren(List<WiredElement> children) { this.children = children; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public WiredElement getParent() { return parent; }
    public void setParent(WiredElement parent) { this.parent = parent; }

    @Override
    public String toString() {
        return String.format("WiredElement{tag='%s', attributes=%s, children=%d, text='%s'}",
            tagName, attributes, children.size(),
            textContent.length() > 50 ? textContent.substring(0, 50) + "..." : textContent);
    }
}

