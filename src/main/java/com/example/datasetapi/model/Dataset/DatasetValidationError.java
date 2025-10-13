package com.example.datasetapi.model.Dataset;


import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dataset_validation_error")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetValidationError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Liên kết với DatasetInformation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_information_id", nullable = false)
    private DatasetInformation datasetInformation;

    // Loại lỗi: NULL_VALUE, DUPLICATE_ROW, INVALID_ENUM, VALUE_VARIATION, ...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErrorCode errorCode;

    // Giai đoạn: SCHEMA_CHECK, MODERATION, ...
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ValidationPhase validationPhase;

    // Cột nào bị lỗi
    private String columnName;

    // Dòng nào bị lỗi (0-based)
    private Long rowIndex;

    // Mô tả chi tiết lỗi
    @Column(columnDefinition = "TEXT")
    private String message;

}