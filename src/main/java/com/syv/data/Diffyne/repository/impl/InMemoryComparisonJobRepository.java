package com.syv.data.Diffyne.repository.impl;

import com.syv.data.Diffyne.model.ComparisonJob;
import com.syv.data.Diffyne.model.ComparisonStatus;
import com.syv.data.Diffyne.repository.ComparisonJobRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryComparisonJobRepository implements ComparisonJobRepository {
    
    private final Map<UUID, ComparisonJob> jobMap = new HashMap<>();
    
    @Override
    public ComparisonJob save(ComparisonJob job) {
        if (job.getId() == null) {
            job.setId(UUID.randomUUID());
        }
        jobMap.put(job.getId(), job);
        return job;
    }
    
    @Override
    public Optional<ComparisonJob> findById(UUID id) {
        return Optional.ofNullable(jobMap.get(id));
    }
    
    @Override
    public List<ComparisonJob> findAll() {
        return new ArrayList<>(jobMap.values());
    }
    
    @Override
    public void deleteById(UUID id) {
        jobMap.remove(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jobMap.containsKey(id);
    }
    
    @Override
    public List<ComparisonJob> findByStatus(ComparisonStatus status) {
        return jobMap.values().stream()
                .filter(job -> job.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ComparisonJob> findByScheduledTimeBefore(LocalDateTime time) {
        return jobMap.values().stream()
                .filter(job -> job.getScheduledTime() != null && job.getScheduledTime().isBefore(time))
                .collect(Collectors.toList());
    }
}