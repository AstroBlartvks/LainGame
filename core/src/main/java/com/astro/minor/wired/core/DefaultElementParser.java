package com.astro.minor.wired.core;

import com.astro.minor.wired.tags.WiredPageMLElement;

public class DefaultElementParser implements ElementParser {

    @Override
    public WiredPageMLElement parse(String content, ParseContext context) {
        WiredPageMLElement element = new WiredPageMLElement();
        element.setTextContent(content != null ? content.trim() : "");
        return element;
    }

    @Override
    public boolean supports(String tagName) {
        return true;
    }
}

