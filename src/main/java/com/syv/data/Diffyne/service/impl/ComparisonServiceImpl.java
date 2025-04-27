package com.syv.data.Diffyne.service.impl;

import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.repository.ComparisonJobRepository;
import com.syv.data.Diffyne.repository.ComparisonResultRepository;
import com.syv.data.Diffyne.repository.SnapshotRepository;
import com.syv.data.Diffyne.service.ComparisonEngine;
import com.syv.data.Diffyne.service.ComparisonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComparisonServiceImpl implements ComparisonService {
    
    private static final Logger logger = LoggerFactory.getLogger(ComparisonServiceImpl.class);
    
    @Autowired
    private ComparisonJobRepository jobRepository;
    
    @Autowired
    private ComparisonResultRepository resultRepository;
    
    @Autowired
    private SnapshotRepository snapshotRepository;
    
    @Autowired
    private ComparisonEngine comparisonEngine;
    
    @Override
    @Transactional
    public ComparisonJob createComparisonJob(UUID sourceSnapshotId, UUID targetSnapshotId, ComparisonConfig config) {
        // Validate that both snapshots exist
        if (!snapshotRepository.existsById(sourceSnapshotId) || !snapshotRepository.existsById(targetSnapshotId)) {
            throw new IllegalArgumentException("Source or target snapshot does not exist");
        }
        
        // Create the job
        ComparisonJob job = new ComparisonJob();
        job.setId(UUID.randomUUID());
        job.setSourceSnapshotId(sourceSnapshotId);
        job.setTargetSnapshotId(targetSnapshotId);
        job.setConfig(config);
        job.setStatus(ComparisonStatus.QUEUED);
        
        // Set default name if not provided
        if (job.getName() == null) {
            job.setName("Comparison Job " + job.getId());
        }
        
        // Save the job
        return jobRepository.save(job);
    }
    
    @Override
    @Transactional
    public ComparisonJob scheduleComparisonJob(ComparisonJob job, LocalDateTime scheduledTime) {
        if (scheduledTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }
        
        job.setScheduledTime(scheduledTime);
        job.setStatus(ComparisonStatus.SCHEDULED);
        
        return jobRepository.save(job);
    }
    
    @Override
    @Transactional
    public ComparisonJob executeComparisonJob(UUID jobId) {
        // Get the job
        ComparisonJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        try {
            // Update status to in progress
            job.setStatus(ComparisonStatus.IN_PROGRESS);
            job.setExecutionTime(LocalDateTime.now());
            jobRepository.save(job);
            
            // Get the snapshots
            DataSnapshot sourceSnapshot = snapshotRepository.findById(job.getSourceSnapshotId())
                    .orElseThrow(() -> new IllegalArgumentException("Source snapshot not found"));
            
            DataSnapshot targetSnapshot = snapshotRepository.findById(job.getTargetSnapshotId())
                    .orElseThrow(() -> new IllegalArgumentException("Target snapshot not found"));
            
            // Run the comparison
            ComparisonResult result = comparisonEngine.compare(sourceSnapshot, targetSnapshot, job.getConfig());
            
            // Associate with job
            result.setComparisonJobId(job.getId());
            
            // Save the result
            result = resultRepository.save(result);
            
            // Update the job
            job.setResult(result);
            job.setStatus(ComparisonStatus.COMPLETED);
            
            return jobRepository.save(job);
        } catch (Exception e) {
            logger.error("Error executing comparison job: {}", jobId, e);
            job.setStatus(ComparisonStatus.FAILED);
            return jobRepository.save(job);
        }
    }
    
    @Override
    public ComparisonJob getComparisonJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
    }
    
    @Override
    public ComparisonResult getComparisonResult(UUID jobId) {
        return resultRepository.findByComparisonJobId(jobId);
    }
    
    @Override
    public List<RecordDifference> getDifferences(UUID resultId, int page, int size) {
        ComparisonResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Result not found: " + resultId));
        
        // Since we're using a Java collection rather than a database query,
        // implement manual paging logic
        return result.getDifferences().stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
    }
}