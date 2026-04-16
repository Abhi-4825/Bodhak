package com.example.bodhakfrontend.Nic.Builder;

import com.example.bodhakfrontend.Nic.Model.Chromosome;
import com.example.bodhakfrontend.Nic.Model.Genes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChromosomeBuilder {

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


    // generate one chromosome

    public Chromosome getChromosome(Map<Genes, Double> genesToWeight,int chromosomeLength){
        List<Genes> genes = new ArrayList<Genes>();

        for(int i=0;i<chromosomeLength;i++){
            genes.add(selectWeightedGene(genesToWeight));
        }
        return new Chromosome(genes);
    }


}
