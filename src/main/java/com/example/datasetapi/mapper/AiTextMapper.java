package com.example.datasetapi.mapper;

import com.example.datasetapi.dto.response.AnalyticsSummaryDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiTextMapper {

    private final ObjectMapper mapper = new ObjectMapper();

    // ```json ... ``` hoặc ``` ... ```
    private static final Pattern FENCE = Pattern.compile(
            "```(?:json)?\\s*(\\{[\\s\\S]*?\\})\\s*```",
            Pattern.CASE_INSENSITIVE
    );

    /** Lấy assistant text từ ChatResponse (Spring AI) */
    public String extractAssistantText(ChatResponse resp) {
        if (resp == null) return null;

        // API Spring AI thường có getResult().getOutput().getContent()
        try {
            var result = resp.getResult();
            if (result != null && result.getOutput() != null
                    && result.getOutput().getText() != null) {
                return result.getOutput().getText();
            }
        } catch (Throwable ignored) {}

        // Fallback: lấy phần tử đầu của results
        try {
            var results = resp.getResults();
            if (results != null && !results.isEmpty()
                    && results.get(0).getOutput() != null
                    && results.get(0).getOutput().getText() != null) {
                return results.get(0).getOutput().getText();
            }
        } catch (Throwable ignored) {}

        // Fallback cuối: serialize sang JSON string rồi tìm "text"
        try {
            String json = mapper.writeValueAsString(resp);
            JsonNode root = mapper.readTree(json);
            JsonNode text = root.at("/result/output/content");
            if (text.isTextual()) return text.asText();
            JsonNode t2 = root.at("/results/0/output/content");
            if (t2.isTextual()) return t2.asText();
        } catch (Throwable ignored) {}

        return null;
    }

    /** Gỡ code fence và cắt block JSON chính */
    public String unwrapToJsonString(String assistantText) {
        if (assistantText == null) return null;
        String t = assistantText.trim();

        Matcher m = FENCE.matcher(t);
        if (m.find()) return m.group(1).trim();

        int first = t.indexOf('{');
        int last  = t.lastIndexOf('}');
        if (first >= 0 && last > first) return t.substring(first, last + 1).trim();

        return t; // có thể đã là JSON thuần
    }

    /** Parse thẳng từ ChatResponse -> DTO */
    public AnalyticsSummaryDto toDto(ChatResponse resp) {
        String text = extractAssistantText(resp);
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy assistant text trong ChatResponse");
        }
        String json = unwrapToJsonString(text);
        try {
            return mapper.readValue(json, AnalyticsSummaryDto.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("JSON assistant không hợp lệ: " + ex.getMessage(), ex);
        }
    }
}