package com.example.datasetapi.service.dataset;

import com.example.datasetapi.dto.request.*;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.dto.response.CheckoutResponseDTO;
import com.example.datasetapi.dto.response.ConsumerBuyResponseDTO;
import com.example.datasetapi.dto.response.DatasetParentReposonseDto;
import com.example.datasetapi.dto.service.DatasetGroupInfor;
import com.example.datasetapi.enums.Datasets.DatasetSourceType;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.enums.DowloadType;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.userManager.ConsumerSubscription;
import com.example.datasetapi.model.userManager.User;
import com.example.datasetapi.dto.response.*;
import com.example.datasetapi.model.dataset.TimeGroup;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface DatasetService {




    ResponseEntity<ApiResponse> getAllCategories();

    ResponseEntity<ApiResponse> getAllDatasetType();


    @Transactional
    void checkExitsAndCreateDatasetGroupAndDateset(
            ProviderUploadDatasetRequest request,
            long providerId,
            Dataset dataset,
            DatasetSourceType datasetSourceType, MultipartFile file);

    ResponseEntity<?> acceptDataset(long datasetInforId, HttpServletRequest request);

    List<Dataset> findAllByStatus(DatasetStatus datasetStatus);

    Dataset uploadCSVFileToSytemFolder(MultipartFile file, Dataset dataset);
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

    ResponseEntity<ApiResponse> searchDatasetByName(String datasetName);

    Dataset uploadCSVFileToPendingFolder(MultipartFile file, Dataset dataset);

    public Dataset moveFileFromPendingToApproveFolder(Dataset dataset);

    String getDownloadTokenOfDatasetForConsumer(long datasetId, HttpServletRequest request);

    ResponseEntity<?> downloadDataset(String dowloadToken, HttpServletRequest request, DowloadType dowloadType);

    @Transactional
    ResponseEntity<?> downloadDatasetNoValidToken(Long datasetId);

    List<DatasetDTO> findAllConsumerDataset(HttpServletRequest request);

    ConsumerBuyResponseDTO buyDatasetWithSub(long datasetId, HttpServletRequest request);

    void moderatorCreateNewDatasetGroup(ModeratorCreateNewDatasetGroupRequest moderatorCreateNewDatasetGroupRequest);

    List<DatasetParentReposonseDto> findAllSystamDatasetGroup();


    List<DatasetParentReposonseDto>  findAllProviderDataset();

    DatasetGroupInfor getDatasetGroupInfor(long datasetChilGroupId);


    List<DatasetDTO> getAllProviderDataset(HttpServletRequest request);

    DatasetDTO getDatasetDetail(Long id, HttpServletRequest request);

    boolean cancelDataset(Long id, HttpServletRequest request);

    DatasetUpdateResponse updateDataset(Long id, DatasetUpdateRequest request);

    ConsumerBuyResponseDTO buyAPIPack(ConsumerBuyRequestDTO buyRequestDTO, HttpServletRequest request);

    ResponseEntity<?> getDataForApiBuying(String token);
}
