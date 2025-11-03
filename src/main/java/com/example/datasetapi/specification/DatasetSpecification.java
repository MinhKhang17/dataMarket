package com.example.datasetapi.specification;

import com.example.datasetapi.dto.request.DatasetFilterRequestDTO;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.model.dataset.DatasetGroup;
import com.example.datasetapi.model.dataset.TimeGroup;
import com.example.datasetapi.model.location.Commune;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DatasetSpecification {

    public static Specification<Dataset> filterDatasets(DatasetFilterRequestDTO request) {
        return (root, query, criteriaBuilder) -> {
            // Defensive: if request null, return match-all
            if (request == null) {
                query.distinct(true);
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            // JOINs
            Join<Dataset, DatasetGroup> datasetGroupJoin = root.join("datasetChildGroup", JoinType.LEFT);
            Join<Dataset, TimeGroup> timeGroupJoin = root.join("timeGroup", JoinType.LEFT);
            Join<DatasetGroup, DatasetGroup> parentGroupJoin = datasetGroupJoin.join("parent", JoinType.LEFT);

            // ===== LOCATION FILTERS =====
            // Commune handling (Commune PK is String idCommune)
            if (request.getCommuneId() != null && !request.getCommuneId().trim().isEmpty()) {
                String communeId = request.getCommuneId().trim();

                List<Predicate> communePreds = new ArrayList<>();

                // Try join to Commune association on child group
                try {
                    Join<DatasetGroup, Commune> childCommuneJoin = datasetGroupJoin.join("commune", JoinType.LEFT);
                    communePreds.add(criteriaBuilder.equal(childCommuneJoin.get("idCommune"), communeId));
                } catch (IllegalArgumentException ignored) {
                    // no association 'commune' on DatasetGroup -> fallback to string fields
                    try {
                        communePreds.add(criteriaBuilder.equal(datasetGroupJoin.get("communeCode"), communeId));
                    } catch (IllegalArgumentException ignored2) {
                        try {
                            communePreds.add(criteriaBuilder.equal(datasetGroupJoin.get("commune"), communeId));
                        } catch (IllegalArgumentException ignored3) {
                            // nothing to add from child side
                        }
                    }
                }

                // Try join to Commune association on parent group
                try {
                    Join<DatasetGroup, Commune> parentCommuneJoin = parentGroupJoin.join("commune", JoinType.LEFT);
                    communePreds.add(criteriaBuilder.equal(parentCommuneJoin.get("idCommune"), communeId));
                } catch (IllegalArgumentException ignored) {
                    // fallback to parent group string fields
                    try {
                        communePreds.add(criteriaBuilder.equal(parentGroupJoin.get("communeCode"), communeId));
                    } catch (IllegalArgumentException ignored2) {
                        try {
                            communePreds.add(criteriaBuilder.equal(parentGroupJoin.get("commune"), communeId));
                        } catch (IllegalArgumentException ignored3) {
                            // nothing to add from parent side
                        }
                    }
                }

                if (!communePreds.isEmpty()) {
                    predicates.add(criteriaBuilder.or(communePreds.toArray(new Predicate[0])));
                } else {
                    // no matching attribute names found — noop (or optionally throw)
                }
            }

            // Province (similar approach but simpler: compare parent's province.id if present)
            if (request.getProvinceId() != null) {
                try {
                    predicates.add(criteriaBuilder.equal(parentGroupJoin.get("province").get("id"), request.getProvinceId()));
                } catch (IllegalArgumentException ex) {
                    // if mapping different, you can add additional fallbacks here
                }
            }

            // ===== TIME FILTERS =====
            if (request.getYear() != null) {
                predicates.add(criteriaBuilder.equal(timeGroupJoin.get("year"), request.getYear()));
            }
            if (request.getMonth() != null) {
                predicates.add(criteriaBuilder.equal(timeGroupJoin.get("month"), request.getMonth()));
            }
            if (request.getDay() != null) {
                predicates.add(criteriaBuilder.equal(timeGroupJoin.get("day"), request.getDay()));
            }

            // Date ranges
            if (request.getStartYear() != null || request.getStartMonth() != null || request.getStartDay() != null) {
                predicates.add(createDateComparisonPredicate(
                        criteriaBuilder,
                        timeGroupJoin,
                        request.getStartYear(),
                        request.getStartMonth(),
                        request.getStartDay(),
                        true
                ));
            }
            if (request.getEndYear() != null || request.getEndMonth() != null || request.getEndDay() != null) {
                predicates.add(createDateComparisonPredicate(
                        criteriaBuilder,
                        timeGroupJoin,
                        request.getEndYear(),
                        request.getEndMonth(),
                        request.getEndDay(),
                        false
                ));
            }

            // ===== OTHER FILTERS =====
            if (request.getProviderId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("provider").get("id"), request.getProviderId()));
            }
            if (request.getDatasetStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("datasetStatus"), request.getDatasetStatus()));
            }
            if (request.getDatasetPack() != null) {
                predicates.add(criteriaBuilder.equal(root.get("datasetPack"), request.getDatasetPack()));
            }
            if (request.getDatasetTypeId() != null) {
                predicates.add(criteriaBuilder.equal(datasetGroupJoin.get("datasetType").get("id"), request.getDatasetTypeId()));
            }

            query.distinct(true);
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Create date comparison using composite value year*10000 + month*100 + day
     * Use COALESCE for month/day to avoid NULL arithmetic.
     */
    private static Predicate createDateComparisonPredicate(
            CriteriaBuilder cb,
            Join<Dataset, TimeGroup> timeGroupJoin,
            Integer year,
            Integer month,
            Integer day,
            boolean isGreaterThanOrEqual
    ) {
        Expression<Integer> yearExpr = timeGroupJoin.get("year");
        Expression<Integer> monthExpr = cb.coalesce(timeGroupJoin.get("month"), 0);
        Expression<Integer> dayExpr = cb.coalesce(timeGroupJoin.get("day"), 0);

        Expression<Integer> dateValue = cb.sum(
                cb.sum(cb.prod(yearExpr, 10000), cb.prod(monthExpr, 100)),
                dayExpr
        );

        int compareValue = (year != null ? year * 10000 : 0)
                + (month != null ? month * 100 : 0)
                + (day != null ? day : 0);

        if (isGreaterThanOrEqual) {
            return cb.greaterThanOrEqualTo(dateValue, compareValue);
        } else {
            return cb.lessThanOrEqualTo(dateValue, compareValue);
        }
    }
}
