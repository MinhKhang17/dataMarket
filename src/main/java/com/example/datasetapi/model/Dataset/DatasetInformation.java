package com.example.datasetapi.model.Dataset;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.enums.Datasets.FileExtension;
import com.example.datasetapi.model.userManager.Provider;
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
    private boolean isHeaderChecked = false;
    @Column
    private boolean isContentChecked = false;
@Column
private String file_url;
@Column
private FileExtension datasetExtension;
@Transient
private List<ValidationErrorDto> validationErrors;

@Column
@OneToMany
@JoinColumn(name =  "dataset_valoidation_error_id")
private List<DatasetValidationError> datasetValidationErrorList;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dataset_type_id")
private DatasetType datasetType;

@ManyToOne
    @JoinColumn(name = "provider_id")
    private Provider provider;
}
