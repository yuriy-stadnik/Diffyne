package com.syv.data.Diffyne.service.impl;


import com.syv.data.Diffyne.connector.ConnectorRegistry;
import com.syv.data.Diffyne.connector.DataSourceConnector;
import com.syv.data.Diffyne.model.DataSnapshot;
import com.syv.data.Diffyne.repository.SnapshotRepository;
import com.syv.data.Diffyne.service.SnapshotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SnapshotServiceImpl implements SnapshotService {

    @Autowired
    private ConnectorRegistry connectorRegistry;

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Override
    public DataSnapshot createSnapshot(String sourceType, String sourceIdentifier, Map<String, Object> params) {
        DataSourceConnector connector = connectorRegistry.getConnector(sourceType);
        if (connector == null) {
            throw new IllegalArgumentException("Unsupported source type: " + sourceType);
        }

        DataSnapshot snapshot = connector.extractSnapshot(sourceIdentifier, params);
        return snapshotRepository.save(snapshot);
    }

    @Override
    public DataSnapshot getSnapshot(UUID snapshotId) {
        return snapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Snapshot not found: " + snapshotId));
    }

    @Override
    public List<DataSnapshot> findSnapshots(Map<String, Object> criteria) {
        // Implement search criteria logic here
        // For simplicity, we're just returning all snapshots
        return snapshotRepository.findAll();
    }

    @Override
    public void deleteSnapshot(UUID snapshotId) {
        snapshotRepository.deleteById(snapshotId);
    }
}
