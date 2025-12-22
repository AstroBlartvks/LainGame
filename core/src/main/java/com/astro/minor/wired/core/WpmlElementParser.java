package com.astro.minor.wired.core;

import com.astro.minor.wired.tags.WiredPageMLElement;

public class WpmlElementParser implements ElementParser {

    @Override
    public WiredPageMLElement parse(String content, ParseContext context) {
        WiredPageMLElement element = new WiredPageMLElement();

        if (content != null && !content.trim().isEmpty()) {
            element.setTextContent(content.trim());
        }

        return element;
    }

    @Override
    public boolean supports(String tagName) {
        return "wpml".equals(tagName);
    }
}

