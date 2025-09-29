package com.example.datasetapi.repository;

import com.example.datasetapi.model.Dataset.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Integer> {

    List<Category> findByNameIn(List<String> location);
}
