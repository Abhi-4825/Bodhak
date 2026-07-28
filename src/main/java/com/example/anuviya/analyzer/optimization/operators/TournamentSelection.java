package com.example.anuviya.analyzer.optimization.operators;

import com.example.anuviya.analyzer.optimization.model.Chromosome;
import com.example.anuviya.analyzer.optimization.model.Population;

import java.util.List;
import java.util.Random;

public class TournamentSelection {

    private final Random random = new Random();
    private  final int tournamentSize;


    public TournamentSelection(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }

    public Chromosome selectTournament(Population population) {

      List<Chromosome> chromosomes = population.getChromosomes();
      Chromosome bestChromosome = null;

      for(int i = 0; i < tournamentSize; i++){
          int randomIndex = random.nextInt(chromosomes.size());
          Chromosome chromosome = chromosomes.get(randomIndex);
          if(bestChromosome == null || chromosome.getFitnessValue()>bestChromosome.getFitnessValue()){
              bestChromosome = chromosome;
          }
      }
       return bestChromosome;

    }

}
