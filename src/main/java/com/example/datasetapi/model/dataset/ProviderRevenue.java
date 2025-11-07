package com.example.datasetapi.model.dataset;

import com.example.datasetapi.model.userManager.Provider;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "provider_revenue")
public class ProviderRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private  Provider provider;
    @ManyToOne(fetch = FetchType.LAZY)
     private Dataset dataset;

     private double revenue_amount;

    private LocalDateTime createdAt =LocalDateTime.now();
}
