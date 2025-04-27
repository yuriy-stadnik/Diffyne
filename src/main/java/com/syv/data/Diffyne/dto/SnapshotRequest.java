package com.syv.data.Diffyne.dto;

import lombok.Data;
import java.util.Map;

@Data
public class SnapshotRequest {
    private String sourceType;         // Type of data source (RDBMS, Kafka, Salesforce, REST_API)
    private String sourceIdentifier;   // Specific identifier for the source (DB name, topic, etc.)
    private String name;               // Human-readable name for this snapshot
    private Map<String, Object> params; // Additional parameters specific to the source type
}
