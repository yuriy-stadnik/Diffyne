package com.syv.data.Diffyne.service;

import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public interface ComparisonService {
    ComparisonJob createComparisonJob(UUID sourceSnapshotId, UUID targetSnapshotId, ComparisonConfig config);
    ComparisonJob scheduleComparisonJob(ComparisonJob job, LocalDateTime scheduledTime);
    ComparisonJob executeComparisonJob(UUID jobId);
    ComparisonJob getComparisonJob(UUID jobId);
    ComparisonResult getComparisonResult(UUID jobId);
    List<RecordDifference> getDifferences(UUID resultId, int page, int size);
}