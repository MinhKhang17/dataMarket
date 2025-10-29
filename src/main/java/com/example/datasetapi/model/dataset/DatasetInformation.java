package com.example.datasetapi.model.dataset;

import com.example.datasetapi.dto.response.ValidationErrorDto;
import com.example.datasetapi.enums.Datasets.DatasetInforStatus;
import com.example.datasetapi.enums.Datasets.FileExtension;
//import com.example.datasetapi.model.userManager.Address;
import com.example.datasetapi.model.userManager.Provider;
import com.example.datasetapi.model.location.Commune;
import com.example.datasetapi.model.userManager.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
private DatasetInforStatus status;

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


@OneToMany(mappedBy = "datasetInformation", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DatasetValidationError> datasetValidationErrorList;

@JsonIgnore
@ManyToOne()
@JoinColumn(name = "dataset_type_id")
private DatasetType datasetType;

@JsonIgnore
@ManyToOne
    @JoinColumn(name = "provider_id")
    private User provider;
@ManyToOne
@JsonIgnore
    @JoinColumn(name = "commune_id")
    private Commune commune;

@Column
private LocalDateTime createAt = LocalDateTime.now();
@Column
    private LocalDateTime updateAt = LocalDateTime.now();
@Column
private LocalDate dataset_time;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Dataset dataset;

}
