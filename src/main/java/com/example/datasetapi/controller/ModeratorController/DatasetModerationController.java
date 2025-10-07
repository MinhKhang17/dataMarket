package com.example.datasetapi.controller.ModeratorController;


import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.model.Dataset.DatasetInformation;
import com.example.datasetapi.repository.DatasetInforRepository;
import com.example.datasetapi.repository.DatasetValidationErrorRepository;
import com.example.datasetapi.model.Dataset.DatasetValidationError;
import com.example.datasetapi.service.dataset.DatasetValidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class DatasetModerationController {

    private final DatasetInforRepository datasetInforRepository;
    private final DatasetValidationErrorRepository datasetValidationErrorRepository;
    private final DatasetValidateService datasetValidateService;

    //Lấy danh sách tất cả dataset để moderator xem
//    @GetMapping("/datasets")
//    public ResponseEntity<?> getAllDatasets() {
//        List<DatasetInformation> all = datasetInforRepository.findAll();
//        return ResponseEntity.ok(new ApiResponse(true, "Danh sách dataset", all));
//    }

    //Lấy chi tiết 1 dataset theo ID (bao gồm thông tin cơ bản)
//    @GetMapping("/datasets/{id}")
//    public ResponseEntity<?> getDatasetById(@PathVariable Long id) {
//        DatasetInformation ds = datasetInforRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dataset với id: " + id));
//        return ResponseEntity.ok(new ApiResponse(true, "Chi tiết dataset", ds));
//    }

    //Lấy danh sách lỗi kiểm duyệt của 1 dataset
//    @GetMapping("/datasets/{id}/errors")
//    public ResponseEntity<?> getDatasetErrors(@PathVariable Long id) {
//        DatasetInformation ds = datasetInforRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dataset với id: " + id));
//
//        List<DatasetValidationError> errors = datasetValidationErrorRepository.findByDatasetInformation(ds);
//
//        if (errors.isEmpty()) {
//            return ResponseEntity.ok(new ApiResponse(true, "Không có lỗi kiểm duyệt nào", List.of()));
//        }
//        return ResponseEntity.ok(new ApiResponse(true, "Danh sách lỗi kiểm duyệt", errors));
//    }


     //Lọc dataset theo trạng thái kiểm duyệt
     //Ví dụ: /api/moderation/datasets/status?status=PENDING_MODERATION

//    @GetMapping("/datasets/status")
//    public ResponseEntity<?> getDatasetByStatus(@RequestParam String status) {
//        List<DatasetInformation> filtered = datasetInforRepository.findAll().stream()
//                .filter(ds -> ds.getStatus().name().equalsIgnoreCase(status))
//                .toList();
//        return ResponseEntity.ok(new ApiResponse(true, "Danh sách dataset theo trạng thái " + status, filtered));
//    }

    @PostMapping("/moderate")
    public ResponseEntity<?> moderateDataset(
            @RequestParam Long datasetId,
            @RequestParam(required = false) Double thresholdPercent) {
        Map<String, Object> result = datasetValidateService.moderate(datasetId, thresholdPercent);
        return ResponseEntity.ok(result);
    }
}
