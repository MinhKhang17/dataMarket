package com.example.datasetapi.specification;


import com.example.datasetapi.model.Dataset.Dataset;
import com.example.datasetapi.dto.request.DatasetFilterRequestDTO;
import com.example.datasetapi.model.Dataset.DatasetGroup;
import com.example.datasetapi.model.Dataset.TimeGroup;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
public class DatasetSpecification {
    public static Specification<Dataset> filterDatasets(DatasetFilterRequestDTO request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();



            Join<Dataset, DatasetGroup> datasetGroupJoin = root.join("datasetChildGroup", JoinType.LEFT);

            // Join với TimeGroup
            Join<DatasetGroup, TimeGroup> timeGroupJoin = datasetGroupJoin.join("timeGroups", JoinType.LEFT);


            // Join với DatasetGroup parent (province)
            Join<DatasetGroup, DatasetGroup> parentGroupJoin = datasetGroupJoin.join("parent", JoinType.LEFT);


            // ===== LOCATION FILTERS =====
            if (request.getCommuneId() != null) {
                // Filter by commune
                predicates.add(criteriaBuilder.equal(
                        datasetGroupJoin.get("commune").get("id"),
                        request.getCommuneId()
                ));
            }

            if (request.getProvinceId() != null) {
                if (request.getCommuneId() == null) {
                    // Filter by province: get all datasets in communes of this province
                    predicates.add(criteriaBuilder.equal(
                            parentGroupJoin.get("province").get("id"),
                            request.getProvinceId()
                    ));
                } else {
                    // Verify commune belongs to province
                    predicates.add(criteriaBuilder.equal(
                            parentGroupJoin.get("province").get("id"),
                            request.getProvinceId()
                    ));
                }
            }

            // ===== TIME FILTERS =====
            // Exact date matching
            if (request.getYear() != null) {
                predicates.add(criteriaBuilder.equal(
                        timeGroupJoin.get("year"),
                        request.getYear()
                ));
            }

            if (request.getMonth() != null) {
                predicates.add(criteriaBuilder.equal(
                        timeGroupJoin.get("month"),
                        request.getMonth()
                ));
            }

            if (request.getDay() != null) {
                predicates.add(criteriaBuilder.equal(
                        timeGroupJoin.get("day"),
                        request.getDay()
                ));
            }

            // Date range filtering
            if (request.getStartYear() != null || request.getStartMonth() != null || request.getStartDay() != null) {
                predicates.add(createDateComparisonPredicate(
                        criteriaBuilder,
                        timeGroupJoin,
                        request.getStartYear(),
                        request.getStartMonth(),
                        request.getStartDay(),
                        true // >= comparison
                ));
            }

            if (request.getEndYear() != null || request.getEndMonth() != null || request.getEndDay() != null) {
                predicates.add(createDateComparisonPredicate(
                        criteriaBuilder,
                        timeGroupJoin,
                        request.getEndYear(),
                        request.getEndMonth(),
                        request.getEndDay(),
                        false // <= comparison
                ));
            }

            // ===== OTHER FILTERS =====
            if (request.getProviderId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("provider").get("id"),
                        request.getProviderId()
                ));
            }

            if (request.getDatasetStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("datasetStatus"),
                        request.getDatasetStatus()
                ));
            }

            if (request.getDatasetPack() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("datasetPack"),
                        request.getDatasetPack()
                ));
            }

            if (request.getDatasetTypeId() != null) {
                predicates.add(criteriaBuilder.equal(
                        datasetGroupJoin.get("datasetType").get("id"),
                        request.getDatasetTypeId()
                ));
            }

            // Distinct results
            query.distinct(true);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Helper method to create date comparison predicate
     * Compares year, month, day as a composite date
     */
    private static Predicate createDateComparisonPredicate(
            CriteriaBuilder cb,
            Join<DatasetGroup, TimeGroup> timeGroupJoin,
            Integer year,
            Integer month,
            Integer day,
            boolean isGreaterThanOrEqual
    ) {
        // Create composite date value: year * 10000 + month * 100 + day
        Expression<Integer> dateValue = cb.sum(
                cb.sum(
                        cb.prod(timeGroupJoin.get("year"), 10000),
                        cb.prod(timeGroupJoin.get("month"), 100)
                ),
                timeGroupJoin.get("day")
        );

        int compareValue = (year != null ? year * 10000 : 0) +
                (month != null ? month * 100 : 0) +
                (day != null ? day : 0);

        if (isGreaterThanOrEqual) {
            return cb.greaterThanOrEqualTo(dateValue, compareValue);
        } else {
            return cb.lessThanOrEqualTo(dateValue, compareValue);
        }
    }
}
