package com.syv.data.Diffyne.controller;

import com.syv.data.Diffyne.dto.DualSourceComparisonRequest;
import com.syv.data.Diffyne.model.ComparisonResult;
import com.syv.data.Diffyne.model.RecordDifference;
import com.syv.data.Diffyne.service.DirectComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/direct-comparisons")
public class DirectComparisonController {

    @Autowired
    private DirectComparisonService directComparisonService;

    /**
     * Compare two REST API endpoints directly
     */
    @PostMapping("/rest-api")
    public ResponseEntity<ComparisonResult> compareRestApis(@RequestBody DualSourceComparisonRequest request) {
        ComparisonResult result = directComparisonService.compareRestApiSources(
                request.getSourceOneUrl(),
                request.getSourceOneParams(),
                request.getSourceTwoUrl(),
                request.getSourceTwoParams(),
                request.getComparisonConfig()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Compare data from two separate sources (can be different types)
     */
    @PostMapping("/multi-source")
    public ResponseEntity<ComparisonResult> compareMultiSource(
            @RequestParam String sourceOneType,
            @RequestParam String sourceOneIdentifier,
            @RequestParam String sourceTwoType,
            @RequestParam String sourceTwoIdentifier,
            @RequestBody DualSourceComparisonRequest request) {

        ComparisonResult result = directComparisonService.compareDataSources(
                sourceOneType,
                sourceOneIdentifier,
                request.getSourceOneParams(),
                sourceTwoType,
                sourceTwoIdentifier,
                request.getSourceTwoParams(),
                request.getComparisonConfig()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Get a previously executed comparison result
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComparisonResult> getComparisonResult(@PathVariable UUID id) {
        ComparisonResult result = directComparisonService.getComparisonResult(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Get specific differences from a comparison result
     */
    @GetMapping("/{id}/differences")
    public ResponseEntity<List<RecordDifference>> getDifferences(
            @PathVariable UUID id,
            @RequestParam(required = false) String differenceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int pageSize) {
        
        ComparisonResult result = directComparisonService.getComparisonResult(id);
        
        List<RecordDifference> differences = result.getDifferences().stream()
                .filter(diff -> differenceType == null || diff.getDifferenceType().toString().equals(differenceType))
                .skip((long) page * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(differences);
    }

    /**
     * Get summary statistics of a comparison result
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable UUID id) {
        ComparisonResult result = directComparisonService.getComparisonResult(id);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", result.getId());
        summary.put("comparisonJobId", result.getComparisonJobId());
        summary.put("totalRecords", result.getTotalRecords());
        summary.put("matchedRecords", result.getMatchedRecords());
        summary.put("mismatchedRecords", result.getMismatchedRecords());
        summary.put("sourceOnlyRecords", result.getSourceOnlyRecords());
        summary.put("targetOnlyRecords", result.getTargetOnlyRecords());
        summary.put("diffCount", result.getDifferences().size());
        
        return ResponseEntity.ok(summary);
    }
}
