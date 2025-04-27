package com.syv.data.Diffyne.controller;

import com.syv.data.Diffyne.dto.ComparisonJobRequest;
import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/comparisons")
public class ComparisonController {
    @Autowired
    private ComparisonService comparisonService;

    /**
     * Create a new comparison job
     */
    @PostMapping
    public ResponseEntity<ComparisonJob> createJob(@RequestBody ComparisonJobRequest request) {
        ComparisonJob job = comparisonService.createComparisonJob(
                request.getSourceSnapshotId(),
                request.getTargetSnapshotId(),
                request.getConfig());
        
        // If scheduled time is provided, schedule the job
        if (request.getScheduledTime() != null) {
            job = comparisonService.scheduleComparisonJob(job, request.getScheduledTime());
        }
        
        return ResponseEntity.ok(job);
    }

    /**
     * Execute a comparison job immediately
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ComparisonJob> executeJob(@PathVariable UUID id) {
        ComparisonJob job = comparisonService.executeComparisonJob(id);
        return ResponseEntity.ok(job);
    }

    /**
     * Schedule a comparison job for future execution
     */
    @PostMapping("/{id}/schedule")
    public ResponseEntity<ComparisonJob> scheduleJob(
            @PathVariable UUID id, 
            @RequestParam LocalDateTime scheduledTime) {
        
        // First get the job
        ComparisonJob job = comparisonService.getComparisonJob(id);
        
        // Then schedule it
        job = comparisonService.scheduleComparisonJob(job, scheduledTime);
        
        return ResponseEntity.ok(job);
    }

    /**
     * Get a comparison job by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComparisonJob> getJob(@PathVariable UUID id) {
        ComparisonJob job = comparisonService.getComparisonJob(id);
        return ResponseEntity.ok(job);
    }

    /**
     * Get the result of a comparison job
     */
    @GetMapping("/{id}/result")
    public ResponseEntity<ComparisonResult> getResult(@PathVariable UUID id) {
        ComparisonResult result = comparisonService.getComparisonResult(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get differences from a comparison result
     */
    @GetMapping("/{id}/differences")
    public ResponseEntity<List<RecordDifference>> getDifferences(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<RecordDifference> differences = comparisonService.getDifferences(id, page, size);
        return ResponseEntity.ok(differences);
    }
    
    /**
     * Get summary statistics about a comparison job result
     */
    @GetMapping("/{id}/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@PathVariable UUID id) {
        ComparisonResult result = comparisonService.getComparisonResult(id);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("jobId", id);
        summary.put("resultId", result.getId());
        summary.put("totalRecords", result.getTotalRecords());
        summary.put("matchedRecords", result.getMatchedRecords());
        summary.put("mismatchedRecords", result.getMismatchedRecords());
        summary.put("sourceOnlyRecords", result.getSourceOnlyRecords());
        summary.put("targetOnlyRecords", result.getTargetOnlyRecords());
        summary.put("differenceCount", result.getDifferences().size());
        
        return ResponseEntity.ok(summary);
    }
}
