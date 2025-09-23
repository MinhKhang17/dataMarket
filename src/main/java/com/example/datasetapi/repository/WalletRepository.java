package com.example.datasetapi.repository;

import com.example.datasetapi.model.paySystem.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet,Long> {

}
