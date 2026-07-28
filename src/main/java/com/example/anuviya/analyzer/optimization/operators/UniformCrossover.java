package com.example.anuviya.analyzer.optimization.operators;

import com.example.anuviya.analyzer.optimization.model.Chromosome;
import com.example.anuviya.analyzer.optimization.model.Genes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UniformCrossover {

    private final Random random = new Random();


    public Chromosome crossover(Chromosome p1, Chromosome p2) {

        List<Genes> childGenes = new ArrayList<>();
        List<Genes> parentGenes1 = p1.getGenesList();
        List<Genes> parentGenes2 = p2.getGenesList();

        for (int i = 0; i < parentGenes1.size(); i++) {
            if(random.nextBoolean()){
                childGenes.add(parentGenes1.get(i));
            }
            else{
                childGenes.add(parentGenes2.get(i));
            }

        }
        return new Chromosome(childGenes);
    }
}
