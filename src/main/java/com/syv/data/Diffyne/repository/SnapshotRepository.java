package com.syv.data.Diffyne.repository;

import com.syv.data.Diffyne.model.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SnapshotRepository {
    DataSnapshot save(DataSnapshot snapshot);
    Optional<DataSnapshot> findById(UUID id);
    List<DataSnapshot> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
    List<DataSnapshot> findBySourceType(String sourceType);
    List<DataSnapshot> findBySnapshotTimeBetween(LocalDateTime start, LocalDateTime end);
}
