package com.astro.minor.wired.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeParser {
    private static final Pattern ATTRIBUTE_PATTERN =
        Pattern.compile("(\\w+)=\"([^\"]*)\"");

    public static Map<String, String> parseAttributes(String attributesText) {
        Map<String, String> attributes = new HashMap<>();
        if (attributesText == null || attributesText.trim().isEmpty()) {
            return attributes;
        }

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributesText);
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            attributes.put(name, value);
        }

        return attributes;
    }
}

