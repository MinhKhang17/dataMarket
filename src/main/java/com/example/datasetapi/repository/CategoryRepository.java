package com.example.datasetapi.repository;

import com.example.datasetapi.model.dataset.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Integer> {

    List<Category> findByNameIn(List<String> location);

    Category findAllByName(String name);

    List<Category> findAllByNameIn(Collection<String> names);
}
