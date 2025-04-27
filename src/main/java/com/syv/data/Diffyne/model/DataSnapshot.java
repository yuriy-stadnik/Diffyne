package com.syv.data.Diffyne.model;


import com.syv.data.Diffyne.config.ComparisonConfig;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
public class DataSnapshot {
    private UUID id;
    private String name;
    private String sourceType; // RDBMS, Kafka, Salesforce, etc.
    private String sourceIdentifier; // Specific DB name, Kafka topic, etc.
    private LocalDateTime snapshotTime;
    private Map<String, Object> metadata;
    private Set<DataRecord> records;
    private SnapshotStatus status;
}

