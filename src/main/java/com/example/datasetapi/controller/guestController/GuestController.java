package com.example.datasetapi.controller.guestController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.DatasetParentReposonseDto;
import com.example.datasetapi.service.dataset.DatasetService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/guest")
public class GuestController {
    @Autowired
    private DatasetService datasetService;
    @GetMapping("dataset/group/get")
    public ResponseEntity<ApiResponse> getDataset(@RequestParam("id") long datasetGroupId, HttpServletRequest request) {
        DatasetParentReposonseDto datasetParentReposonseDtos = datasetService.getDatasetParentWithId(datasetGroupId);
     return ResponseEntity.ok().body(new ApiResponse(true,"load success",datasetParentReposonseDtos));
    }
    @GetMapping("dataset/detail/get")
    public ResponseEntity<ApiResponse> getDatasetDetail(@RequestParam("id") long datasetId, HttpServletRequest request) {
        DatasetParentReposonseDto datasetParentReposonseDto = datasetService.getDatasetParentDetailByDatasetId(datasetId);
        return ResponseEntity.ok().body(new ApiResponse(true,"load success",datasetParentReposonseDto));
    }
}
