package com.example.datasetapi.controller.DatasetController;

import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.repository.DatasetErrorRepository;
import com.example.datasetapi.service.dataset.DatasetModerationService;
import com.example.datasetapi.service.dataset.DatasetSchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetSchemaService schemaService;
    private final DatasetModerationService moderationService;

    @PostMapping("/readHeader")
    public ResponseEntity<?> uploadLocal(
            @RequestParam List<Long> datasetTypeIds,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Dataset ds = schemaService.uploadAndSchemaCheckMulti(datasetTypeIds, file, name, description);
            return ResponseEntity.ok(ds);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Loi upload file local: " + e.getMessage());
        }
    }

    @PostMapping("/readByUrlMulti")
    public ResponseEntity<?> uploadByUrlMulti(
            @RequestParam List<Long> datasetTypeIds,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String fileUrl
    ) {
        try {
            Dataset ds = schemaService.uploadAndSchemaCheckByUrlMulti(datasetTypeIds, fileUrl, name, description);
            return ResponseEntity.ok(ds);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Loi upload bang URL multi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/moderate")
    public ResponseEntity<?> moderate(@PathVariable Long id, @RequestParam(required = false) Double thresholdPercent) {
        try {
            return ResponseEntity.ok(moderationService.moderate(id, thresholdPercent));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Loi moderation: " + e.getMessage());
        }
    }
}