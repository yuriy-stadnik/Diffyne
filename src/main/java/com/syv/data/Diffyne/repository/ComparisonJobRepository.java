package com.syv.data.Diffyne.repository;

import com.syv.data.Diffyne.model.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComparisonJobRepository {
    ComparisonJob save(ComparisonJob job);
    Optional<ComparisonJob> findById(UUID id);
    List<ComparisonJob> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
    List<ComparisonJob> findByStatus(ComparisonStatus status);
    List<ComparisonJob> findByScheduledTimeBefore(LocalDateTime time);
}
