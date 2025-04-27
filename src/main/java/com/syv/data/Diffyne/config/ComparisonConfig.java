package com.syv.data.Diffyne.config;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ComparisonConfig {
    private Set<String> keyFields;
    private Set<String> fieldsToCompare;
    private Map<String, Double> toleranceLevels; // For numeric fields
    private boolean ignoreCase; // For string fields
    private boolean ignoreWhitespace;
    private Set<String> excludeFields;
}
