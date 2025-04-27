package com.syv.data.Diffyne.service.impl;


import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.repository.ComparisonResultRepository;
import com.syv.data.Diffyne.service.ComparisonEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComparisonEngineImpl implements ComparisonEngine {

    @Autowired
    private ComparisonResultRepository resultRepository;

    @Override
    public ComparisonResult compare(DataSnapshot sourceSnapshot, DataSnapshot targetSnapshot, ComparisonConfig config) {
        // Create a new result object
        ComparisonResult result = new ComparisonResult();
        result.setId(UUID.randomUUID());

        // Initialize counters
        int totalRecords = 0;
        int matchedRecords = 0;
        int mismatchedRecords = 0;
        int sourceOnlyRecords = 0;
        int targetOnlyRecords = 0;

        // Store differences
        Set<RecordDifference> differences = new HashSet<>();

        // Get key fields from config
        Set<String> keyFields = config.getKeyFields();
        if (keyFields == null || keyFields.isEmpty()) {
            // Default to using primary key
            keyFields = Collections.singleton("primaryKeyValue");
        }

        // Index target records by key for efficient lookup
        Map<String, DataRecord> targetRecordsMap = createRecordIndex(targetSnapshot.getRecords(), keyFields);

        // Compare each source record with target
        for (DataRecord sourceRecord : sourceSnapshot.getRecords()) {
            totalRecords++;

            // Create composite key for this record
            String compositeKey = createCompositeKey(sourceRecord, keyFields);

            // Look for matching record in target
            DataRecord targetRecord = targetRecordsMap.get(compositeKey);

            if (targetRecord == null) {
                // Record exists only in source
                sourceOnlyRecords++;

                RecordDifference difference = new RecordDifference();
                difference.setPrimaryKeyValue(sourceRecord.getPrimaryKeyValue());
                difference.setDifferenceType(DifferenceType.SOURCE_ONLY);
                differences.add(difference);
            } else {
                // Record exists in both sources, compare fields
                Set<String> fieldsToCompare = determineFieldsToCompare(config, sourceRecord, targetRecord);
                boolean recordMatches = true;

                for (String field : fieldsToCompare) {
                    Object sourceValue = sourceRecord.getData().get(field);
                    Object targetValue = targetRecord.getData().get(field);

                    if (!areValuesEqual(sourceValue, targetValue, field, config)) {
                        // Values don't match
                        recordMatches = false;

                        RecordDifference difference = new RecordDifference();
                        difference.setPrimaryKeyValue(sourceRecord.getPrimaryKeyValue());
                        difference.setFieldName(field);
                        difference.setSourceValue(sourceValue);
                        difference.setTargetValue(targetValue);
                        difference.setDifferenceType(DifferenceType.VALUE_MISMATCH);
                        differences.add(difference);
                    }
                }

                if (recordMatches) {
                    matchedRecords++;
                } else {
                    mismatchedRecords++;
                }

                // Remove this record from the target map to track what's left
                targetRecordsMap.remove(compositeKey);
            }
        }

        // Any records remaining in the target map exist only in the target
        for (DataRecord targetOnlyRecord : targetRecordsMap.values()) {
            targetOnlyRecords++;
            totalRecords++;

            RecordDifference difference = new RecordDifference();
            difference.setPrimaryKeyValue(targetOnlyRecord.getPrimaryKeyValue());
            difference.setDifferenceType(DifferenceType.TARGET_ONLY);
            differences.add(difference);
        }

        // Populate the result object
        result.setTotalRecords(totalRecords);
        result.setMatchedRecords(matchedRecords);
        result.setMismatchedRecords(mismatchedRecords);
        result.setSourceOnlyRecords(sourceOnlyRecords);
        result.setTargetOnlyRecords(targetOnlyRecords);
        result.setDifferences(differences);

        return result;
    }

    private Map<String, DataRecord> createRecordIndex(Set<DataRecord> records, Set<String> keyFields) {
        Map<String, DataRecord> recordsMap = new HashMap<>();

        for (DataRecord record : records) {
            String compositeKey = createCompositeKey(record, keyFields);
            recordsMap.put(compositeKey, record);
        }

        return recordsMap;
    }

    private String createCompositeKey(DataRecord record, Set<String> keyFields) {
        // For records where we use the primary key value directly
        if (keyFields.size() == 1 && keyFields.contains("primaryKeyValue")) {
            return record.getPrimaryKeyValue();
        }

        // For records where we combine multiple fields as the key
        StringBuilder keyBuilder = new StringBuilder();

        for (String field : keyFields) {
            Object value = record.getData().get(field);
            keyBuilder.append(value != null ? value.toString() : "null").append(":");
        }

        return keyBuilder.toString();
    }

    private Set<String> determineFieldsToCompare(ComparisonConfig config, DataRecord sourceRecord, DataRecord targetRecord) {
        // If specific fields to compare are configured, use those
        if (config.getFieldsToCompare() != null && !config.getFieldsToCompare().isEmpty()) {
            return config.getFieldsToCompare();
        }

        // Otherwise, compare all fields present in both records, except excluded ones
        Set<String> excludeFields = config.getExcludeFields() != null ? config.getExcludeFields() : Collections.emptySet();

        // Get all field names from both records
        Set<String> allFields = new HashSet<>();
        allFields.addAll(sourceRecord.getData().keySet());
        allFields.addAll(targetRecord.getData().keySet());

        // Remove key fields and excluded fields
        allFields.removeAll(config.getKeyFields());
        allFields.removeAll(excludeFields);

        return allFields;
    }

    private boolean areValuesEqual(Object value1, Object value2, String fieldName, ComparisonConfig config) {
        // If both values are null, they're equal
        if (value1 == null && value2 == null) {
            return true;
        }

        // If only one value is null, they're not equal
        if (value1 == null || value2 == null) {
            return false;
        }

        // For numeric values, check tolerance
        if (value1 instanceof Number && value2 instanceof Number) {
            double num1 = ((Number) value1).doubleValue();
            double num2 = ((Number) value2).doubleValue();

            // Check if this field has a specific tolerance level
            if (config.getToleranceLevels() != null && config.getToleranceLevels().containsKey(fieldName)) {
                double tolerance = config.getToleranceLevels().get(fieldName);
                return Math.abs(num1 - num2) <= tolerance;
            }

            // Default exact numeric comparison
            return num1 == num2;
        }

        // For string values, apply string comparison rules
        if (value1 instanceof String && value2 instanceof String) {
            String str1 = (String) value1;
            String str2 = (String) value2;

            // Apply ignore case if configured
            if (config.isIgnoreCase()) {
                str1 = str1.toLowerCase();
                str2 = str2.toLowerCase();
            }

            // Apply ignore whitespace if configured
            if (config.isIgnoreWhitespace()) {
                str1 = str1.trim();
                str2 = str2.trim();
            }

            return str1.equals(str2);
        }

        // Default comparison using equals()
        return value1.equals(value2);
    }
}