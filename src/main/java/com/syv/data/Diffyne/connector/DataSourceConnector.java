package com.syv.data.Diffyne.connector;

import com.syv.data.Diffyne.model.DataSnapshot;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

public interface DataSourceConnector {
    DataSnapshot extractSnapshot(String sourceIdentifier, Map<String, Object> extractionParams);
    boolean validateConnection(String sourceIdentifier);
    Set<String> getAvailableSources();
}
