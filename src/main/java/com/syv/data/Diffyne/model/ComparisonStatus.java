package com.syv.data.Diffyne.model;

public enum ComparisonStatus {
    SCHEDULED,    // Job is scheduled for future execution
    QUEUED,       // Job is in the queue waiting to be executed
    IN_PROGRESS,  // Job is currently being executed
    COMPLETED,    // Job has completed successfully
    FAILED        // Job execution failed
}