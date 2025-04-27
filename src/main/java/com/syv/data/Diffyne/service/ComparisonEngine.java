package com.syv.data.Diffyne.service;


import com.syv.data.Diffyne.config.ComparisonConfig;
import com.syv.data.Diffyne.model.ComparisonResult;
import com.syv.data.Diffyne.model.DataSnapshot;

public interface ComparisonEngine {
    /**
     * Compare two data snapshots based on the provided configuration
     *
     * @param sourceSnapshot The source/primary snapshot
     * @param targetSnapshot The target/secondary snapshot to compare against
     * @param config Configuration for the comparison
     * @return The result of the comparison
     */
    ComparisonResult compare(DataSnapshot sourceSnapshot, DataSnapshot targetSnapshot, ComparisonConfig config);
}
