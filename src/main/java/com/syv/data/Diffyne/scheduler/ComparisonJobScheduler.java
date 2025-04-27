package com.syv.data.Diffyne.scheduler;

import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.service.ComparisonService;
import com.syv.data.Diffyne.repository.ComparisonJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComparisonJobScheduler {
    @Autowired
    private ComparisonJobRepository jobRepository;

    @Autowired
    private ComparisonService comparisonService;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void executeScheduledJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<ComparisonJob> pendingJobs = jobRepository.findByScheduledTimeBefore(now)
                .stream()
                .filter(job -> job.getStatus() == ComparisonStatus.SCHEDULED)
                .collect(Collectors.toList());

        for (ComparisonJob job : pendingJobs) {
            comparisonService.executeComparisonJob(job.getId());
        }
    }
}
