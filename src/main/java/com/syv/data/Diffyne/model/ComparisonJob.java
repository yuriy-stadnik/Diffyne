package com.syv.data.Diffyne.model;

import com.syv.data.Diffyne.config.ComparisonConfig;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ComparisonJob {
    private UUID id;
    private UUID sourceSnapshotId;
    private UUID targetSnapshotId;
    private String name;
    private ComparisonConfig config;
    private LocalDateTime scheduledTime;
    private LocalDateTime executionTime;
    private ComparisonStatus status;
    private ComparisonResult result;
}
