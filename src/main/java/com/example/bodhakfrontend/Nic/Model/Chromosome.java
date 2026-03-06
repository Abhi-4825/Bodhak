package com.example.bodhakfrontend.Nic.Model;

import java.util.ArrayList;
import java.util.List;

public class Chromosome {

    private final List<Genes> genesList;
    private  double fitnessValue;


    public Chromosome(List<Genes> genesList) {
        this.genesList = new ArrayList<>(genesList);
    }
    public List<Genes> getGenesList() {
        return genesList;
    }
    public void setFitnessValue(double fitnessValue) {
        this.fitnessValue = fitnessValue;
    }
    public double getFitnessValue() {
        return fitnessValue;
    }

    @Override
    public String toString() {
        return "Chromosome{" +
                "genes=" + genesList +
                ", fitness=" + fitnessValue +
                '}';
    }

}
