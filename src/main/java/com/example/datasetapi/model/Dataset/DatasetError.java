package com.example.datasetapi.model.Dataset;


import com.example.datasetapi.enums.Datasets.ErrorCode;
import com.example.datasetapi.enums.Datasets.ValidationPhase;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "dataset_error")
@Getter @Setter
public class DatasetError {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    @Enumerated(EnumType.STRING)
    private ValidationPhase phase;     // dang o phase nao

    @Enumerated(EnumType.STRING)
    private ErrorCode code;

    @Column private String columnName; // co the null
    @Column private Long rowIndex;     // co the null
    @Column(length = 1000) private String message;
}
