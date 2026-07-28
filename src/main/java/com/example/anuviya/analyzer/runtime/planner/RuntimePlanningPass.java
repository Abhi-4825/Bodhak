package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.runtime.RuntimeFacts;

/**
 * Interface representing a single pass in the Runtime Validation planning pipeline.
 */
public interface RuntimePlanningPass {
    void execute(RuntimePlanningContext context, RuntimeFacts facts);
}
