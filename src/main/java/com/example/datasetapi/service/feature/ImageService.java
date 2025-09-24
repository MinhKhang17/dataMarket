package com.example.datasetapi.service.feature;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    public String uploadImage(MultipartFile file);
}
