package com.example.datasetapi.model.paySystem;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WithdrawOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long userId;
    @Column
    private String otp;
    @Column
    private Instant expireAt;
}
