package com.example.datasetapi.service.ai;

import com.example.datasetapi.dto.response.AnalyticsSummaryDto;
import com.example.datasetapi.mapper.AiTextMapper;
import com.example.datasetapi.model.dataset.AiPromptRecord;
import com.example.datasetapi.model.dataset.DatasetAnalysis;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.repository.AiPromptRecordRepository;
import com.example.datasetapi.repository.DatasetAnalysisRepository;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.service.ChatService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AiAnalyticsMapperService {

    private final DatasetInforRepository datasetInforRepository;
    private final DatasetAnalysisRepository datasetAnalysisRepository;
    private final AiPromptRecordRepository aiPromptRecordRepository;
    private final ChatService chatService;
    private final AiTextMapper aiTextMapper;

    public AiAnalyticsMapperService(
            DatasetInforRepository datasetInforRepository,
            DatasetAnalysisRepository datasetAnalysisRepository,
            AiPromptRecordRepository aiPromptRecordRepository,
            ChatService chatService,
            AiTextMapper aiTextMapper
    ) {
        this.datasetInforRepository = datasetInforRepository;
        this.datasetAnalysisRepository = datasetAnalysisRepository;
        this.aiPromptRecordRepository = aiPromptRecordRepository;
        this.chatService = chatService;
        this.aiTextMapper = aiTextMapper;
    }

    /**
     * Không build prompt. Lấy prompt đã lưu trong DB -> gọi LLM -> map DTO.
     */
    public AnalyticsSummaryDto datasetAnalistByAi(long datasetId) {
        // 1) Lấy dataset + analysis + prompt record
        DatasetInformation ds = datasetInforRepository.findByDatasetId(datasetId);
        if (ds == null) throw new IllegalArgumentException("Dataset không tồn tại: " + datasetId);

        DatasetAnalysis analysis = datasetAnalysisRepository.findByDatasetInformation(ds);
        if (analysis == null) throw new IllegalArgumentException("Chưa có bản ghi DatasetAnalysis cho datasetId=" + datasetId);

        AiPromptRecord promptRecord = aiPromptRecordRepository.findByDatasetAnalysis(analysis);
        if (promptRecord == null || promptRecord.getPromptText() == null || promptRecord.getPromptText().isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy prompt đã lưu trong DB cho datasetId=" + datasetId);
        }

        String prompt = promptRecord.getPromptText();

        // 2) Gọi LLM bằng prompt đã lưu
        ChatResponse resp = chatService.response(prompt);

        // 3) Bóc assistant text và parse sang DTO (charts, alerts, notes)
        AnalyticsSummaryDto dto = aiTextMapper.toDto(resp);

        // 4) (Tùy chọn) Lưu lại raw AI response và timestamp, nếu entity có field tương ứng
        try {
            String assistantText = aiTextMapper.extractAssistantText(resp); // raw text (có thể chứa code fence)
            if (assistantText != null) {
                // Nếu AiPromptRecord có các field này, bỏ comment để lưu:
                // promptRecord.setLastAiRawResponse(assistantText.length() > 20000 ? assistantText.substring(0, 20000) : assistantText);
                // promptRecord.setLastRunAt(LocalDateTime.now());
                // aiPromptRecordRepository.save(promptRecord);
            }
        } catch (Exception ignored) {}

        return dto;
    }
}
