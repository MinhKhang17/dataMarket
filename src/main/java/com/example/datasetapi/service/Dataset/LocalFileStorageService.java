package com.example.datasetapi.service.Dataset;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalFileStorageService implements com.example.datasetapi.service.Dataset.FileStorageService {

    private final Path root = Paths.get(System.getProperty("java.io.tmpdir"), "uploads");

    public LocalFileStorageService() throws IOException { Files.createDirectories(root); }

    @Override
    public String store(MultipartFile file) throws IOException {
        Path dest = root.resolve(UUID.randomUUID() + "_" + Objects.requireNonNull(file.getOriginalFilename()));
        try (InputStream in = file.getInputStream()) { Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING); }
        return dest.toAbsolutePath().toString();
    }
}
