package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.enums.Datasets.FileExtension;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "datasetInformation")
public class DatasetInformation {
@Id
@GeneratedValue(strategy= GenerationType.IDENTITY)
private Long id;

@Column
private String name;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private DatasetInforStatus status = DatasetInforStatus.PENDING;

@Column
private Long rowCount;

@Column
private String file_url;
@Column
private FileExtension datasetExtension;
@Transient
private List<ValidationErrorDto> validationErrors;

@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dataset_type_id")
private DatasetType datasetType;
}
