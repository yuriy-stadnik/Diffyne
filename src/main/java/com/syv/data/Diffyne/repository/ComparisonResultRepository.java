package com.syv.data.Diffyne.repository;

import com.syv.data.Diffyne.model.ComparisonResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComparisonResultRepository {
    ComparisonResult save(ComparisonResult result);
    Optional<ComparisonResult> findById(UUID id);
    List<ComparisonResult> findAll();
    void deleteById(UUID id);
    ComparisonResult findByComparisonJobId(UUID jobId);
}