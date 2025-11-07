package com.example.datasetapi.service.ai;

import com.example.datasetapi.dto.response.AnalyticsSummaryDto;
import com.example.datasetapi.dto.service.ChartDto;
import com.example.datasetapi.dto.service.ChartDto;
import com.example.datasetapi.mapper.AiTextMapper;
import com.example.datasetapi.model.dataset.AiPromptRecord;
import com.example.datasetapi.model.dataset.DatasetAnalysis;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.repository.AiPromptRecordRepository;
import com.example.datasetapi.repository.DatasetAnalysisRepository;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiAnalyticsMapperService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalyticsMapperService.class);

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
     * KHÔNG build prompt mới.
     * Lấy prompt đã lưu trong DB -> gọi LLM -> map sang DTO.
     * Chỉ bổ sung chống-null an toàn cho DTO để UI không lỗi (không đổi logic nghiệp vụ).
     */
    public AnalyticsSummaryDto datasetAnalistByAi(long datasetId) {
        // 1) Lấy dataset + analysis + prompt record
        DatasetInformation ds = datasetInforRepository.findByDatasetId(datasetId);
        if (ds == null) {
            throw new IllegalArgumentException("Dataset không tồn tại: " + datasetId);
        }

        DatasetAnalysis analysis = datasetAnalysisRepository.findByDatasetInformation(ds);
        if (analysis == null) {
            throw new IllegalArgumentException("Chưa có bản ghi DatasetAnalysis cho datasetId=" + datasetId);
        }

        AiPromptRecord promptRecord = aiPromptRecordRepository.findByDatasetAnalysis(analysis);
        if (promptRecord == null || isBlank(promptRecord.getPromptText())) {
            throw new IllegalArgumentException("Không tìm thấy prompt đã lưu trong DB cho datasetId=" + datasetId);
        }

        final String prompt = promptRecord.getPromptText();
        log.debug("AI analysis prompt loaded for datasetId={}: {} chars", datasetId, prompt.length());

        // 2) Gọi LLM bằng prompt đã lưu
        ChatResponse resp = chatService.response(prompt);

        // 3) Parse assistant text -> DTO
        AnalyticsSummaryDto dto = null;
        try {
            dto = aiTextMapper.toDto(resp);
        } catch (Exception ex) {
            log.warn("Mapper toDto failed for datasetId={}, fallback to empty DTO. Cause: {}", datasetId, ex.toString());
        }
        if (dto == null) {
            dto = new AnalyticsSummaryDto();
        }

        // 3.1) Chống null: đảm bảo các list tồn tại để FE render an toàn
        if (dto.getCharts() == null) dto.setCharts(new ArrayList<>());
        if (dto.getAlerts() == null) dto.setAlerts(new ArrayList<>());
        if (dto.getNotes() == null) dto.setNotes("");

        // 3.2) Chống null cho data trong từng chart (FE cần data là array)
        ensureChartDataArrays(dto.getCharts());

        // 4) (Tuỳ chọn) Lưu lại raw AI response và timestamp (nếu entity có field)
        try {
            String assistantText = aiTextMapper.extractAssistantText(resp); // raw text (có thể có code fence)
            if (!isBlank(assistantText)) {
                // Nếu có các field tương ứng trong AiPromptRecord, bỏ comment để lưu:
                // promptRecord.setLastAiRawResponse(assistantText.length() > 20000 ? assistantText.substring(0, 20000) : assistantText);
                // promptRecord.setLastRunAt(LocalDateTime.now());
                // aiPromptRecordRepository.save(promptRecord);
            }
        } catch (Exception ignore) { /* no-op */ }

        return dto;
    }

    /* ------------------------------- helpers ------------------------------- */

    private void ensureChartDataArrays(List<ChartDto> charts) {
        if (charts == null) return;
        for (ChartDto c : charts) {
            if (c == null) continue;
            if (c.getData() == null) {
                c.setData(new ArrayList<>()); // không tự bơm dữ liệu, chỉ đảm bảo mảng trống
            }
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
