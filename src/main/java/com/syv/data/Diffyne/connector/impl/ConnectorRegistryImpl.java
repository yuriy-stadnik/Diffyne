package com.syv.data.Diffyne.connector.impl;


import com.syv.data.Diffyne.connector.ConnectorRegistry;
import com.syv.data.Diffyne.connector.DataSourceConnector;
import com.syv.data.Diffyne.connector.KafkaConnector;
import com.syv.data.Diffyne.connector.RdbmsConnector;
import com.syv.data.Diffyne.connector.RestApiConnector;
import com.syv.data.Diffyne.connector.SalesforceConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ConnectorRegistryImpl implements ConnectorRegistry {

    private final Map<String, DataSourceConnector> connectors = new HashMap<>();

    @Autowired
    private RdbmsConnector rdbmsConnector;

    @Autowired
    private KafkaConnector kafkaConnector;

    @Autowired
    private SalesforceConnector salesforceConnector;

    @Autowired
    private RestApiConnector restApiConnector;

    @PostConstruct
    public void init() {
        connectors.put("RDBMS", rdbmsConnector);
        connectors.put("KAFKA", kafkaConnector);
        connectors.put("SALESFORCE", salesforceConnector);
        connectors.put("REST_API", restApiConnector);
    }

    @Override
    public DataSourceConnector getConnector(String sourceType) {
        return connectors.get(sourceType);
    }

    @Override
    public Set<String> getSupportedSourceTypes() {
        return connectors.keySet();
    }

    @Override
    public void registerConnector(String sourceType, DataSourceConnector connector) {
        connectors.put(sourceType, connector);
    }
}
