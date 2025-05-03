package com.syv.data.Diffyne.model;

import lombok.Data;
import java.util.Set;
import java.util.HashSet;

@Data
public class RecordDifference {
    private String primaryKeyValue;
    private String fieldName;
    private String targetFieldName; // Used when field names differ between sources
    private Object sourceValue;
    private Object targetValue;
    private DifferenceType differenceType;
    private String primaryKeyName;
    private boolean primaryKey;
    private Set<RecordDifference> differences = new HashSet<>();
}
