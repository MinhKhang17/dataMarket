package com.example.datasetapi.service.Dataset;

import com.example.datasetapi.model.Dataset.Dataset;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface DatasetService {

    ResponseEntity<?> dowloadDataset(String dowloadToken);

    Dataset uploadCSVFileToPendingFolder(MultipartFile file,Dataset dataset);
}
