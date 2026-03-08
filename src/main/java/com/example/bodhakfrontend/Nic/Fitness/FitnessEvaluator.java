package com.example.bodhakfrontend.Nic.Fitness;

import com.example.bodhakfrontend.Nic.Model.Chromosome;
import com.example.bodhakfrontend.Nic.Model.Genes;
import com.example.bodhakfrontend.Nic.Model.Metrics;
import com.example.bodhakfrontend.Nic.Model.Population;

import java.util.HashSet;
import java.util.Set;

public class FitnessEvaluator {
    private final FitnessWeight fitnessWeight;
    public FitnessEvaluator(FitnessWeight fitnessWeight) {
        this.fitnessWeight = fitnessWeight;
    }



       public void populationFitness(Population population,Metrics metrics) {
        for(Chromosome chromosome : population.getChromosomes()) {
            double fitness=getChromosomeFitness(chromosome,metrics);
            chromosome.setFitnessValue(fitness);
        }

       }


       private double getChromosomeFitness(Chromosome chromosome, Metrics metrics) {
           double loc=metrics.getTotalLoc();
           double avgMethodsLoc=metrics.getAverageMethodLoc();
           double deps=metrics.getTotalDependency();
//           Set<Genes> uniqueGenes=new HashSet<>(chromosome.getGenesList());

           for(Genes gene:chromosome.getGenesList()) {
               loc*=gene.getLocFactor();
               avgMethodsLoc*=gene.getMethodLengthFactor();
               deps*=gene.getDependencyFactor();
           }

           // for safety
           loc=Math.max(loc,0);
           avgMethodsLoc=Math.max(avgMethodsLoc,0);
           deps=Math.max(deps,0);


           // now we will compute improvement ratio

           double locImprovement= metrics.getTotalLoc()==0?0:(metrics.getTotalLoc()-loc)/metrics.getTotalLoc();
           double avgMethodsImprovement=metrics.getAverageMethodLoc()==0?0:(metrics.getAverageMethodLoc()-avgMethodsLoc)/metrics.getAverageMethodLoc();
           double depsImprovement=metrics.getTotalDependency()==0?0:(metrics.getTotalDependency()-deps)/metrics.getTotalDependency();

         //return the fitness

           return fitnessWeight.getLocWeight()*locImprovement+
                   fitnessWeight.getMethodWeight()*avgMethodsImprovement+
                   fitnessWeight.getDependencyWeight()*depsImprovement;

       }



}
