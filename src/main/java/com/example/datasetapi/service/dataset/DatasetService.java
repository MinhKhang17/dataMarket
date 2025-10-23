package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.CheckoutRequestDTO;
import com.example.datasetapi.dto.request.ConsumerBuyRequestDTO;
import com.example.datasetapi.dto.request.ProviderUploadDatasetRequest;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.CheckoutResponseDTO;
import com.example.datasetapi.dto.response.ConsumerBuyResponseDTO;
import com.example.datasetapi.dto.response.DatasetParentReposonseDto;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetInformation;
import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.model.dataset.TimeGroup;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public interface DatasetService {


    public void checkExitsAndCreateDatasetGroupAndDateset(ProviderUploadDatasetRequest providerUploadDatasetRequest, long Provider_id, DatasetInformation datasetInformation);


    ResponseEntity<ApiResponse> getAllCategories();

    ResponseEntity<ApiResponse> getAllDatasetType();


    ResponseEntity<?> acceptDataset(long datasetInforId, HttpServletRequest request);
    Dataset uploadCSVFileToPendingFolder(File file, Dataset dataset);

    ResponseEntity<?> getAllAllDataset();

    ResponseEntity<?> rejectDataset(long datasetInforId, HttpServletRequest request,String reason);

    ResponseEntity<?> getAllDatasetParent();

    DatasetParentReposonseDto getDatasetParentWithId(long datasetGroupId);

    DatasetParentReposonseDto getDatasetParentDetailByDatasetId(long datasetId);

    CheckoutResponseDTO checkoutDatasetPayment(CheckoutRequestDTO checkoutRequestDTO, HttpServletRequest request);

    ConsumerBuyResponseDTO buyDatasetRequest(ConsumerBuyRequestDTO buyRequestDTO, HttpServletRequest request);

    ConsumerBuyResponseDTO subRegister(long subType, HttpServletRequest request);

    List<ConsumerSubscription> findConsumerSub(User consumer);

    ConsumerBuyResponseDTO selectSubPack(long consumerSubId, HttpServletRequest request);

    List<TimeGroupDTO> getAllTimeGroupFollowDatasetChildGroup(long datasetChildGroupId);

    List<DatasetDTO> findAllDatasetByTimeGroup(long datasetTimeGroupId);

    ConsumerBuyResponseDTO buyWithTimeGroup(long timeGroupId, HttpServletRequest request);

    void saveTimeGroup(TimeGroup timeGroup);

    ConsumerBuyResponseDTO buyTimeGroupWithSub(long timeGroupId, HttpServletRequest request);

    ConsumerBuyResponseDTO buyGroupByAPI(long timeGroupId, HttpServletRequest request);

    ConsumerBuyResponseDTO buyApiPack(long apiPackId, HttpServletRequest request);
}
