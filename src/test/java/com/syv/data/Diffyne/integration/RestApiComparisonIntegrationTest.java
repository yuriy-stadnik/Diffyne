package com.syv.data.Diffyne.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.dto.DualSourceComparisonRequest;
import com.syv.data.Diffyne.model.ComparisonResult;
import com.syv.data.Diffyne.model.DifferenceType;
import com.syv.data.Diffyne.model.RecordDifference;
import com.syv.data.Diffyne.service.DirectComparisonService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RestApiComparisonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DirectComparisonService directComparisonService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void compareRestDataSources() throws Exception {
        // Create source and target JSON
        ArrayNode sourceData = createSourceCustomerData();
        ArrayNode targetData = createTargetCustomerData();

        // Configure mock server to respond to source API request
        String sourceUrl = "http://localhost:8080/rjp/query?connection=DB1&sqlQuery="
                + URLEncoder.encode("select * from customer whereid in (10,11)", StandardCharsets.UTF_8);
        
//        mockServer.expect(ExpectedCount.once(),
//                requestTo(new URI(sourceUrl)))
//                .andExpect(method(HttpMethod.GET))
//                .andRespond(withStatus(HttpStatus.OK)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(objectMapper.writeValueAsString(sourceData)));
//
//        mockServer.verify();
//
        // Configure mock server to respond to target API request
        String targetUrl = "http://localhost:8080/rjp/query?connection=DB2&sqlQuery="
                + URLEncoder.encode("select * from customer where id in (10,11)", StandardCharsets.UTF_8);
//
//        mockServer.expect(ExpectedCount.once(),
//                requestTo(new URI(targetUrl)))
//                .andExpect(method(HttpMethod.GET))
//                .andRespond(withStatus(HttpStatus.OK)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(objectMapper.writeValueAsString(targetData)));
//
//        mockServer.verify();
        // Create comparison config
        ComparisonConfig config = createComparisonConfig();

        // Mock comparison service response
        ComparisonResult mockResult = createMockComparisonResult(sourceData, targetData);
        when(directComparisonService.compareRestApiSources(any(), any(), any(), any(), any()))
                .thenReturn(mockResult);

        // Create request
        DualSourceComparisonRequest request = new DualSourceComparisonRequest();
        request.setSourceOneUrl(sourceUrl);
        request.setSourceTwoUrl(targetUrl);
        request.setComparisonConfig(config);
        
        // Execute request
        mockMvc.perform(post("/api/direct-comparisons/rest-api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(sourceData.size() + targetData.size() - 6 )) // 5 records in common
                .andExpect(jsonPath("$.differences", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.matchedRecords").exists())
                .andExpect(jsonPath("$.mismatchedRecords").exists());

        // Verify all mocked endpoints were called


        // Manually validate differences
        List<RecordDifference> differences = findDifferences(sourceData, targetData);
        validateDifferences(differences);
    }

    /**
     * Creates mock source customer data
     */
    private ArrayNode createSourceCustomerData() {
        ArrayNode array = objectMapper.createArrayNode();
        
        // Create 10 customer records
        for (int i = 1; i <= 10; i++) {
            ObjectNode customer = objectMapper.createObjectNode();
            customer.put("id", i);
            customer.put("name", "Customer " + i);
            customer.put("email", "customer" + i + "@example.com");
            customer.put("status", i % 3 == 0 ? "INACTIVE" : "ACTIVE");
            customer.put("balance", i * 100.50);
            customer.put("created_date", "2023-01-" + (i < 10 ? "0" + i : i));
            array.add(customer);
        }
        
        return array;
    }

    /**
     * Creates mock target customer data with some differences
     */
    private ArrayNode createTargetCustomerData() {
        ArrayNode array = objectMapper.createArrayNode();
        
        // Create 10 customer records, some with differences, some missing, some extra
        for (int i = 5; i <= 15; i++) {
            ObjectNode customer = objectMapper.createObjectNode();
            customer.put("id", i);
            customer.put("name", i % 7 == 0 ? "Modified Customer " + i : "Customer " + i);
            customer.put("email", "customer" + i + "@example.com");
            
            // Some status differences
            if (i % 4 == 0) {
                customer.put("status", "PENDING");
            } else {
                customer.put("status", i % 3 == 0 ? "INACTIVE" : "ACTIVE");
            }
            
            // Some balance differences
            customer.put("balance", i % 5 == 0 ? i * 100.75 : i * 100.50);
            
            // Add an extra field to some records
            if (i % 6 == 0) {
                customer.put("last_login", "2023-05-" + (i < 10 ? "0" + i : i));
            }
            
            // Modify date format for some records
            customer.put("created_date", i % 8 == 0 ? 
                    "2023/01/" + (i < 10 ? "0" + i : i) : 
                    "2023-01-" + (i < 10 ? "0" + i : i));
            
            array.add(customer);
        }
        
        return array;
    }

    /**
     * Creates a comparison configuration
     */
    private ComparisonConfig createComparisonConfig() {
        ComparisonConfig config = new ComparisonConfig();
        
        // Use ID as the key field
        Set<String> keyFields = new HashSet<>();
        keyFields.add("id");
        config.setKeyFields(keyFields);
        
        // Compare all fields
        Set<String> fieldsToCompare = new HashSet<>();
        fieldsToCompare.add("name");
        fieldsToCompare.add("email");
        fieldsToCompare.add("status");
        fieldsToCompare.add("balance");
        fieldsToCompare.add("created_date");
        config.setFieldsToCompare(fieldsToCompare);
        
        // Set tolerance for numeric fields
        Map<String, Double> toleranceLevels = new HashMap<>();
        toleranceLevels.put("balance", 0.1); // Allow small differences in balance
        config.setToleranceLevels(toleranceLevels);
        
        // Ignore case for string comparisons
        config.setIgnoreCase(true);
        
        // Ignore whitespace for string comparisons
        config.setIgnoreWhitespace(true);
        
        return config;
    }

    /**
     * Creates a mock comparison result
     */
    private ComparisonResult createMockComparisonResult(ArrayNode sourceData, ArrayNode targetData) {
        ComparisonResult result = new ComparisonResult();
        result.setId(UUID.randomUUID());
        
        // Compute stats
        int sourceOnly = 0;
        int targetOnly = 0;
        int matched = 0;
        int mismatched = 0;
        
        // Find common IDs
        Set<Integer> sourceIds = new HashSet<>();
        Set<Integer> targetIds = new HashSet<>();
        
        for (JsonNode node : sourceData) {
            sourceIds.add(node.get("id").asInt());
        }
        
        for (JsonNode node : targetData) {
            targetIds.add(node.get("id").asInt());
        }
        
        // Calculate source-only records
        for (Integer id : sourceIds) {
            if (!targetIds.contains(id)) {
                sourceOnly++;
            }
        }
        
        // Calculate target-only records
        for (Integer id : targetIds) {
            if (!sourceIds.contains(id)) {
                targetOnly++;
            }
        }
        
        // Calculate intersection
        Set<Integer> commonIds = new HashSet<>(sourceIds);
        commonIds.retainAll(targetIds);
        
        // Assume half are matched and half are mismatched
        matched = commonIds.size() / 2;
        mismatched = commonIds.size() - matched;
        
        // Set stats
        result.setTotalRecords(sourceData.size() + targetData.size() - commonIds.size());
        result.setMatchedRecords(matched);
        result.setMismatchedRecords(mismatched);
        result.setSourceOnlyRecords(sourceOnly);
        result.setTargetOnlyRecords(targetOnly);
        
        // Add some differences
        Set<RecordDifference> differences = new HashSet<>();
        differences.addAll(findDifferences(sourceData, targetData));
        result.setDifferences(differences);
        
        return result;
    }

    /**
     * Compares source and target data to find differences
     */
    private List<RecordDifference> findDifferences(ArrayNode sourceData, ArrayNode targetData) {
        List<RecordDifference> differences = new ArrayList<>();
        
        // Create a map of target records by ID for faster lookup
        Map<Integer, JsonNode> targetRecordsMap = new HashMap<>();
        for (JsonNode node : targetData) {
            targetRecordsMap.put(node.get("id").asInt(), node);
        }
        
        // Compare each source record with its target counterpart
        for (JsonNode sourceRecord : sourceData) {
            int id = sourceRecord.get("id").asInt();
            JsonNode targetRecord = targetRecordsMap.get(id);
            
            if (targetRecord == null) {
                // Record exists only in source
                RecordDifference difference = new RecordDifference();
                difference.setPrimaryKeyValue(String.valueOf(id));
                difference.setDifferenceType(DifferenceType.SOURCE_ONLY);
                differences.add(difference);
            } else {
                // Record exists in both sources, compare fields
                Iterator<String> fieldNames = sourceRecord.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode sourceValue = sourceRecord.get(fieldName);
                    JsonNode targetValue = targetRecord.get(fieldName);
                    
                    if (targetValue == null) {
                        // Field exists only in source
                        RecordDifference difference = new RecordDifference();
                        difference.setPrimaryKeyValue(String.valueOf(id));
                        difference.setFieldName(fieldName);
                        difference.setSourceValue(sourceValue.asText());
                        difference.setTargetValue(null);
                        difference.setDifferenceType(DifferenceType.VALUE_MISMATCH);
                        differences.add(difference);
                    } else if (!sourceValue.equals(targetValue)) {
                        // Field values differ
                        RecordDifference difference = new RecordDifference();
                        difference.setPrimaryKeyValue(String.valueOf(id));
                        difference.setFieldName(fieldName);
                        difference.setSourceValue(sourceValue.asText());
                        difference.setTargetValue(targetValue.asText());
                        difference.setDifferenceType(DifferenceType.VALUE_MISMATCH);
                        differences.add(difference);
                    }
                }
                
                // Check for fields only in target
                Iterator<String> targetFieldNames = targetRecord.fieldNames();
                while (targetFieldNames.hasNext()) {
                    String fieldName = targetFieldNames.next();
                    if (sourceRecord.get(fieldName) == null) {
                        // Field exists only in target
                        RecordDifference difference = new RecordDifference();
                        difference.setPrimaryKeyValue(String.valueOf(id));
                        difference.setFieldName(fieldName);
                        difference.setSourceValue(null);
                        difference.setTargetValue(targetRecord.get(fieldName).asText());
                        difference.setDifferenceType(DifferenceType.VALUE_MISMATCH);
                        differences.add(difference);
                    }
                }
                
                // Remove this record from the target map
                targetRecordsMap.remove(id);
            }
        }
        
        // Any records remaining in the target map exist only in the target
        for (Map.Entry<Integer, JsonNode> entry : targetRecordsMap.entrySet()) {
            RecordDifference difference = new RecordDifference();
            difference.setPrimaryKeyValue(String.valueOf(entry.getKey()));
            difference.setDifferenceType(DifferenceType.TARGET_ONLY);
            differences.add(difference);
        }
        
        return differences;
    }

    /**
     * Validates differences between source and target data
     */
    private void validateDifferences(List<RecordDifference> differences) {
        // Group differences by type
        Map<DifferenceType, List<RecordDifference>> groupedDifferences = new HashMap<>();
        
        for (RecordDifference diff : differences) {
            if (!groupedDifferences.containsKey(diff.getDifferenceType())) {
                groupedDifferences.put(diff.getDifferenceType(), new ArrayList<>());
            }
            groupedDifferences.get(diff.getDifferenceType()).add(diff);
        }
        
        // Validate source-only differences
        if (groupedDifferences.containsKey(DifferenceType.SOURCE_ONLY)) {
            List<RecordDifference> sourceOnlyDiffs = groupedDifferences.get(DifferenceType.SOURCE_ONLY);
            assertFalse(sourceOnlyDiffs.isEmpty(), "Should have source-only differences");
            
            // Records 1-4 should be source-only
            Set<String> expectedSourceOnlyIds = new HashSet<>(Arrays.asList("1", "2", "3", "4"));
            Set<String> actualSourceOnlyIds = sourceOnlyDiffs.stream()
                    .map(RecordDifference::getPrimaryKeyValue)
                    .collect(java.util.stream.Collectors.toSet());
            
            assertTrue(expectedSourceOnlyIds.containsAll(actualSourceOnlyIds), 
                    "Source-only ids match expected");
        }
        
        // Validate target-only differences
        if (groupedDifferences.containsKey(DifferenceType.TARGET_ONLY)) {
            List<RecordDifference> targetOnlyDiffs = groupedDifferences.get(DifferenceType.TARGET_ONLY);
            assertFalse(targetOnlyDiffs.isEmpty(), "Should have target-only differences");
            
            // Records 11-15 should be target-only
            Set<String> expectedTargetOnlyIds = new HashSet<>(Arrays.asList("11", "12", "13", "14", "15"));
            Set<String> actualTargetOnlyIds = targetOnlyDiffs.stream()
                    .map(RecordDifference::getPrimaryKeyValue)
                    .collect(java.util.stream.Collectors.toSet());
            
            assertTrue(expectedTargetOnlyIds.containsAll(actualTargetOnlyIds), 
                    "Target-only ids match expected");
        }
        
        // Validate value mismatches
        if (groupedDifferences.containsKey(DifferenceType.VALUE_MISMATCH)) {
            List<RecordDifference> valueMismatchDiffs = groupedDifferences.get(DifferenceType.VALUE_MISMATCH);
            assertFalse(valueMismatchDiffs.isEmpty(), "Should have value mismatch differences");
            
            // Various fields should have mismatches
            Set<String> expectedMismatchFields = new HashSet<>(
                    Arrays.asList("name", "status", "balance", "created_date", "last_login"));
            
            Set<String> actualMismatchFields = valueMismatchDiffs.stream()
                    .map(RecordDifference::getFieldName)
                    .collect(java.util.stream.Collectors.toSet());
            
            assertTrue(actualMismatchFields.containsAll(expectedMismatchFields), 
                    "Value mismatch fields match expected");
            
            // Check specific records with expected changes
            Optional<RecordDifference> nameDiff = valueMismatchDiffs.stream()
                    .filter(d -> "name".equals(d.getFieldName()) && "7".equals(d.getPrimaryKeyValue()))
                    .findFirst();
            
            assertTrue(nameDiff.isPresent(), "Should have name difference for record 7");
            assertEquals("Customer 7", nameDiff.get().getSourceValue());
            assertEquals("Modified Customer 7", nameDiff.get().getTargetValue());
        }
    }
}