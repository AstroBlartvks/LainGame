package com.astro.minor.lainAI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LainChatService {
    private final KoboldCppProxy proxy;
    private final LainAIConfig config;
    private final List<ConversationMessage> history;
    private static final int MAX_HISTORY_SIZE = 5;
    private static LainChatService instance;

    private LainChatService(KoboldCppProxy proxy) {
        this.proxy = proxy;
        this.config = new LainAIConfig();
        this.history = new ArrayList<>();
    }

    public static synchronized LainChatService getInstance() {
        if (instance == null) {
            KoboldCppProxy proxy = new KoboldCppProxy("localhost", 5001);
            instance = new LainChatService(proxy);
        }
        return instance;
    }

    public CompletableFuture<String> askLain(String userMessage) {
        String formattedPrompt = formatPrompt(userMessage);
        
        return proxy.generateText(formattedPrompt, config)
            .thenApply(response -> {
                String cleanResponse = cleanResponse(response);
                addToHistory(userMessage, cleanResponse);
                return cleanResponse;
            });
    }

    private String formatPrompt(String userInput) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("### System Instruction:\n");
        prompt.append(config.getSystemPrompt());
        prompt.append("\n\n");
        
        for (ConversationMessage msg : history) {
            if (msg.isUser()) {
                prompt.append("Human: ").append(msg.getContent()).append("\n");
            } else {
                prompt.append("Assistant: ").append(msg.getContent()).append("\n");
            }
        }
        
        prompt.append("Human: ").append(userInput).append("\n");
        prompt.append("Assistant: ");
        
        return prompt.toString();
    }

    private String cleanResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "...";
        }
        
        String cleaned = response.trim();
        
        if (cleaned.startsWith("Assistant:")) {
            cleaned = cleaned.substring("Assistant:".length()).trim();
        }
        
        if (cleaned.contains("Human:")) {
            int humanIndex = cleaned.indexOf("Human:");
            cleaned = cleaned.substring(0, humanIndex).trim();
        }
        
        if (cleaned.isEmpty()) {
            cleaned = "...";
        }
        
        return cleaned;
    }

    private void addToHistory(String userMessage, String assistantResponse) {
        history.add(new ConversationMessage(true, userMessage));
        history.add(new ConversationMessage(false, assistantResponse));
        
        while (history.size() > MAX_HISTORY_SIZE * 2) {
            history.remove(0);
        }
    }

    public void clearHistory() {
        history.clear();
    }

    public boolean isProxyHealthy() {
        return proxy.isHealthy();
    }

    public CompletableFuture<Boolean> checkProxyHealth() {
        return proxy.checkHealth();
    }

    public LainAIConfig getConfig() {
        return config;
    }

    private static class ConversationMessage {
        private final boolean isUser;
        private final String content;

        public ConversationMessage(boolean isUser, String content) {
            this.isUser = isUser;
            this.content = content;
        }

        public boolean isUser() {
            return isUser;
        }

        public String getContent() {
            return content;
        }
    }
}
