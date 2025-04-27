package com.syv.data.Diffyne.service;


import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.model.ComparisonResult;

import java.util.Map;
import java.util.UUID;

public interface DirectComparisonService {

    /**
     * Compare two REST API data sources directly
     *
     * @param sourceOneUrl URL for the first REST API source
     * @param sourceOneParams Parameters for the first REST API source
     * @param sourceTwoUrl URL for the second REST API source
     * @param sourceTwoParams Parameters for the second REST API source
     * @param config Comparison configuration
     * @return The comparison result
     */
    ComparisonResult compareRestApiSources(
            String sourceOneUrl,
            Map<String, Object> sourceOneParams,
            String sourceTwoUrl,
            Map<String, Object> sourceTwoParams,
            ComparisonConfig config
    );

    /**
     * Compare two data sources of any supported type
     *
     * @param sourceOneType Type of the first data source
     * @param sourceOneIdentifier Identifier for the first data source
     * @param sourceOneParams Parameters for the first data source
     * @param sourceTwoType Type of the second data source
     * @param sourceTwoIdentifier Identifier for the second data source
     * @param sourceTwoParams Parameters for the second data source
     * @param config Comparison configuration
     * @return The comparison result
     */
    ComparisonResult compareDataSources(
            String sourceOneType,
            String sourceOneIdentifier,
            Map<String, Object> sourceOneParams,
            String sourceTwoType,
            String sourceTwoIdentifier,
            Map<String, Object> sourceTwoParams,
            ComparisonConfig config
    );

    /**
     * Get a previously generated comparison result
     *
     * @param resultId The ID of the comparison result
     * @return The comparison result
     */
    ComparisonResult getComparisonResult(UUID resultId);
}
