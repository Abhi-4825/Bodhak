package com.example.anuviya.compiler;

/**
 * Interface that represents a single, cohesive static analysis stage (compiler pass).
 */
public interface CompilerPass {
    
    /**
     * Executes the compiler pass logic on the given pipeline context.
     */
    void execute(PipelineContext context);

    /**
     * Returns the name of the compiler pass.
     */
    String getName();
}
