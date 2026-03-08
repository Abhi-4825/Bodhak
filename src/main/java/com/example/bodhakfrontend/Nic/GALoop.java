package com.example.bodhakfrontend.Nic;

import com.example.bodhakfrontend.Nic.Crossover.UniformCrossover;
import com.example.bodhakfrontend.Nic.Fitness.FitnessEvaluator;
import com.example.bodhakfrontend.Nic.Fitness.FitnessWeight;
import com.example.bodhakfrontend.Nic.Fitness.FitnessWeightStrategy;
import com.example.bodhakfrontend.Nic.Model.*;
import com.example.bodhakfrontend.Nic.Mutation.Mutation;
import com.example.bodhakfrontend.Nic.Selection.TournamentSelection;


import java.util.*;
import java.util.function.Consumer;

public class GALoop {



    private final int generation;
    private final TournamentSelection selection;
    private final UniformCrossover crossover;
    private final Mutation mutation;
    private Consumer<String> progressListener;

    public GALoop(int generation, TournamentSelection selection, UniformCrossover crossover, Mutation mutation) {
        this.generation = generation;
        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
    }
    public void setProgressListener(Consumer<String> progressListener) {
        this.progressListener = progressListener;
    }


    public GAResult run(Population population, Metrics metrics, Map<Genes,Double> geneWeihtedMap){

        List<Double> fitnessHistory = new ArrayList<>();
        boolean convergedEarly=false;
        int generationExecuted=0;


         // get Fitness weight
        FitnessWeight fitnessWeight= FitnessWeightStrategy.deriveWeights(metrics);

        FitnessEvaluator fitnessEvaluator= new FitnessEvaluator(fitnessWeight);
        double bestFitness=Double.NEGATIVE_INFINITY;
        int segmentCount=0;
        int maxSegmentCount=10;
        notifyProgressListener("Initializing Genetic Optimization Engine...");
        notifyProgressListener("Population Size: " +
                population.getChromosomes().size());
        notifyProgressListener("Max Generations: " + generation);
        notifyProgressListener("--------------------------------------");


        for(int i=0;i<generation;i++){


        // set fitness score
        fitnessEvaluator.populationFitness(population, metrics);

        // selection
        //1>-- 1 best chromosome and then select with tournament selection+ crossover+mutation
        population.getChromosomes()
                .sort(Comparator.comparingDouble(
                        Chromosome::getFitnessValue).reversed());

        List<Chromosome> newGeneration=new ArrayList<>();
        newGeneration.add(population.getChromosomes().get(0));

        double currentBest=population.getChromosomes().get(0).getFitnessValue();
        fitnessHistory.add(currentBest);
        generationExecuted=i+1;
            notifyProgressListener("Generation " + (i + 1)
                    + " | Best Fitness: "
                    + String.format("%.4f", currentBest));
        if(currentBest>bestFitness){
            bestFitness=currentBest;
            segmentCount=0;
        }
        else
            segmentCount++;

        if(segmentCount>=maxSegmentCount){
            convergedEarly=true;
            notifyProgressListener("Early convergence detected.");
            break;
        }


        while(newGeneration.size()<population.getChromosomes().size()){

            Chromosome parent1=selection.selectTournament(population);
            Chromosome parent2=selection.selectTournament(population);

            // crossover
            Chromosome child=crossover.crossover(parent1,parent2);

            //mutation
            child=mutation.mutate(child,geneWeihtedMap);

            newGeneration.add(child);
        }
           population=new Population(newGeneration);

        }

        fitnessEvaluator.populationFitness(population, metrics);
        population.getChromosomes()
                .sort(Comparator.comparingDouble(
                        Chromosome::getFitnessValue).reversed());


        Chromosome best = population.getChromosomes().get(0);

        notifyProgressListener("--------------------------------------");
        notifyProgressListener("Optimization Completed.");
        notifyProgressListener("Best Fitness Achieved: "
                + String.format("%.4f",
                best.getFitnessValue()));
        notifyProgressListener("Best Strategy: "
                + best.getGenesList());

       return new GAResult(metrics,getAfterMetrics(metrics,best),best,generationExecuted,convergedEarly,best.getFitnessValue(),fitnessHistory);
    }


    private Metrics getAfterMetrics(Metrics metrics,Chromosome chromosome) {
//        Set<Genes> Genes= new HashSet<>(chromosome.getGenesList());
        long loc=metrics.getTotalLoc();
        double avgMethodLoc=metrics.getAverageMethodLoc();
        int deps=metrics.getTotalDependency();

        for(Genes gene:chromosome.getGenesList()){
            loc*=gene.getLocFactor();
            avgMethodLoc*=gene.getMethodLengthFactor();
            deps*=gene.getDependencyFactor();
        }
        return new Metrics(metrics.getTotalClass(),loc,metrics.getAverageLoc(),metrics.getTotalMethods(),avgMethodLoc,deps,metrics.getTotalCircularDependencies(),metrics.getTotalUnusedClasses(),metrics.getTotalFanIn());


    }


    private void notifyProgressListener(String message) {
        if(progressListener!=null){
            progressListener.accept(message);
        }
    }



}
