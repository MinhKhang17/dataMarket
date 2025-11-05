package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long>, JpaSpecificationExecutor<Dataset> {
    Optional<Dataset> findById(long Id);

    @Query("SELECT d FROM Dataset d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Dataset> searchByKeyword(String keyword);
    @Query("SELECT d FROM Dataset d WHERE d.id IN :ids")
    List<Dataset> findAllByIdIn( List<Long> ids);
}
