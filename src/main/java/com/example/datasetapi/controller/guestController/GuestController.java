package com.example.datasetapi.controller.guestController;

import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.DatasetParentReposonseDto;
import com.example.datasetapi.service.dataset.DatasetService;
import com.example.datasetapi.dto.service.PricingRuleDTO;
import com.example.datasetapi.service.dataset.PriceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/guest")
public class GuestController {
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private PriceService priceService;
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
    @GetMapping("/sub/get")
    public ResponseEntity<ApiResponse> getDatasetSub() {
        List<PricingRuleDTO> subPacks = priceService.getAllSubPack();
        return  ResponseEntity.ok().body(new ApiResponse(true,"load success",subPacks));
    }
    @GetMapping("/getTime")
    public ResponseEntity<ApiResponse> getDatasetTime(@RequestParam long dataset_child_group_id){
        return ResponseEntity.ok().body(new ApiResponse(true,"load success",datasetService.getAllTimeGroupFollowDatasetChildGroup(dataset_child_group_id))) ;
    }
    @GetMapping("/getDatasets")
    public ResponseEntity<ApiResponse> getDataset(@RequestParam long dataset_time_Group_id){
        return ResponseEntity.ok().body(new ApiResponse(true,"load success",datasetService.findAllDatasetByTimeGroup(dataset_time_Group_id)));
    }
    @GetMapping("apiPack/get")
    public ResponseEntity<ApiResponse>getAPIs(){
        List<PricingRuleDTO> pricingRuleDTO = priceService.getAllAPIPricingRule();
        return ResponseEntity.ok().body(new ApiResponse(true,"load success",pricingRuleDTO));
    }

}
