package com.example.anuviya.compiler;

import com.example.anuviya.frontend.FrontendRegistry;
import com.example.anuviya.frontend.LanguageFrontend;
import com.example.anuviya.ir.IRNode;
import com.example.anuviya.compiler.pass.classification.ProjectClassifierPass;
import com.example.anuviya.compiler.pass.entity.EntityBuilderPass;
import com.example.anuviya.compiler.pass.flag.QualityAnalysisPass;
import com.example.anuviya.compiler.pass.root.ProjectRootsBuilder;
import com.example.anuviya.compiler.pass.metrics.MetricBuilderPass;
import com.example.anuviya.compiler.pass.relationships.RelationshipBuilderPass;
import java.nio.file.Path;
import java.util.*;

/**
 * Orchestrates the compiler stages: sets up parser services, builds semantic IR, and runs analysis passes.
 */
public class AnalysisPipeline {

    private final List<CompilerPass> passes = new ArrayList<>();
    private final FrontendRegistry frontendRegistry;

    public AnalysisPipeline(FrontendRegistry frontendRegistry) {
        this.frontendRegistry = frontendRegistry;

        // Register default passes in order
        passes.add(new EntityBuilderPass());
        passes.add(new MetricBuilderPass());
        passes.add(new RelationshipBuilderPass());
        passes.add(new ProjectRootsBuilder());
        passes.add(new ProjectClassifierPass());
        passes.add(new QualityAnalysisPass());
    }

    public void addPass(CompilerPass pass) {
        passes.add(pass);
    }

    public PipelineContext analyze(List<Path> files) {
        PipelineContext pipelineContext = new PipelineContext();

        com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
            new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.ParsingStarted(files.size())
        );

        int count = 0;
        for (Path file : files) {
            count++;
            Optional<LanguageFrontend> frontendOpt = frontendRegistry.forFile(file);
            if (frontendOpt.isPresent()) {
                try {
                    IRNode ir = frontendOpt.get().parseToIR(file);
                    if (ir != null) {
                        pipelineContext.addCompilationUnit(new CompilationUnit(file, ir));
                    }
                } catch (Exception e) {
                    System.err.println("Pipeline failed parsing " + file + ": " + e.getMessage());
                }
            }
            com.example.anuviya.orchestration.progress.ProgressPublisher.publish(
                new com.example.anuviya.orchestration.progress.AnalysisProgressEvents.CompilationUnitParsed(
                    file, file.getFileName().toString(), count, files.size()
                )
            );
        }

        // Execute passes
        for (CompilerPass pass : passes) {
            pass.execute(pipelineContext);
        }

        return pipelineContext;
    }
}
