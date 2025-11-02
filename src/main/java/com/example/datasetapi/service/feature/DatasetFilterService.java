package com.example.datasetapi.service.feature;

import com.example.datasetapi.mapper.DatasetMapper;
import com.example.datasetapi.dto.request.DatasetFilterRequestDTO;
import com.example.datasetapi.dto.response.DatasetDTO;
import com.example.datasetapi.enums.Datasets.DatasetStatus;
import com.example.datasetapi.model.dataset.Dataset;
import com.example.datasetapi.repository.DatasetRepository;
import com.example.datasetapi.specification.DatasetSpecification;
import org.springframework.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class DatasetFilterService {
    @Autowired
    private  DatasetRepository datasetRepository;
    @Autowired
    private DatasetMapper datasetMapper;

    /**
     * Filter datasets with pagination
     */
    public Page<DatasetDTO> filterDatasets(DatasetFilterRequestDTO request) {

        // ✅ Nếu request null → tạo Pageable mặc định
        Pageable pageable = (request != null) ? createPageable(request)
                : PageRequest.of(0, 10); // size mặc định 10

        Specification<Dataset> spec = null;
        if (request != null) {
            spec = DatasetSpecification.filterDatasets(request);
        }

        Page<Dataset> page;
        if (spec == null) {
            page = datasetRepository.findAll(pageable);
        } else {
            page = datasetRepository.findAll(spec, pageable);
        }

        List<DatasetDTO> filteredList = page.getContent().stream()
                .filter(dataset -> dataset.getDatasetStatus() == DatasetStatus.APPROVE)
                .map(datasetMapper::toDatasetDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(filteredList, pageable, page.getTotalElements());
    }

    /**
     * Filter datasets without pagination (get all matching)
     */
    public List<Dataset> filterDatasetsAll(DatasetFilterRequestDTO request) {
        Specification<Dataset> spec = DatasetSpecification.filterDatasets(request);
        Sort sort = createSort(request);

        return datasetRepository.findAll(spec, sort);
    }

    /**
     * Count datasets matching filter criteria
     */
    public long countDatasets(DatasetFilterRequestDTO request) {
        Specification<Dataset> spec = DatasetSpecification.filterDatasets(request);
        return datasetRepository.count(spec);
    }

    /**
     * Quick filters - convenience methods
     */

    public Page<DatasetDTO> getDatasetsByProvince(String provinceId, int page, int size) {
        DatasetFilterRequestDTO request = DatasetFilterRequestDTO.builder()
                .provinceId(provinceId)
                .page(page)
                .size(size)
                .build();
        return filterDatasets(request);
    }

    public Page<DatasetDTO> getDatasetsByCommune(Long communeId, int page, int size) {
        DatasetFilterRequestDTO request = DatasetFilterRequestDTO.builder()
                .communeId(communeId)
                .page(page)
                .size(size)
                .build();
        return filterDatasets(request);
    }

    public Page<DatasetDTO> getDatasetsByYear(Integer year, int page, int size) {
        DatasetFilterRequestDTO request = DatasetFilterRequestDTO.builder()
                .year(year)
                .page(page)
                .size(size)
                .build();
        return filterDatasets(request);
    }

    public Page<DatasetDTO> getDatasetsByYearMonth(Integer year, Integer month, int page, int size) {
        DatasetFilterRequestDTO request = DatasetFilterRequestDTO.builder()
                .year(year)
                .month(month)
                .page(page)
                .size(size)
                .build();
        return filterDatasets(request);
    }

    public Page<DatasetDTO> getDatasetsByProvinceAndYear(
            String provinceId,
            Integer year,
            int page,
            int size
    ) {
        DatasetFilterRequestDTO request = DatasetFilterRequestDTO.builder()
                .provinceId(provinceId)
                .year(year)
                .page(page)
                .size(size)
                .build();
        return filterDatasets(request);
    }

    public Page<DatasetDTO> getDatasetsByDateRange(
            Integer startYear,
            Integer startMonth,
            Integer startDay,
            Integer endYear,
            Integer endMonth,
            Integer endDay,
            int page,
            int size
    ) {
        DatasetFilterRequestDTO request = DatasetFilterRequestDTO.builder()
                .startYear(startYear)
                .startMonth(startMonth)
                .startDay(startDay)
                .endYear(endYear)
                .endMonth(endMonth)
                .endDay(endDay)
                .page(page)
                .size(size)
                .build();
        return filterDatasets(request);
    }

    /**
     * Helper methods
     */

    private Pageable createPageable(DatasetFilterRequestDTO request) {
        Sort sort = createSort(request);
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Sort createSort(DatasetFilterRequestDTO request) {
        Sort.Direction direction = request.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, request.getSortBy());
    }
}
