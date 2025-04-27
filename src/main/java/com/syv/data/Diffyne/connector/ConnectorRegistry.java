package com.syv.data.Diffyne.connector;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface ConnectorRegistry {
    DataSourceConnector getConnector(String sourceType);
    Set<String> getSupportedSourceTypes();
    void registerConnector(String sourceType, DataSourceConnector connector);
}