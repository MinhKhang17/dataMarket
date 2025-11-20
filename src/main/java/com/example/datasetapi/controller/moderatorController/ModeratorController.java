package com.example.datasetapi.controller.moderatorController;

import com.example.datasetapi.dto.request.ModeratorCreateNewDatasetGroupRequest;
import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.DatasetDTO;
import com.example.datasetapi.dto.response.DatasetParentReposonseDto;
import com.example.datasetapi.dto.service.DatasetGroupInfor;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.model.dataset.DatasetGroup;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.service.dataset.DatasetValidateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/moderator/dataset")
public class ModeratorController {
    @Autowired
    private DatasetValidateService datasetValidateService;
    @Autowired
    private DatasetService datasetService;

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("/get-dataset-validation")
    public ResponseEntity<?> getAllDatasetInformation() {
        return datasetValidateService.getAllDatasetErrorWithDatasetInfor();
    }
    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping("/accept")
    public ResponseEntity<?> acceptDataset(@RequestParam long datasetInforId, HttpServletRequest request) {
            return datasetService.acceptDataset(datasetInforId,request);
    }
    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping("/reject")
    public ResponseEntity<?> rejectDataset(@RequestParam long datasetInforId, HttpServletRequest request,String reason) {
        return datasetService.rejectDataset(datasetInforId,request,reason);
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping("dataset-group")
    public ResponseEntity<?> CreateDatasetGroup(@RequestParam ModeratorCreateNewDatasetGroupRequest moderatorCreateNewDatasetGroupRequest, HttpServletRequest request) {
        datasetService.moderatorCreateNewDatasetGroup(moderatorCreateNewDatasetGroupRequest);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("dataset-group")
    public ResponseEntity<?> getDatasetGroup() {
        List<DatasetParentReposonseDto> datasetParentReposonseDtos = datasetService.findAllSystamDatasetGroup();
        return ResponseEntity.ok().body(new ApiResponse(true,"load succes",datasetParentReposonseDtos));
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping("system-dataset/update")
    public ResponseEntity<?> uploadNewDatasetToDatasetGroup(@RequestParam MultipartFile file,
                                                                      @RequestParam long datasetTypeId,
                                                                      HttpServletRequest request,
                                                                      @ModelAttribute ProviderUploadDatasetRequest providerUploadDatasetRequest) {
        return datasetValidateService.uploadAndHeaderCheckCSVFile(file,datasetTypeId,request,providerUploadDatasetRequest, DatasetSourceType.SYSTEM_DATASET);
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("dataset-provider")
    public ResponseEntity<?> getDatasetProvider(HttpServletRequest request) {
        return ResponseEntity.ok().body(new ApiResponse(true,"load succes",datasetService.findAllProviderDataset()));
    }

    @PreAuthorize("hasRole('MODERATOR')")
    @GetMapping("group/info")
    public ResponseEntity<?> getGroupInfor(@RequestParam long datasetGroupId) {
        DatasetGroupInfor datasetGroupInfor = datasetService.getDatasetGroupInfor(datasetGroupId);
        return ResponseEntity.ok().body(new ApiResponse(true,"load succes",datasetGroupInfor));
    }
    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping("/download/{id}")
    public ResponseEntity<?> downloadDataset(@PathVariable("id") long id,HttpServletRequest request) {
        return datasetService.downloadDatasetNoValidToken(id);
    }

}
