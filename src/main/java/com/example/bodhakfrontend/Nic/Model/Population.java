package com.example.bodhakfrontend.Nic.Model;

import java.util.ArrayList;
import java.util.List;

public class Population {
    private final List<Chromosome> chromosomes;

    public Population(List<Chromosome> chromosomes) {
        this.chromosomes =new ArrayList<>(chromosomes);
    }
    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }


}
