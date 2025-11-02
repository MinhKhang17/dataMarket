package com.example.datasetapi.controller.datasetController;
import com.example.datasetapi.mapper.DatasetMapper;
import com.example.datasetapi.dto.request.DatasetFilterRequestDTO;
import com.example.datasetapi.dto.response.DatasetDTO;
import com.example.datasetapi.service.feature.DatasetFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/filter/datasets")
@RequiredArgsConstructor

public class DatasetFilterController {
    @Autowired
    private final DatasetFilterService filterService;
    @Autowired
    private DatasetMapper datasetMapper;

    /**
     * Main filter endpoint - supports all filter combinations
     * POST /api/datasets/filter
     */
    @PostMapping("/filter")
    public ResponseEntity<Page<DatasetDTO>> filterDatasets(
            @RequestBody(required = false) DatasetFilterRequestDTO request
    ) {
        Page<DatasetDTO> result = filterService.filterDatasets(request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET endpoint with query parameters
     * GET /api/datasets/search?provinceId=1&year=2024&month=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DatasetDTO>> searchDatasets(
            @RequestParam(required = false) String provinceId,
            @RequestParam(required = false) Long communeId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false) Long providerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        DatasetFilterRequestDTO request = DatasetFilterRequestDTO.builder()
                .provinceId(provinceId)
                .communeId(communeId)
                .year(year)
                .month(month)
                .day(day)
                .providerId(providerId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();

        Page<DatasetDTO> result = filterService.filterDatasets(request);


        return ResponseEntity.ok(result);
    }

    /**
     * Count datasets matching criteria
     */
    @PostMapping("/count")
    public ResponseEntity<Long> countDatasets(@RequestBody DatasetFilterRequestDTO request) {
        long count = filterService.countDatasets(request);
        return ResponseEntity.ok(count);
    }

    /**
     * Quick filter endpoints
     */

    @GetMapping("/by-province/{provinceId}")
    public ResponseEntity<Page<DatasetDTO>> getByProvince(
            @PathVariable String provinceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Page<DatasetDTO> result = filterService.getDatasetsByProvince(provinceId, page, size);
        return ResponseEntity.ok(result);

    }

    @GetMapping("/by-commune/{communeId}")
    public ResponseEntity<Page<DatasetDTO>> getByCommune(
            @PathVariable Long communeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DatasetDTO> result = filterService.getDatasetsByCommune(communeId, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-year/{year}")
    public ResponseEntity<Page<DatasetDTO>> getByYear(
            @PathVariable Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<DatasetDTO> result = filterService.getDatasetsByYear(year, page, size);
        return ResponseEntity.ok(result);
    }
}
