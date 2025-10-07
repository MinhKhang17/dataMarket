package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.config.S3Config;
import com.example.datasetapi.dto.response.ApiResponse;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.Dataset.Category;
import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.model.Dataset.DatasetType;
import com.example.datasetapi.model.Dataset.DownloadToken;
import com.example.datasetapi.repository.CategoryRepository;
import com.example.datasetapi.repository.DatasetRepository;
import com.example.datasetapi.repository.DatasetTypeRepository;
import com.example.datasetapi.repository.DowloadTokenRepository;
import com.example.datasetapi.service.Dataset.DatasetService;
import com.example.datasetapi.service.user.TokenService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DatasetServiceImpl implements DatasetService {

    @Autowired
    private S3Client s3Client;
    @Autowired
    private DatasetRepository datasetRepository;
    @Autowired
    private DowloadTokenRepository dowloadTokenRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private DatasetTypeRepository datasetTypeRepository;
    DatasetServiceImpl(S3Client s3Client, S3Config s3Config, DatasetRepository datasetRepository,DowloadTokenRepository dowloadTokenRepository) {
    this.dowloadTokenRepository = dowloadTokenRepository;
        this.s3Client = s3Client;
        this.datasetRepository = datasetRepository;
    }

    @Value("${aws.bucket.name}")
    private String BUCKET_NAME;


    @Override
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(new ApiResponse(true,"load Category success",categories));
    }

    @Override
    public ResponseEntity<ApiResponse> getAllDatasetType() {
        List<DatasetType> datasetTypes = datasetTypeRepository.findAll();
        if(datasetTypes.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(new ApiResponse(true,"load DatasetType success",datasetTypes));
    }


    @Override
    public Dataset uploadCSVFileToPendingFolder(MultipartFile file,Dataset dataset) {
        try {

            String fileName = file.getOriginalFilename();
            String fileKey = "PENDING/"+UUID.randomUUID()+file.getOriginalFilename();

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(fileKey)
                            .build(),
                    RequestBody.fromBytes(file.getBytes()));



            dataset.setFileKey(fileKey);
            dataset.setName(fileName);
            dataset.setDatasetStatus(DatasetStatus.PEDDING);
            return datasetRepository.save(dataset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Transactional
    @Override
    public ResponseEntity<?> dowloadDataset(String dowloadToken) {

        //lay dowload token tu request checck xem nguoi dung co permussion de su dung hay khong
        Optional<DownloadToken> downloadTokenOptional = dowloadTokenRepository.findById(UUID.fromString(dowloadToken));

        //neu khong ton tai thi tra ve loi
        if(!downloadTokenOptional.isPresent()){
            return ResponseEntity.internalServerError().body(new ApiResponse(false,"token is not valid",null));
        }

        long datasetId = downloadTokenOptional.get().getDatasetId();

        Optional<Dataset> datasetGetFromToken = datasetRepository.findById(datasetId);

        if(!datasetGetFromToken.isPresent()){
            return ResponseEntity.internalServerError().body(new ApiResponse(false,"can not find dataset with id + "+datasetId,null));
        }


        Dataset dataset = datasetGetFromToken.get();

        String fileKey = dataset.getFileKey();

        DownloadToken downloadToken = downloadTokenOptional.get();
        downloadToken.setUsed(true);
        dowloadTokenRepository.save(downloadToken);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(fileKey)
                .build();

        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
        InputStreamResource resource = new InputStreamResource(s3Object);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + Paths.get(fileKey).getFileName().toString() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
