package com.example.bodhakfrontend.engine.optimization;

import com.example.bodhakfrontend.core.model.project.Hotspot;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.optimization.Builder.*;
import com.example.bodhakfrontend.engine.optimization.Crossover.UniformCrossover;
import com.example.bodhakfrontend.engine.optimization.Model.*;
import com.example.bodhakfrontend.engine.optimization.Mutation.Mutation;
import com.example.bodhakfrontend.engine.optimization.Selection.TournamentSelection;
import com.example.bodhakfrontend.engine.optimization.Suggestions.RefactoringSuggestionEngine;
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
                                        projectInfo.getEntities(),
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
                                        genePoolMap, 5, 50
                                );

                GALoop ga =
                        new GALoop(100, selection, crossover, mutation);

                ga.setProgressListener(progressListener);
                GAResult result=ga.run(population, metrics, genePoolMap);
                progressListener.accept("Analyzing refactoring targets...");
                RefactoringSuggestionEngine refactoringSuggestionEngine =new RefactoringSuggestionEngine();

                List<RefactoringSuggestion> suggestions=refactoringSuggestionEngine.generateSuggestions(result,projectInfo,metrics);

                double beforeScore=calculateScore(result.beforeMetrics());
                double afterScore=calculateScore(result.afterMetrics());
                List<Hotspot> hotspots=projectInfo.getHotspots();
                progressListener.accept("Optimization analysis completed.");





                return new OptimizationReport(result,suggestions,beforeScore,afterScore,hotspots);
            }
        };
    }

    private double calculateScore(Metrics metrics) {

        double maxLoc=metrics.getMaxLoc();
        double minLoc=metrics.getMinLoc();
        double avgLoc=metrics.getAverageLoc();
        double avgDeps=metrics.getAverageDeps();
        double maxDeps= metrics.getMaxDeps();
        double minDeps= metrics.getMinDeps();
        double avgMethodsLength= metrics.getAverageMethodLoc();
        double maxMethodsLength= metrics.getMaxMethodLoc();
        double minMethodsLength= metrics.getMinMethodLoc();


        double methodScore= normalize(avgMethodsLength,minMethodsLength,maxMethodsLength);
        double locScore=normalize(avgLoc,minLoc,maxLoc);
        double depsScore=normalize(avgDeps,minDeps,maxDeps);

        double score=(methodScore+locScore+depsScore)/3.0;

        score=Math.max(0,Math.min(score,1));

        return score*100;


    }
    private double normalize(double value, double min, double max) {

        if (max - min == 0) return 1;

        return 1 - ((value - min) / (max - min));
    }


}
