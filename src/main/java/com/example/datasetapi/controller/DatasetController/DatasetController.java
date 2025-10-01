package com.example.datasetapi.controller.DatasetController;

import com.example.datasetapi.model.Dataset.DatasetInfor;
import com.example.datasetapi.service.dataset.DatasetSchemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private final DatasetSchemaService schemaService;

    @PostMapping("/readHeader")
    public ResponseEntity<?> uploadLocal(
            @RequestParam Long datasetTypeId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file
    ) {
        DatasetInfor ds = schemaService.uploadAndSchemaCheck(datasetTypeId, file, name, description);
        return ResponseEntity.ok(ds);
    }

    @PostMapping("/readByUrl")
    public ResponseEntity<?> uploadByUrl(
            @RequestParam Long datasetTypeId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String fileUrl
    ) {
        DatasetInfor ds = schemaService.uploadAndSchemaCheckByUrl(datasetTypeId, fileUrl, name, description);
        return ResponseEntity.ok(ds);
    }
}
