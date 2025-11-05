package com.example.datasetapi.service.feature;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * AiService dùng Spring AI ChatClient (ví dụ Gemini).
 * Gọi đồng bộ, trả kết quả JSON nếu hợp lệ.
 */
@Service
public class AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final Duration llmTimeout;

    @Autowired
    public AiService(ChatClient.Builder chatClient,
                     ObjectMapper objectMapper,
                     @Value("${ai.enabled:false}") boolean enabled,
                     @Value("${ai.timeout.seconds:30}") long timeoutSeconds) {
        this.chatClient = chatClient.build();
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.llmTimeout = Duration.ofSeconds(timeoutSeconds);
    }

    public AiResult callAnalysis(String prompt, String datasetType) {
        if (!enabled) {
            System.out.println("[AiService] AI disabled — printing prompt only:");
            System.out.println("==== PROMPT START ====");
            System.out.println(prompt);
            System.out.println("==== PROMPT END ====");
            return new AiResult(prompt, null, null);
        }

        String rawResponse = null;
        JsonNode jsonResponse = null;

        try {
            System.out.println("[AiService] Calling Gemini for dataset type=" + datasetType + " ...");

            ChatResponse response = chatClient.prompt()
                    .user(prompt)
                    .call().
                    chatResponse();

            if (response == null || response.getResult() == null) {
                System.err.println("[AiService] Empty response from Gemini.");
                return new AiResult(prompt, null, null);
            }

            // Lấy text kết quả
            rawResponse = response.getResult().getOutput().getText();

            if (rawResponse == null || rawResponse.isBlank()) {
                System.err.println("[AiService] Gemini returned blank text.");
                return new AiResult(prompt, null, null);
            }

            // Parse JSON nếu có thể
            try {
                jsonResponse = objectMapper.readTree(rawResponse);
            } catch (Exception e) {
                System.err.println("[AiService] Response is not valid JSON — will store raw text only.");
            }

        } catch (Exception e) {
            System.err.println("[AiService] Gemini call failed: " + e.getMessage());
        }

        return new AiResult(prompt, rawResponse, jsonResponse);
    }

    public static class AiResult {
        private final String prompt;
        private final String rawResponse;
        private final JsonNode jsonResponse;

        public AiResult(String prompt, String rawResponse, JsonNode jsonResponse) {
            this.prompt = prompt;
            this.rawResponse = rawResponse;
            this.jsonResponse = jsonResponse;
        }

        public String getPrompt() { return prompt; }
        public String getRawResponse() { return rawResponse; }
        public JsonNode getJsonResponse() { return jsonResponse; }
    }
}
