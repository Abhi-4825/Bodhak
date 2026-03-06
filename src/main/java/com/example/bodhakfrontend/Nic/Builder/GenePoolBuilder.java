package com.example.bodhakfrontend.Nic.Builder;

import com.example.bodhakfrontend.Nic.LookUpClasses;
import com.example.bodhakfrontend.Nic.Model.Genes;
import com.example.bodhakfrontend.Nic.Model.Metrics;

import java.util.*;

public class GenePoolBuilder {
    public Map<Genes, Double> getWeightedGenePool(
            Metrics metrics,
            Set<LookUpClasses.ClasseSuggestion> classes) {

        Map<Genes, Integer> severityCount = new HashMap<>();


        if (metrics.getTotalUnusedClasses() > 0) {
            severityCount.merge(
                    Genes.REMOVE_UNUSED_CODE,
                    metrics.getTotalUnusedClasses(),
                    Integer::sum
            );
        }
        for (LookUpClasses.ClasseSuggestion suggestion : classes) {

            for (Genes gene : suggestion.forSuggestion()) {
                severityCount.merge(gene, 1, Integer::sum);
            }
        }
        int total = severityCount.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        Map<Genes, Double> weightedPool = new HashMap<>();

        for (Map.Entry<Genes, Integer> entry : severityCount.entrySet()) {

            double probability =
                    total == 0 ? 0.0 :
                            (double) entry.getValue() / total;

            weightedPool.put(entry.getKey(), probability);
        }

        return weightedPool;
    }


}
