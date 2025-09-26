package com.example.datasetapi.repository;

import com.example.datasetapi.model.UserManager.ProviderIdentityDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProviderIndentityDocumentRepository extends JpaRepository<ProviderIdentityDocument, Integer> {
}
