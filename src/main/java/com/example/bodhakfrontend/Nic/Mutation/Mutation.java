package com.example.bodhakfrontend.Nic.Mutation;

import com.example.bodhakfrontend.Nic.Model.Chromosome;
import com.example.bodhakfrontend.Nic.Model.Genes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Mutation {

    private final Random  random = new Random();
    private final double mutationRate;

    public Mutation(double mutationRate) {
        this.mutationRate = mutationRate;
    }


    private Genes selectWeightedGene(Map<Genes, Double> genesToWeight){

        double random = Math.random();
        double com=0.0;
        for(Map.Entry<Genes, Double> entry:genesToWeight.entrySet()){
            com+=entry.getValue();
            if(random<=com){
                return entry.getKey();
            }
        }
        return genesToWeight.keySet().iterator().next();

    }

    public Chromosome mutate(Chromosome chromosome,Map<Genes, Double> genesToWeight){

        List<Genes> newGenes=new ArrayList<>(chromosome.getGenesList());
        if(random.nextDouble()<mutationRate){
            int mutatedGeneIndex=random.nextInt(newGenes.size());
            Genes newGene= selectWeightedGene(genesToWeight);
            newGenes.set(mutatedGeneIndex,newGene);
        }
    return  new Chromosome(newGenes);

    }

}
