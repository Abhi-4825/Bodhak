package com.example.anuviya.analyzer.runtime.engine;

import com.example.anuviya.model.runtime.ExecutionPlan;
import com.example.anuviya.model.runtime.PerformanceReport;
import com.example.anuviya.model.runtime.RuntimeValidationPlan;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks the execution lifecycle, timing, progress, and cancellation token of an active load run.
 */
public class ExecutionSession {
    private final String sessionId;
    private final ExecutionPlan executionPlan;
    private final RuntimeValidationPlan validationPlan;
    private final ExecutionProvider provider;
    private final Instant startTime;
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    public ExecutionSession(ExecutionPlan executionPlan, RuntimeValidationPlan validationPlan, ExecutionProvider provider) {
        this.sessionId = UUID.randomUUID().toString();
        this.executionPlan = executionPlan;
        this.validationPlan = validationPlan;
        this.provider = provider;
        this.startTime = Instant.now();
    }

    public String getSessionId() {
        return sessionId;
    }

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    public RuntimeValidationPlan getValidationPlan() {
        return validationPlan;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        cancelled.set(true);
    }

    public PerformanceReport run() {
        if (isCancelled()) {
            throw new IllegalStateException("Session was cancelled before execution started.");
        }
        return provider.execute(executionPlan, validationPlan);
    }
}
