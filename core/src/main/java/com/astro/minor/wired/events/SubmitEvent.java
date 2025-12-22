package com.astro.minor.wired.events;

import java.util.Map;


public class SubmitEvent extends WiredPageEvent {
    private final String formName;
    private final Map<String, String> formData;

    public SubmitEvent(String formName, Map<String, String> formData) {
        super("submit");
        this.formName = formName;
        this.formData = formData;
    }

    public String getFormName() {
        return formName;
    }

    public Map<String, String> getFormData() {
        return formData;
    }

    @Override
    public void handle() {
        System.out.println("[SubmitEvent] Form '" + formName + "' submitted with data:");
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }
    }
}

