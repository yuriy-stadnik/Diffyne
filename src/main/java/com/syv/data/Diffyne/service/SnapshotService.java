package com.syv.data.Diffyne.service;

import com.syv.data.Diffyne.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public interface SnapshotService {
    DataSnapshot createSnapshot(String sourceType, String sourceIdentifier, Map<String, Object> params);
    DataSnapshot getSnapshot(UUID snapshotId);
    List<DataSnapshot> findSnapshots(Map<String, Object> criteria);
    void deleteSnapshot(UUID snapshotId);
}
