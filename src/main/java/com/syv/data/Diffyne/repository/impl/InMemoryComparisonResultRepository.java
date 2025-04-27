package com.syv.data.Diffyne.repository.impl;

import com.syv.data.Diffyne.model.ComparisonResult;
import com.syv.data.Diffyne.repository.ComparisonResultRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class InMemoryComparisonResultRepository implements ComparisonResultRepository {
    
    private final Map<UUID, ComparisonResult> resultMap = new HashMap<>();
    
    @Override
    public ComparisonResult save(ComparisonResult result) {
        if (result.getId() == null) {
            result.setId(UUID.randomUUID());
        }
        resultMap.put(result.getId(), result);
        return result;
    }
    
    @Override
    public Optional<ComparisonResult> findById(UUID id) {
        return Optional.ofNullable(resultMap.get(id));
    }
    
    @Override
    public List<ComparisonResult> findAll() {
        return new ArrayList<>(resultMap.values());
    }
    
    @Override
    public void deleteById(UUID id) {
        resultMap.remove(id);
    }
    
    @Override
    public ComparisonResult findByComparisonJobId(UUID jobId) {
        return resultMap.values().stream()
                .filter(result -> result.getComparisonJobId().equals(jobId))
                .findFirst()
                .orElse(null);
    }
}