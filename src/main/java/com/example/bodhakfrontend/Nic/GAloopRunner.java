package com.example.bodhakfrontend.Nic;

import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.Nic.Builder.*;
import com.example.bodhakfrontend.Nic.Crossover.UniformCrossover;
import com.example.bodhakfrontend.Nic.Model.*;
import com.example.bodhakfrontend.Nic.Mutation.Mutation;
import com.example.bodhakfrontend.Nic.Selection.TournamentSelection;
import com.example.bodhakfrontend.Nic.Suggestions.RefactoringSuggestionEngine;
import javafx.concurrent.Task;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;




public class GAloopRunner {

    public Task<OptimizationReport> createTask(
            ProjectInfo projectInfo,
            Consumer<String> progressListener
    ) {
        return new Task<>() {
            @Override
            protected OptimizationReport call() {

                ProjectMetricsBuilder projectMetricsBuilder =
                        new ProjectMetricsBuilder();

                LookUpClasses lookUpClasses =
                        new LookUpClasses();

                Metrics metrics =
                        projectMetricsBuilder
                                .getProjectMetrics(projectInfo);

                GenePoolBuilder genePoolBuilder =
                        new GenePoolBuilder();

                Map<Genes, Double> genePoolMap =
                        genePoolBuilder.getWeightedGenePool(
                                metrics,
                                lookUpClasses.getClassSuggestion(
                                        projectInfo.getClassInfos(),
                                        metrics
                                )
                        );

                TournamentSelection selection =
                        new TournamentSelection(3);

                UniformCrossover crossover =
                        new UniformCrossover();

                Mutation mutation =
                        new Mutation(0.1);

                PopulationBuilder populationBuilder =
                        new PopulationBuilder();

                Population population =
                        populationBuilder
                                .createInitialPopulation(
                                        genePoolMap, 5, 30
                                );

                GALoop ga =
                        new GALoop(100, selection, crossover, mutation);

                ga.setProgressListener(progressListener);
                GAResult result=ga.run(population, metrics, genePoolMap);
                progressListener.accept("Analyzing refactoring targets...");
                RefactoringSuggestionEngine refactoringSuggestionEngine =new RefactoringSuggestionEngine();

                List<RefactoringSuggestion> suggestions=refactoringSuggestionEngine.generateSuggestions(result,projectInfo,metrics);
                progressListener.accept("Optimization analysis completed.");





                return new OptimizationReport(result,suggestions);
            }
        };
    }
}
