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
                
                // Set primary key information
                String primaryKeyField = keyFields.iterator().next();
                difference.setPrimaryKeyName(primaryKeyField);
                
                // Group related differences
                Set<RecordDifference> childDifferences = new HashSet<>();
                for (String field : sourceRecord.getData().keySet()) {
                    RecordDifference childDiff = new RecordDifference();
                    childDiff.setFieldName(field);
                    childDiff.setSourceValue(sourceRecord.getData().get(field));
                    childDiff.setTargetValue(null);
                    childDiff.setDifferenceType(DifferenceType.SOURCE_ONLY);
                    childDiff.setPrimaryKey(keyFields.contains(field));
                    if (keyFields.contains(field)) {
                        childDiff.setPrimaryKeyName(field);
                    }
                    childDifferences.add(childDiff);
                }
                difference.setDifferences(childDifferences);
                
                differences.add(difference);
            } else {
                // Record exists in both sources, compare fields
                Set<String> fieldsToCompare = determineFieldsToCompare(config, sourceRecord, targetRecord);
                boolean recordMatches = true;

                for (String sourceField : fieldsToCompare) {
                    // Get the corresponding target field using the mapping
                    String targetField = sourceField;
                    if (config.getFieldsToCompare() != null && config.getFieldsToCompare().containsKey(sourceField)) {
                        targetField = config.getFieldsToCompare().get(sourceField);
                    }
                    
                    Object sourceValue = sourceRecord.getData().get(sourceField);
                    Object targetValue = targetRecord.getData().get(targetField);

                    if (!areValuesEqual(sourceValue, targetValue, sourceField, config)) {
                        // Values don't match
                        recordMatches = false;

                        // Check if we already have a parent difference record for this primary key
                        RecordDifference parentDifference = null;
                        for (RecordDifference diff : differences) {
                            if (diff.getDifferenceType() == DifferenceType.VALUE_MISMATCH && 
                                diff.getPrimaryKeyValue() != null && 
                                diff.getPrimaryKeyValue().equals(sourceRecord.getPrimaryKeyValue())) {
                                parentDifference = diff;
                                break;
                            }
                        }
                        
                        // If no parent record found, create one
                        if (parentDifference == null) {
                            parentDifference = new RecordDifference();
                            parentDifference.setPrimaryKeyValue(sourceRecord.getPrimaryKeyValue());
                            parentDifference.setDifferenceType(DifferenceType.VALUE_MISMATCH);
                            parentDifference.setPrimaryKeyName(keyFields.iterator().next());
                            parentDifference.setDifferences(new HashSet<>());
                            differences.add(parentDifference);
                        }
                        
                        // Create child difference for this field
                        RecordDifference fieldDifference = new RecordDifference();
                        fieldDifference.setPrimaryKeyValue(sourceRecord.getPrimaryKeyValue());
                        fieldDifference.setFieldName(sourceField);
                        fieldDifference.setSourceValue(sourceValue);
                        fieldDifference.setTargetValue(targetValue);
                        fieldDifference.setDifferenceType(DifferenceType.VALUE_MISMATCH);
                        
                        // Store the mapped field name if different
                        if (!sourceField.equals(targetField)) {
                            fieldDifference.setTargetFieldName(targetField);
                        }
                        
                        // Set primary key information
                        for (String keyField : keyFields) {
                            if (keyField.equals(sourceField)) {
                                fieldDifference.setPrimaryKey(true);
                                fieldDifference.setPrimaryKeyName(sourceField);
                                break;
                            }
                        }
                        
                        // Add to parent's differences collection
                        parentDifference.getDifferences().add(fieldDifference);
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
            
            // Set primary key information
            String primaryKeyField = keyFields.iterator().next();
            difference.setPrimaryKeyName(primaryKeyField);
            
            // Group related differences
            Set<RecordDifference> childDifferences = new HashSet<>();
            for (String field : targetOnlyRecord.getData().keySet()) {
                RecordDifference childDiff = new RecordDifference();
                childDiff.setFieldName(field);
                childDiff.setSourceValue(null);
                childDiff.setTargetValue(targetOnlyRecord.getData().get(field));
                childDiff.setDifferenceType(DifferenceType.TARGET_ONLY);
                childDiff.setPrimaryKey(keyFields.contains(field));
                if (keyFields.contains(field)) {
                    childDiff.setPrimaryKeyName(field);
                }
                childDifferences.add(childDiff);
            }
            difference.setDifferences(childDifferences);
            
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
            return config.getFieldsToCompare().keySet();
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