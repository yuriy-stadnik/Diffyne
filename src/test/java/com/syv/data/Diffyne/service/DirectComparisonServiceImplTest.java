package com.syv.data.Diffyne.service;

import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.connector.ConnectorRegistry;
import com.syv.data.Diffyne.connector.DataSourceConnector;
import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.repository.ComparisonResultRepository;
import com.syv.data.Diffyne.service.impl.DirectComparisonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DirectComparisonServiceImplTest {

    @Mock
    private ConnectorRegistry connectorRegistry;

    @Mock
    private ComparisonEngine comparisonEngine;

    @Mock
    private ComparisonResultRepository resultRepository;

    @Mock
    private DataSourceConnector restApiConnector;

    @Mock
    private DataSourceConnector rdbmsConnector;

    @InjectMocks
    private DirectComparisonServiceImpl directComparisonService;

    private ComparisonConfig config;
    private DataSnapshot sourceSnapshot;
    private DataSnapshot targetSnapshot;
    private ComparisonResult comparisonResult;
    private UUID resultId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize test data
        resultId = UUID.randomUUID();

        // Create config
        config = new ComparisonConfig();
        config.setKeyFields(Collections.singleton("id"));
        config.setFieldsToCompare(new HashSet<>(Arrays.asList("name", "value")));

        // Create source snapshot
        sourceSnapshot = new DataSnapshot();
        sourceSnapshot.setId(UUID.randomUUID());
        sourceSnapshot.setName("Source API Snapshot");
        sourceSnapshot.setSourceType("REST_API");
        sourceSnapshot.setSourceIdentifier("https://api.example.com/source");
        sourceSnapshot.setSnapshotTime(LocalDateTime.now());
        sourceSnapshot.setStatus(SnapshotStatus.COMPLETED);
        
        // Create some records for source
        Set<DataRecord> sourceRecords = new HashSet<>();
        for (int i = 1; i <= 5; i++) {
            DataRecord record = new DataRecord();
            record.setId(UUID.randomUUID());
            record.setPrimaryKeyValue("record" + i);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", i);
            data.put("name", "Source Record " + i);
            data.put("value", i * 10);
            record.setData(data);
            
            sourceRecords.add(record);
        }
        sourceSnapshot.setRecords(sourceRecords);

        // Create target snapshot
        targetSnapshot = new DataSnapshot();
        targetSnapshot.setId(UUID.randomUUID());
        targetSnapshot.setName("Target API Snapshot");
        targetSnapshot.setSourceType("REST_API");
        targetSnapshot.setSourceIdentifier("https://api.example.com/target");
        targetSnapshot.setSnapshotTime(LocalDateTime.now());
        targetSnapshot.setStatus(SnapshotStatus.COMPLETED);
        
        // Create some records for target
        Set<DataRecord> targetRecords = new HashSet<>();
        for (int i = 1; i <= 5; i++) {
            DataRecord record = new DataRecord();
            record.setId(UUID.randomUUID());
            record.setPrimaryKeyValue("record" + i);
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", i);
            data.put("name", "Target Record " + i);
            data.put("value", i * 10 + (i % 2)); // Some values differ slightly
            record.setData(data);
            
            targetRecords.add(record);
        }
        targetSnapshot.setRecords(targetRecords);

        // Create comparison result
        comparisonResult = new ComparisonResult();
        comparisonResult.setId(resultId);
        comparisonResult.setTotalRecords(5);
        comparisonResult.setMatchedRecords(3);
        comparisonResult.setMismatchedRecords(2);
        comparisonResult.setSourceOnlyRecords(0);
        comparisonResult.setTargetOnlyRecords(0);
        
        // Setup differences
        Set<RecordDifference> differences = new HashSet<>();
        for (int i = 1; i <= 2; i++) {
            RecordDifference diff = new RecordDifference();
            diff.setPrimaryKeyValue("record" + (i * 2 - 1));
            diff.setFieldName("value");
            diff.setSourceValue((i * 2 - 1) * 10);
            diff.setTargetValue((i * 2 - 1) * 10 + 1);
            diff.setDifferenceType(DifferenceType.VALUE_MISMATCH);
            differences.add(diff);
        }
        comparisonResult.setDifferences(differences);
    }

    @Test
    void compareRestApiSources_Success() {
        // Setup
        String sourceOneUrl = "https://api.example.com/source";
        Map<String, Object> sourceOneParams = new HashMap<>();
        sourceOneParams.put("authToken", "TEST_SOURCE_TOKEN");
        sourceOneParams.put("recordsPath", "data.items");
        
        String sourceTwoUrl = "https://api.example.com/target";
        Map<String, Object> sourceTwoParams = new HashMap<>();
        sourceTwoParams.put("authToken", "TEST_TARGET_TOKEN");
        sourceTwoParams.put("primaryKeyField", "itemId");
        
        when(connectorRegistry.getConnector("REST_API")).thenReturn(restApiConnector);
        when(restApiConnector.extractSnapshot(eq(sourceOneUrl), any())).thenReturn(sourceSnapshot);
        when(restApiConnector.extractSnapshot(eq(sourceTwoUrl), any())).thenReturn(targetSnapshot);
        when(comparisonEngine.compare(sourceSnapshot, targetSnapshot, config)).thenReturn(comparisonResult);
        when(resultRepository.save(comparisonResult)).thenReturn(comparisonResult);

        // Execute
        ComparisonResult result = directComparisonService.compareRestApiSources(
                sourceOneUrl,
                sourceOneParams,
                sourceTwoUrl,
                sourceTwoParams,
                config
        );

        // Verify result properties
        assertNotNull(result);
        assertEquals(resultId, result.getId());
        assertEquals(5, result.getTotalRecords());
        assertEquals(3, result.getMatchedRecords());
        assertEquals(2, result.getMismatchedRecords());
        
        // Verify that parameters were correctly passed with URL added
        verify(restApiConnector).extractSnapshot(eq(sourceOneUrl), argThat(params -> 
                params.containsKey("url") && params.get("url").equals(sourceOneUrl) &&
                params.containsKey("authToken") && params.get("authToken").equals("source-token") &&
                params.containsKey("recordsPath") && params.get("recordsPath").equals("data.items")
        ));
        
        verify(restApiConnector).extractSnapshot(eq(sourceTwoUrl), argThat(params -> 
                params.containsKey("url") && params.get("url").equals(sourceTwoUrl) &&
                params.containsKey("authToken") && params.get("authToken").equals("target-token") &&
                params.containsKey("primaryKeyField") && params.get("primaryKeyField").equals("itemId")
        ));
        
        // Verify service interactions
        verify(connectorRegistry, times(2)).getConnector("REST_API");
        verify(comparisonEngine).compare(sourceSnapshot, targetSnapshot, config);
        verify(resultRepository).save(comparisonResult);
    }
    
    @Test
    void compareRestApiSources_WithNullParams() {
        // Setup - test with null params
        String sourceOneUrl = "https://api.example.com/source";
        String sourceTwoUrl = "https://api.example.com/target";
        
        when(connectorRegistry.getConnector("REST_API")).thenReturn(restApiConnector);
        when(restApiConnector.extractSnapshot(eq(sourceOneUrl), any())).thenReturn(sourceSnapshot);
        when(restApiConnector.extractSnapshot(eq(sourceTwoUrl), any())).thenReturn(targetSnapshot);
        when(comparisonEngine.compare(sourceSnapshot, targetSnapshot, config)).thenReturn(comparisonResult);
        when(resultRepository.save(comparisonResult)).thenReturn(comparisonResult);

        // Execute
        ComparisonResult result = directComparisonService.compareRestApiSources(
                sourceOneUrl,
                null,
                sourceTwoUrl,
                null,
                config
        );

        // Verify
        assertNotNull(result);
        assertEquals(resultId, result.getId());
        
        // Verify that default empty maps were created and URL was still added
        verify(restApiConnector).extractSnapshot(eq(sourceOneUrl), argThat(params -> 
                params.containsKey("url") && params.get("url").equals(sourceOneUrl) &&
                params.size() == 1 // Only URL parameter
        ));
        
        verify(restApiConnector).extractSnapshot(eq(sourceTwoUrl), argThat(params -> 
                params.containsKey("url") && params.get("url").equals(sourceTwoUrl) &&
                params.size() == 1 // Only URL parameter
        ));
    }

    @Test
    void compareDataSources_Success() {
        // Setup
        String sourceOneType = "REST_API";
        String sourceOneIdentifier = "https://api.example.com/source";
        Map<String, Object> sourceOneParams = new HashMap<>();
        sourceOneParams.put("authToken", "source-token");
        
        String sourceTwoType = "RDBMS";
        String sourceTwoIdentifier = "my-database";
        Map<String, Object> sourceTwoParams = new HashMap<>();
        sourceTwoParams.put("query", "SELECT * FROM products");
        
        when(connectorRegistry.getConnector("REST_API")).thenReturn(restApiConnector);
        when(connectorRegistry.getConnector("RDBMS")).thenReturn(rdbmsConnector);
        when(restApiConnector.extractSnapshot(sourceOneIdentifier, sourceOneParams)).thenReturn(sourceSnapshot);
        when(rdbmsConnector.extractSnapshot(sourceTwoIdentifier, sourceTwoParams)).thenReturn(targetSnapshot);
        when(comparisonEngine.compare(sourceSnapshot, targetSnapshot, config)).thenReturn(comparisonResult);
        when(resultRepository.save(comparisonResult)).thenReturn(comparisonResult);

        // Execute
        ComparisonResult result = directComparisonService.compareDataSources(
                sourceOneType,
                sourceOneIdentifier,
                sourceOneParams,
                sourceTwoType,
                sourceTwoIdentifier,
                sourceTwoParams,
                config
        );

        // Verify
        assertNotNull(result);
        assertEquals(resultId, result.getId());
        assertEquals(5, result.getTotalRecords());
        assertEquals(3, result.getMatchedRecords());
        assertEquals(2, result.getMismatchedRecords());
        
        verify(connectorRegistry).getConnector("REST_API");
        verify(connectorRegistry).getConnector("RDBMS");
        verify(restApiConnector).extractSnapshot(sourceOneIdentifier, sourceOneParams);
        verify(rdbmsConnector).extractSnapshot(sourceTwoIdentifier, sourceTwoParams);
        verify(comparisonEngine).compare(sourceSnapshot, targetSnapshot, config);
        verify(resultRepository).save(comparisonResult);
    }

    @Test
    void compareDataSources_UnsupportedSourceType() {
        // Setup
        String sourceOneType = "UNSUPPORTED";
        String sourceOneIdentifier = "source";
        Map<String, Object> sourceOneParams = new HashMap<>();
        
        String sourceTwoType = "REST_API";
        String sourceTwoIdentifier = "target";
        Map<String, Object> sourceTwoParams = new HashMap<>();
        
        when(connectorRegistry.getConnector("UNSUPPORTED")).thenReturn(null);
        when(connectorRegistry.getConnector("REST_API")).thenReturn(restApiConnector);

        // Execute & Verify
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            directComparisonService.compareDataSources(
                    sourceOneType,
                    sourceOneIdentifier,
                    sourceOneParams,
                    sourceTwoType,
                    sourceTwoIdentifier,
                    sourceTwoParams,
                    config
            );
        });
        
        assertTrue(exception.getMessage().contains("Unsupported source type"));
    }

    @Test
    void getComparisonResult_Success() {
        // Setup
        when(resultRepository.findById(resultId)).thenReturn(Optional.of(comparisonResult));

        // Execute
        ComparisonResult result = directComparisonService.getComparisonResult(resultId);

        // Verify
        assertNotNull(result);
        assertEquals(resultId, result.getId());
        verify(resultRepository).findById(resultId);
    }

    @Test
    void getComparisonResult_NotFound() {
        // Setup
        when(resultRepository.findById(resultId)).thenReturn(Optional.empty());

        // Execute & Verify
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            directComparisonService.getComparisonResult(resultId);
        });
        
        assertTrue(exception.getMessage().contains("Comparison result not found"));
    }
}