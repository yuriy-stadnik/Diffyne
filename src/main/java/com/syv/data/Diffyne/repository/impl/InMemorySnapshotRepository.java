package com.syv.data.Diffyne.repository.impl;

import com.syv.data.Diffyne.model.DataSnapshot;
import com.syv.data.Diffyne.repository.SnapshotRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemorySnapshotRepository implements SnapshotRepository {
    
    private final Map<UUID, DataSnapshot> snapshotMap = new HashMap<>();
    
    @Override
    public DataSnapshot save(DataSnapshot snapshot) {
        if (snapshot.getId() == null) {
            snapshot.setId(UUID.randomUUID());
        }
        snapshotMap.put(snapshot.getId(), snapshot);
        return snapshot;
    }
    
    @Override
    public Optional<DataSnapshot> findById(UUID id) {
        return Optional.ofNullable(snapshotMap.get(id));
    }
    
    @Override
    public List<DataSnapshot> findAll() {
        return new ArrayList<>(snapshotMap.values());
    }
    
    @Override
    public void deleteById(UUID id) {
        snapshotMap.remove(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return snapshotMap.containsKey(id);
    }
    
    @Override
    public List<DataSnapshot> findBySourceType(String sourceType) {
        return snapshotMap.values().stream()
                .filter(snapshot -> snapshot.getSourceType().equals(sourceType))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<DataSnapshot> findBySnapshotTimeBetween(LocalDateTime start, LocalDateTime end) {
        return snapshotMap.values().stream()
                .filter(snapshot -> {
                    LocalDateTime time = snapshot.getSnapshotTime();
                    return time != null && 
                           (time.isEqual(start) || time.isAfter(start)) && 
                           (time.isEqual(end) || time.isBefore(end));
                })
                .collect(Collectors.toList());
    }
}