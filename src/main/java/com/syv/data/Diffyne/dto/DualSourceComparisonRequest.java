package com.syv.data.Diffyne.dto;


import com.syv.data.Diffyne.config.ComparisonConfig;
import lombok.Data;

import java.util.Map;

@Data
public class DualSourceComparisonRequest {
    // Source One (first data source)
    private String sourceOneUrl;
    private Map<String, Object> sourceOneParams;

    // Source Two (second data source)
    private String sourceTwoUrl;
    private Map<String, Object> sourceTwoParams;

    // Comparison configuration
    private ComparisonConfig comparisonConfig;

    // Optional - name for this comparison
    private String comparisonName;

    // Optional - whether to save snapshots for future reference
    private boolean saveSnapshots = false;
}
