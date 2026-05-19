package com.syv.data.Diffyne.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.dto.DualSourceComparisonRequest;
import com.syv.data.Diffyne.model.ComparisonResult;
import com.syv.data.Diffyne.model.DifferenceType;
import com.syv.data.Diffyne.model.RecordDifference;
import com.syv.data.Diffyne.service.DirectComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DirectComparisonControllerTest {

    @Mock
    private DirectComparisonService directComparisonService;

    @InjectMocks
    private DirectComparisonController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ComparisonResult mockResult;
    private UUID resultId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        
        // Initialize test data
        resultId = UUID.randomUUID();
        
        // Create mock result
        mockResult = new ComparisonResult();
        mockResult.setId(resultId);
        mockResult.setComparisonJobId(UUID.randomUUID());
        mockResult.setTotalRecords(10);
        mockResult.setMatchedRecords(7);
        mockResult.setMismatchedRecords(2);
        mockResult.setSourceOnlyRecords(1);
        mockResult.setTargetOnlyRecords(0);
        
        // Add differences
        Set<RecordDifference> differences = new HashSet<>();
        for (int i = 1; i <= 3; i++) {
            RecordDifference diff = new RecordDifference();
            diff.setPrimaryKeyValue("record-" + i);
            diff.setFieldName("field" + i);
            diff.setSourceValue("source-" + i);
            diff.setTargetValue("target-" + i);
            diff.setDifferenceType(i % 2 == 0 ? DifferenceType.VALUE_MISMATCH : DifferenceType.SOURCE_ONLY);
            differences.add(diff);
        }
        mockResult.setDifferences(differences);
    }

    @Test
    void compareRestApis() throws Exception {
        // Arrange
        String sourceOneUrl = "https://api.example.com/source";
        String sourceTwoUrl = "https://api.example.com/target";
        
        // Create comparison config
        ComparisonConfig config = new ComparisonConfig();
        config.setKeyFields(Collections.singleton("id"));
        Map<String, String> fieldsMap = new HashMap<>();
        fieldsMap.put("name", "name");
        fieldsMap.put("value", "value");
        config.setFieldsToCompare(fieldsMap);
        config.setIgnoreCase(true);
        
        // Create source params
        Map<String, Object> sourceOneParams = new HashMap<>();
        sourceOneParams.put("authToken", "source-token");
        sourceOneParams.put("recordsPath", "data.records");
        
        Map<String, Object> sourceTwoParams = new HashMap<>();
        sourceTwoParams.put("authToken", "target-token");
        sourceTwoParams.put("recordsPath", "data.records");
        
        // Create request
        DualSourceComparisonRequest request = new DualSourceComparisonRequest();
        request.setSourceOneUrl(sourceOneUrl);
        request.setSourceOneParams(sourceOneParams);
        request.setSourceTwoUrl(sourceTwoUrl);
        request.setSourceTwoParams(sourceTwoParams);
        request.setComparisonConfig(config);
        request.setComparisonName("Test API Comparison");
        
        // Mock service response
        when(directComparisonService.compareRestApiSources(
                eq(sourceOneUrl),
                eq(sourceOneParams),
                eq(sourceTwoUrl),
                eq(sourceTwoParams),
                any(ComparisonConfig.class)
        )).thenReturn(mockResult);
        
        // Act & Assert
        mockMvc.perform(post("/api/direct-comparisons/rest-api")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resultId.toString()))
                .andExpect(jsonPath("$.totalRecords").value(10))
                .andExpect(jsonPath("$.matchedRecords").value(7))
                .andExpect(jsonPath("$.mismatchedRecords").value(2))
                .andExpect(jsonPath("$.sourceOnlyRecords").value(1))
                .andExpect(jsonPath("$.targetOnlyRecords").value(0))
                .andExpect(jsonPath("$.differences", hasSize(3)));
    }
    
    @Test
    void getComparisonResult() throws Exception {
        // Arrange
        when(directComparisonService.getComparisonResult(resultId)).thenReturn(mockResult);
        
        // Act & Assert
        mockMvc.perform(get("/api/direct-comparisons/" + resultId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resultId.toString()))
                .andExpect(jsonPath("$.totalRecords").value(10))
                .andExpect(jsonPath("$.matchedRecords").value(7))
                .andExpect(jsonPath("$.differences", hasSize(3)));
    }
    
    @Test
    void getDifferences() throws Exception {
        // Arrange
        when(directComparisonService.getComparisonResult(resultId)).thenReturn(mockResult);
        
        // Act & Assert
        mockMvc.perform(get("/api/direct-comparisons/" + resultId + "/differences")
                .param("page", "0")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].primaryKeyValue").exists())
                .andExpect(jsonPath("$[0].fieldName").exists())
                .andExpect(jsonPath("$[0].sourceValue").exists())
                .andExpect(jsonPath("$[0].targetValue").exists())
                .andExpect(jsonPath("$[0].differenceType").exists());
                
        // Test filtering differences by type
        mockMvc.perform(get("/api/direct-comparisons/" + resultId + "/differences")
                .param("differenceType", "VALUE_MISMATCH")
                .param("page", "0")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].differenceType").value("VALUE_MISMATCH"));
    }
    
    @Test
    void getSummary() throws Exception {
        // Arrange
        when(directComparisonService.getComparisonResult(resultId)).thenReturn(mockResult);
        
        // Act & Assert
        mockMvc.perform(get("/api/direct-comparisons/" + resultId + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(resultId.toString()))
                .andExpect(jsonPath("$.totalRecords").value(10))
                .andExpect(jsonPath("$.matchedRecords").value(7))
                .andExpect(jsonPath("$.mismatchedRecords").value(2))
                .andExpect(jsonPath("$.sourceOnlyRecords").value(1))
                .andExpect(jsonPath("$.targetOnlyRecords").value(0))
                .andExpect(jsonPath("$.diffCount").value(3));
    }
}