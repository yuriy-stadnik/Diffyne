package com.syv.data.Diffyne.service.impl;


import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.connector.ConnectorRegistry;
import com.syv.data.Diffyne.connector.DataSourceConnector;
import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.repository.ComparisonResultRepository;
import com.syv.data.Diffyne.service.ComparisonEngine;
import com.syv.data.Diffyne.service.DirectComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DirectComparisonServiceImpl implements DirectComparisonService {

    @Autowired
    private ConnectorRegistry connectorRegistry;

    @Autowired
    private ComparisonEngine comparisonEngine;

    @Autowired
    private ComparisonResultRepository resultRepository;

    @Override
    public ComparisonResult compareRestApiSources(
            String sourceOneUrl,
            Map<String, Object> sourceOneParams,
            String sourceTwoUrl,
            Map<String, Object> sourceTwoParams,
            ComparisonConfig config) {

        // Set default parameters if not provided
        Map<String, Object> params1 = new HashMap<>(sourceOneParams != null ? sourceOneParams : new HashMap<>());
        Map<String, Object> params2 = new HashMap<>(sourceTwoParams != null ? sourceTwoParams : new HashMap<>());

        // Add URLs to the params
        params1.put("url", sourceOneUrl);
        params2.put("url", sourceTwoUrl);

        // Use the general method to compare two data sources
        return compareDataSources(
                "REST_API", sourceOneUrl, params1,
                "REST_API", sourceTwoUrl, params2,
                config
        );
    }
    @Override
    public ComparisonResult compareDataSources(
            String sourceOneType,
            String sourceOneIdentifier,
            Map<String, Object> sourceOneParams,
            String sourceTwoType,
            String sourceTwoIdentifier,
            Map<String, Object> sourceTwoParams,
            ComparisonConfig config) {

        // Get the appropriate connectors
        DataSourceConnector connector1 = connectorRegistry.getConnector(sourceOneType);
        DataSourceConnector connector2 = connectorRegistry.getConnector(sourceTwoType);

        if (connector1 == null) {
            throw new IllegalArgumentException("Unsupported source type: " + sourceOneType);
        }

        if (connector2 == null) {
            throw new IllegalArgumentException("Unsupported source type: " + sourceTwoType);
        }
        sourceOneParams.put("primaryKeyField",config.getKeyFields().stream().findFirst().orElse("id"));
        sourceTwoParams.put("primaryKeyField", config.getKeyFields().stream().findFirst().orElse("id"));
        // Extract data from both sources
        DataSnapshot snapshot1 = connector1.extractSnapshot(sourceOneIdentifier, sourceOneParams);
        DataSnapshot snapshot2 = connector2.extractSnapshot(sourceTwoIdentifier, sourceTwoParams);

        // Perform comparison
        ComparisonResult result = comparisonEngine.compare(snapshot1, snapshot2, config);

        // Save the result to the repository
        return resultRepository.save(result);
    }

    @Override
    public ComparisonResult getComparisonResult(UUID resultId) {
        return resultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Comparison result not found: " + resultId));
    }
}