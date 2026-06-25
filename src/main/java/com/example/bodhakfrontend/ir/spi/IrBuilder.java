package com.example.bodhakfrontend.ir.spi;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.ir.model.IrProject;

/**
 * Service Provider Interface for BIR construction.
 *
 * <p>Each language plugin provides one implementation of this interface.
 * The implementation receives the shared {@link AnalysisContext} (which already
 * contains parsed entities and the dependency graph) and produces an
 * {@link IrProject} containing semantic operations for every entity.</p>
 *
 * <p>Implementations must be stateless and thread-safe.</p>
 *
 * <p>To add a new language:
 * <ol>
 *   <li>Create {@code languages/kotlin/KotlinIrBuilder.java} implementing this interface.</li>
 *   <li>Return it from {@code KotlinLanguagePlugin} via {@link LanguageIrProvider}.</li>
 *   <li>No other files need changing.</li>
 * </ol>
 * </p>
 */
public interface IrBuilder {
    IrProject build(AnalysisContext context);
}
