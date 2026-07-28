package com.example.anuviya.analyzer.runtime.engine;

import com.example.anuviya.model.runtime.ExecutionPlan;
import com.example.anuviya.model.runtime.PerformanceReport;
import com.example.anuviya.model.runtime.RuntimeValidationPlan;

/**
 * Agnostic interface representing pluggable validation execution backends.
 */
public interface ExecutionProvider {
    String getProviderId();
    String getName();
    ExecutionProviderCapabilities getCapabilities();
    PerformanceReport execute(ExecutionPlan plan, RuntimeValidationPlan validationPlan);
}
