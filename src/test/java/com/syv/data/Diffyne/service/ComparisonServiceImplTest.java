package com.syv.data.Diffyne.service;

import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.repository.ComparisonJobRepository;
import com.syv.data.Diffyne.repository.ComparisonResultRepository;
import com.syv.data.Diffyne.repository.SnapshotRepository;
import com.syv.data.Diffyne.service.impl.ComparisonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ComparisonServiceImplTest {

    @Mock
    private ComparisonJobRepository jobRepository;

    @Mock
    private ComparisonResultRepository resultRepository;

    @Mock
    private SnapshotRepository snapshotRepository;

    @Mock
    private ComparisonEngine comparisonEngine;

    @InjectMocks
    private ComparisonServiceImpl comparisonService;

    private UUID sourceSnapshotId;
    private UUID targetSnapshotId;
    private UUID jobId;
    private UUID resultId;
    private ComparisonConfig config;
    private DataSnapshot sourceSnapshot;
    private DataSnapshot targetSnapshot;
    private ComparisonJob job;
    private ComparisonResult result;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        sourceSnapshotId = UUID.randomUUID();
        targetSnapshotId = UUID.randomUUID();
        jobId = UUID.randomUUID();
        resultId = UUID.randomUUID();

        // Create config
        config = new ComparisonConfig();
        config.setKeyFields(Collections.singleton("id"));
        config.setFieldsToCompare(new HashSet<>(Arrays.asList("name", "value")));

        // Create source snapshot
        sourceSnapshot = new DataSnapshot();
        sourceSnapshot.setId(sourceSnapshotId);
        sourceSnapshot.setName("Source Snapshot");

        // Create target snapshot
        targetSnapshot = new DataSnapshot();
        targetSnapshot.setId(targetSnapshotId);
        targetSnapshot.setName("Target Snapshot");

        // Create comparison job
        job = new ComparisonJob();
        job.setId(jobId);
        job.setSourceSnapshotId(sourceSnapshotId);
        job.setTargetSnapshotId(targetSnapshotId);
        job.setConfig(config);
        job.setStatus(ComparisonStatus.QUEUED);

        // Create result
        result = new ComparisonResult();
        result.setId(resultId);
        result.setComparisonJobId(jobId);
        result.setTotalRecords(10);
        result.setMatchedRecords(5);
        result.setMismatchedRecords(3);
        result.setSourceOnlyRecords(1);
        result.setTargetOnlyRecords(1);

        // Setup differences
        Set<RecordDifference> differences = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            RecordDifference diff = new RecordDifference();
            diff.setPrimaryKeyValue("key" + i);
            diff.setFieldName("field" + i);
            diff.setSourceValue("srcVal" + i);
            diff.setTargetValue("tgtVal" + i);
            diff.setDifferenceType(DifferenceType.VALUE_MISMATCH);
            differences.add(diff);
        }
        result.setDifferences(differences);
    }

    @Test
    void createComparisonJob_Success() {
        // Setup
        when(snapshotRepository.existsById(sourceSnapshotId)).thenReturn(true);
        when(snapshotRepository.existsById(targetSnapshotId)).thenReturn(true);
        when(jobRepository.save(any(ComparisonJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        ComparisonJob createdJob = comparisonService.createComparisonJob(sourceSnapshotId, targetSnapshotId, config);

        // Verify
        assertNotNull(createdJob);
        assertEquals(sourceSnapshotId, createdJob.getSourceSnapshotId());
        assertEquals(targetSnapshotId, createdJob.getTargetSnapshotId());
        assertEquals(ComparisonStatus.QUEUED, createdJob.getStatus());
        verify(jobRepository).save(any(ComparisonJob.class));
    }

    @Test
    void createComparisonJob_MissingSnapshot() {
        // Setup
        when(snapshotRepository.existsById(sourceSnapshotId)).thenReturn(false);
        when(snapshotRepository.existsById(targetSnapshotId)).thenReturn(true);

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.createComparisonJob(sourceSnapshotId, targetSnapshotId, config);
        });
    }

    @Test
    void scheduleComparisonJob_Success() {
        // Setup
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        when(jobRepository.save(any(ComparisonJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        ComparisonJob scheduledJob = comparisonService.scheduleComparisonJob(job, futureTime);

        // Verify
        assertNotNull(scheduledJob);
        assertEquals(futureTime, scheduledJob.getScheduledTime());
        assertEquals(ComparisonStatus.SCHEDULED, scheduledJob.getStatus());
        verify(jobRepository).save(job);
    }

    @Test
    void scheduleComparisonJob_PastTime() {
        // Setup
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.scheduleComparisonJob(job, pastTime);
        });
    }

    @Test
    void executeComparisonJob_Success() {
        // Setup
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(snapshotRepository.findById(sourceSnapshotId)).thenReturn(Optional.of(sourceSnapshot));
        when(snapshotRepository.findById(targetSnapshotId)).thenReturn(Optional.of(targetSnapshot));
        when(comparisonEngine.compare(any(), any(), any())).thenReturn(result);
        when(resultRepository.save(any(ComparisonResult.class))).thenReturn(result);
        when(jobRepository.save(any(ComparisonJob.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execute
        ComparisonJob executedJob = comparisonService.executeComparisonJob(jobId);

        // Verify
        assertNotNull(executedJob);
        assertEquals(ComparisonStatus.COMPLETED, executedJob.getStatus());
        assertNotNull(executedJob.getExecutionTime());
        assertEquals(result, executedJob.getResult());
        verify(comparisonEngine).compare(sourceSnapshot, targetSnapshot, config);
        verify(resultRepository).save(result);
        verify(jobRepository, times(2)).save(job); // Once to set IN_PROGRESS, once to set COMPLETED
    }

    @Test
    void executeComparisonJob_JobNotFound() {
        // Setup
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.executeComparisonJob(jobId);
        });
    }

    @Test
    void getComparisonJob_Success() {
        // Setup
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        // Execute
        ComparisonJob retrievedJob = comparisonService.getComparisonJob(jobId);

        // Verify
        assertNotNull(retrievedJob);
        assertEquals(jobId, retrievedJob.getId());
        verify(jobRepository).findById(jobId);
    }
    
    @Test
    void getComparisonJob_NotFound() {
        // Setup
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.getComparisonJob(jobId);
        });
    }

    @Test
    void getComparisonResult_Success() {
        // Setup
        when(resultRepository.findByComparisonJobId(jobId)).thenReturn(result);

        // Execute
        ComparisonResult retrievedResult = comparisonService.getComparisonResult(jobId);

        // Verify
        assertNotNull(retrievedResult);
        assertEquals(resultId, retrievedResult.getId());
        assertEquals(jobId, retrievedResult.getComparisonJobId());
        verify(resultRepository).findByComparisonJobId(jobId);
    }

    @Test
    void getDifferences_Success() {
        // Setup
        when(resultRepository.findById(resultId)).thenReturn(Optional.of(result));

        // Execute
        List<RecordDifference> differences = comparisonService.getDifferences(resultId, 0, 3);

        // Verify
        assertNotNull(differences);
        assertEquals(3, differences.size());
        verify(resultRepository).findById(resultId);
    }

    @Test
    void getDifferences_ResultNotFound() {
        // Setup
        when(resultRepository.findById(resultId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.getDifferences(resultId, 0, 3);
        });
    }
}