package com.astro.minor.wired.core;

import com.astro.minor.wired.elements.WiredElement;
import com.astro.minor.wired.tags.WiredPageMLElement;

public interface ElementParser {
    WiredPageMLElement parse(String content, ParseContext context);
    boolean supports(String tagName);
}

