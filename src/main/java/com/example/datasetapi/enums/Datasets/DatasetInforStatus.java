package com.example.datasetapi.enums.Datasets;

public enum DatasetInforStatus {
    PENDING,
    SCHEMA_FAILED,        // Bước 1 fail
    PENDING_MODERATION,   // Bước 1 qua
    REJECTED,             // Bước 2 fail (>2%)
    APPROVED              // Bước 2 pass (≤2%)
}