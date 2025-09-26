package com.example.datasetapi.service.feature;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    public String uploadImage(MultipartFile file) throws IOException;
}
