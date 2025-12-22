package com.astro.minor.wired.tags;

import com.astro.minor.wired.elements.WiredElement;

import java.util.ArrayList;
import java.util.List;

public class WiredPageMLElement extends WiredElement {

    public WiredPageMLElement() {
        super("wpml");
    }

    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        for (WiredElement child : children) {
            String childTag = child.getTagName();
            if (childTag == null || childTag.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public String getAlign() {
        return getAttribute("align");
    }

    public void setAlign(String align) {
        addAttribute("align", align);
    }

    public List<WiredElement> getHeadings() {
        List<WiredElement> headings = new ArrayList<>();
        for (WiredElement child : children) {
            if (child.getTagName().matches("h[1-3]")) {
                headings.add(child);
            }
            headings.addAll(child.findElementsByTag("h[1-3]"));
        }
        return headings;
    }

    public List<WiredElement> getImages() {
        return findElementsByTag("img");
    }
}

