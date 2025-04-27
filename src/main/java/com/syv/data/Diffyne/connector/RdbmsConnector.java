package com.syv.data.Diffyne.connector;

import com.syv.data.Diffyne.model.DataRecord;
import com.syv.data.Diffyne.model.DataSnapshot;
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

/**
 * This connector uses REST APIs to access database data instead of direct JDBC connections.
 * It assumes the database data is exposed through a REST API gateway.
 */
@Component
public class RdbmsConnector implements DataSourceConnector {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    public DataSnapshot extractSnapshot(String sourceIdentifier, Map<String, Object> extractionParams) {
        DataSnapshot snapshot = new DataSnapshot();
        snapshot.setId(UUID.randomUUID());
        snapshot.setSourceType("RDBMS");
        snapshot.setSourceIdentifier(sourceIdentifier);
        snapshot.setSnapshotTime(LocalDateTime.now());
        snapshot.setMetadata(extractionParams);
        snapshot.setStatus(SnapshotStatus.IN_PROGRESS);
        
        try {
            // Extract parameters
            String dbServiceUrl = (String) extractionParams.getOrDefault("dbServiceUrl", "https://api.example.com/db");
            String query = (String) extractionParams.getOrDefault("query", "SELECT * FROM " + sourceIdentifier);
            String primaryKeyField = (String) extractionParams.getOrDefault("primaryKeyField", "id");
            
            // Create request with headers
            HttpHeaders headers = new HttpHeaders();
            if (extractionParams.containsKey("authToken")) {
                headers.set("Authorization", "Bearer " + extractionParams.get("authToken"));
            }
            
            // Add custom headers if provided
            if (extractionParams.containsKey("headers")) {
                Map<String, String> customHeaders = (Map<String, String>) extractionParams.get("headers");
                customHeaders.forEach(headers::set);
            }
            
            // Build request body with query
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("database", sourceIdentifier);
            requestBody.put("query", query);
            
            // Execute REST call to database service
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<List> response = restTemplate.exchange(
                    dbServiceUrl,
                    HttpMethod.POST,
                    entity,
                    List.class
            );
            
            // Process result
            List<Map<String, Object>> records = response.getBody();
            Set<DataRecord> dataRecords = new HashSet<>();
            
            if (records != null) {
                for (Map<String, Object> record : records) {
                    DataRecord dataRecord = new DataRecord();
                    dataRecord.setId(UUID.randomUUID());
                    dataRecord.setData(record);
                    
                    // Extract primary key
                    if (record.containsKey(primaryKeyField)) {
                        dataRecord.setPrimaryKeyValue(String.valueOf(record.get(primaryKeyField)));
                    } else {
                        dataRecord.setPrimaryKeyValue(UUID.randomUUID().toString());
                    }
                    
                    dataRecords.add(dataRecord);
                }
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
    
    @Override
    public boolean validateConnection(String sourceIdentifier) {
        try {
            // Use catalog API to check if database exists
            String catalogUrl = "https://api.example.com/dbcatalog";
            Map<String, String> request = Map.of("database", sourceIdentifier);
            Boolean result = restTemplate.postForObject(catalogUrl, request, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Set<String> getAvailableSources() {
        try {
            // Get list of databases from catalog service
            String catalogUrl = "https://api.example.com/dbcatalog/list";
            List<String> databases = restTemplate.getForObject(catalogUrl, List.class);
            return databases != null ? new HashSet<>(databases) : new HashSet<>();
        } catch (Exception e) {
            return new HashSet<>();
        }
    }
}
