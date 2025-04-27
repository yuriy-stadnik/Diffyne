package com.syv.data.Diffyne.model;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class ComparisonResult {
    private UUID id;
    private UUID comparisonJobId;
    private int totalRecords;
    private int matchedRecords;
    private int mismatchedRecords;
    private int sourceOnlyRecords;
    private int targetOnlyRecords;
    private Set<RecordDifference> differences;
}
