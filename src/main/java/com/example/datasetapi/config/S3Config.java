package com.example.datasetapi.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @PostConstruct
    public void validateCredentials() {
        System.out.println("=== AWS Configuration Check ===");
        System.out.println("Access Key Length: " + (accessKey != null ? accessKey.length() : "NULL"));
        System.out.println("Access Key Prefix: " + (accessKey != null && accessKey.length() >= 4 ? accessKey.substring(0, 4) : "INVALID"));
        System.out.println("Secret Key Length: " + (secretKey != null ? secretKey.length() : "NULL"));
        System.out.println("Region: " + region);
        System.out.println("Has leading/trailing spaces in access key: " + (!accessKey.equals(accessKey.trim())));
        System.out.println("Has leading/trailing spaces in secret key: " + (!secretKey.equals(secretKey.trim())));
        System.out.println("==============================");
    }

    @Bean
    public S3Client s3Client() {
        // Trim to remove any whitespace
        String cleanAccessKey = accessKey.trim();
        String cleanSecretKey = secretKey.trim();
        String cleanRegion = region.trim();

        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(
                cleanAccessKey,
                cleanSecretKey
        );

        return S3Client.builder()
                .region(Region.of(cleanRegion))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }
}