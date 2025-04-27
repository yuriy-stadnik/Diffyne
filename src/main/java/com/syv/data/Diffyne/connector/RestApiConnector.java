package com.syv.data.Diffyne.connector;


import com.syv.data.Diffyne.model.DataSnapshot;
import com.syv.data.Diffyne.model.DataRecord;
import com.syv.data.Diffyne.model.SnapshotStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class RestApiConnector implements DataSourceConnector {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public DataSnapshot extractSnapshot(String sourceIdentifier, Map<String, Object> extractionParams) {
        DataSnapshot snapshot = new DataSnapshot();
        snapshot.setId(UUID.randomUUID());
        snapshot.setSourceType("REST_API");
        snapshot.setSourceIdentifier(sourceIdentifier);
        snapshot.setSnapshotTime(LocalDateTime.now());
        snapshot.setMetadata(extractionParams);
        snapshot.setStatus(SnapshotStatus.IN_PROGRESS);

        try {
            // Extract API details from params
            String url = (String) extractionParams.getOrDefault("url", sourceIdentifier);
            String primaryKeyField = (String) extractionParams.getOrDefault("primaryKeyField", "id");
            String recordsPath = (String) extractionParams.getOrDefault("recordsPath", "$");

            // Handle authentication if provided
            HttpHeaders headers = new HttpHeaders();
            if (extractionParams.containsKey("authToken")) {
                headers.set("Authorization", "Bearer " + extractionParams.get("authToken"));
            }

            // Add any custom headers
            if (extractionParams.containsKey("headers")) {
                Map<String, String> customHeaders = (Map<String, String>) extractionParams.get("headers");
                customHeaders.forEach(headers::set);
            }

            // Make the REST call
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Object.class);

            // Process the response
            Object responseBody = response.getBody();
            List<Map<String, Object>> records = extractRecordsFromResponse(responseBody, recordsPath);

            // Convert to DataRecords
            Set<DataRecord> dataRecords = new HashSet<>();
            for (Map<String, Object> record : records) {
                DataRecord dataRecord = new DataRecord();
                dataRecord.setId(UUID.randomUUID());
                dataRecord.setData(record);

                // Extract primary key value
                if (record.containsKey(primaryKeyField)) {
                    dataRecord.setPrimaryKeyValue(String.valueOf(record.get(primaryKeyField)));
                } else {
                    dataRecord.setPrimaryKeyValue(UUID.randomUUID().toString());
                }

                dataRecords.add(dataRecord);
            }

            snapshot.setRecords(dataRecords);
            snapshot.setStatus(SnapshotStatus.COMPLETED);

        } catch (Exception e) {
            snapshot.setStatus(SnapshotStatus.FAILED);
            Map<String, Object> metadata = snapshot.getMetadata();
            metadata.put("error", e.getMessage());
            snapshot.setMetadata(metadata);
        }

        return snapshot;
    }

    private List<Map<String, Object>> extractRecordsFromResponse(Object responseBody, String recordsPath) {
        if (responseBody instanceof List) {
            // If response is already a list
            return convertToMapList((List<?>) responseBody);
        } else if (responseBody instanceof Map) {
            // If response is a map, try to navigate to records using the path
            Map<String, Object> responseMap = (Map<String, Object>) responseBody;

            if ("$".equals(recordsPath)) {
                // Treat the entire response as a single record
                return Collections.singletonList(responseMap);
            }

            // Navigate through the path to find the records array
            String[] pathParts = recordsPath.split("\\.");
            Object current = responseMap;

            for (String part : pathParts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else {
                    throw new IllegalArgumentException("Cannot navigate path " + recordsPath);
                }
            }

            if (current instanceof List) {
                return convertToMapList((List<?>) current);
            }
        }

        throw new IllegalArgumentException("Unexpected response format");
    }

    private List<Map<String, Object>> convertToMapList(List<?> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                result.add((Map<String, Object>) item);
            }
        }
        return result;
    }

    @Override
    public boolean validateConnection(String sourceIdentifier) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("url", sourceIdentifier);
            restTemplate.getForObject(sourceIdentifier, Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Set<String> getAvailableSources() {
        // REST APIs don't have a fixed list of available sources
        // This could be implemented by maintaining a registry of known API endpoints
        return new HashSet<>();
    }
}
