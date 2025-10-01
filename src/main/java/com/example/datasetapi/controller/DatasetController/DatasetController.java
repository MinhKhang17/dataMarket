package com.example.datasetapi.controller.DatasetController;

import com.example.datasetapi.model.Dataset.DatasetInfor;
import com.example.datasetapi.service.Dataset.DatasetValidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
public class DatasetController {

    private DatasetValidateService datasetValidateService;

    @PostMapping("/readHeader")
    public ResponseEntity<?> uploadLocal(
            @RequestParam Long datasetTypeId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file
    ) {
       return  datasetValidateService.uploadAndHeaderCheck(datasetTypeId, file, name, description);

    }

    @PostMapping("/readByUrl")
    public ResponseEntity<?> uploadByUrl(
            @RequestParam Long datasetTypeId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String fileUrl
    ) {
        DatasetInfor ds = datasetValidateService.uploadAndSchemaCheckByUrl(datasetTypeId, fileUrl, name, description);
        return ResponseEntity.ok(ds);
    }
}
