package com.astro.minor.lainAI;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class KoboldCppProxy {
    private final String baseUrl;
    private final AtomicBoolean isHealthy = new AtomicBoolean(false);
    private static final int TIMEOUT = 30000;

    public KoboldCppProxy(String host, int port) {
        this.baseUrl = "http://" + host + ":" + port;
    }

    public CompletableFuture<Boolean> checkHealth() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        Net.HttpRequest request = new Net.HttpRequest(Net.HttpMethods.GET);
        request.setUrl(baseUrl + "/api/v1/model");
        request.setTimeOut(5000);

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                boolean healthy = httpResponse.getStatus().getStatusCode() == 200;
                isHealthy.set(healthy);
                future.complete(healthy);
            }

            @Override
            public void failed(Throwable t) {
                isHealthy.set(false);
                future.complete(false);
            }

            @Override
            public void cancelled() {
                isHealthy.set(false);
                future.complete(false);
            }
        });

        return future;
    }

    public CompletableFuture<String> generateText(String prompt, LainAIConfig config) {
        CompletableFuture<String> future = new CompletableFuture<>();

        String jsonPayload = buildJsonPayload(prompt, config);
        Gdx.app.log("KoboldCpp", "Sending request with payload length: " + jsonPayload.length());

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest request = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(baseUrl + "/api/v1/generate")
            .header("Content-Type", "application/json; charset=UTF-8")
            .header("Accept", "application/json; charset=UTF-8")
            .content(jsonPayload)
            .timeout(TIMEOUT)
            .build();

        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    if (httpResponse.getStatus().getStatusCode() == 200) {
                        byte[] resultBytes = httpResponse.getResult();
                        String responseText = new String(resultBytes, java.nio.charset.StandardCharsets.UTF_8);
                        Gdx.app.log("KoboldCpp", "Response received, length: " + responseText.length());
                        String generatedText = parseGeneratedText(responseText);
                        Gdx.app.log("KoboldCpp", "Parsed text length: " + generatedText.length());
                        future.complete(generatedText);
                    } else {
                        Gdx.app.error("KoboldCpp", "HTTP Error: " + httpResponse.getStatus().getStatusCode());
                        future.completeExceptionally(
                            new RuntimeException("HTTP Error: " + httpResponse.getStatus().getStatusCode())
                        );
                    }
                } catch (Exception e) {
                    Gdx.app.error("KoboldCpp", "Exception in response handler", e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("KoboldCpp", "Request failed", t);
                future.completeExceptionally(t);
            }

            @Override
            public void cancelled() {
                Gdx.app.error("KoboldCpp", "Request cancelled");
                future.completeExceptionally(new RuntimeException("Request cancelled"));
            }
        });

        return future;
    }

    private String buildJsonPayload(String prompt, LainAIConfig config) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"prompt\":\"").append(escapeJson(prompt)).append("\",");
        json.append("\"max_length\":").append(config.getMaxTokens()).append(",");
        json.append("\"temperature\":").append(config.getTemperature()).append(",");
        json.append("\"top_p\":").append(config.getTopP()).append(",");
        json.append("\"top_k\":").append(config.getTopK()).append(",");
        json.append("\"rep_pen\":").append(config.getRepeatPenalty());
        json.append("}");
        return json.toString();
    }

    private String parseGeneratedText(String jsonResponse) {
        int resultsStart = jsonResponse.indexOf("\"results\"");
        if (resultsStart == -1) {
            return "";
        }

        int textStart = jsonResponse.indexOf("\"text\"", resultsStart);
        if (textStart == -1) {
            return "";
        }

        int valueStart = jsonResponse.indexOf("\"", textStart + 6) + 1;
        
        int valueEnd = valueStart;
        boolean inEscape = false;
        for (int i = valueStart; i < jsonResponse.length(); i++) {
            char c = jsonResponse.charAt(i);
            if (inEscape) {
                inEscape = false;
                continue;
            }
            if (c == '\\') {
                inEscape = true;
                continue;
            }
            if (c == '"') {
                valueEnd = i;
                break;
            }
        }
        
        if (valueStart > 0 && valueEnd > valueStart) {
            String text = jsonResponse.substring(valueStart, valueEnd);
            return unescapeJson(text);
        }

        return "";
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String unescapeJson(String str) {
        if (str == null) return "";
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            char c = str.charAt(i);
            if (c == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        i += 2;
                        break;
                    case 'r':
                        result.append('\r');
                        i += 2;
                        break;
                    case 't':
                        result.append('\t');
                        i += 2;
                        break;
                    case '"':
                        result.append('"');
                        i += 2;
                        break;
                    case '\\':
                        result.append('\\');
                        i += 2;
                        break;
                    case 'u':
                        if (i + 5 < str.length()) {
                            try {
                                String hex = str.substring(i + 2, i + 6);
                                int codePoint = Integer.parseInt(hex, 16);
                                result.append((char) codePoint);
                                i += 6;
                            } catch (NumberFormatException e) {
                                result.append(c);
                                i++;
                            }
                        } else {
                            result.append(c);
                            i++;
                        }
                        break;
                    default:
                        result.append(c);
                        i++;
                        break;
                }
            } else {
                result.append(c);
                i++;
            }
        }
        return result.toString();
    }

    public boolean isHealthy() {
        return isHealthy.get();
    }
}
