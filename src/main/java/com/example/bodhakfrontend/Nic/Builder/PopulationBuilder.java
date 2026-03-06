package com.example.bodhakfrontend.Nic.Builder;

import com.example.bodhakfrontend.Nic.Model.Chromosome;
import com.example.bodhakfrontend.Nic.Model.Genes;
import com.example.bodhakfrontend.Nic.Model.Population;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PopulationBuilder {

    private final ChromosomeBuilder chromosomeBuilder=new ChromosomeBuilder();
    public Population createInitialPopulation(Map<Genes,Double> weightedGenePool,int chromosomeLength, int populationSize)
    {

        if (weightedGenePool == null || weightedGenePool.isEmpty()) {
            throw new IllegalArgumentException("Weighted gene pool cannot be empty.");
        }
        if (chromosomeLength <= 0) {
            throw new IllegalArgumentException("Chromosome length must be positive.");
        }
        if (populationSize <= 0) {
            throw new IllegalArgumentException("Population size must be positive.");
        }
        List<Chromosome> population=new ArrayList<Chromosome>();
          for(int i=0;i<populationSize;i++){
              population.add(chromosomeBuilder.getChromosome(weightedGenePool,chromosomeLength));
          }
        return new Population(population);
    }


}
