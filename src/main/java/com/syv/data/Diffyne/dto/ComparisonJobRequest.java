package com.syv.data.Diffyne.dto;

import lombok.Data;
import com.syv.data.Diffyne.config.ComparisonConfig;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ComparisonJobRequest {
    private UUID sourceSnapshotId;      // ID of the source snapshot to compare
    private UUID targetSnapshotId;      // ID of the target snapshot to compare against
    private String name;                // Optional name for this comparison job
    private ComparisonConfig config;    // Configuration settings for the comparison
    private LocalDateTime scheduledTime; // Optional time to schedule execution (null = immediate)
}
