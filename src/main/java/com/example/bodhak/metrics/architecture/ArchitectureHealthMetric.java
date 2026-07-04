package com.example.bodhak.metrics.architecture;


import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.metrics.Metric;
import com.example.bodhak.context.GraphSnapshot;

public class ArchitectureHealthMetric implements Metric<Void> {

    @Override
    public double calculate(Void target, AnalysisContext context) {

        GraphSnapshot snapshot =
                context.getDependencyGraph()
                        .snapshot();
        int cycles = snapshot.circularGroups().size();

        int dependencies =snapshot.globalDependencies().values().stream().mapToInt(java.util.Set::size).sum();

        double score = 100;

        score -= cycles * 15;

        if (dependencies > 200) {
            score -= 10;
        }

        return Math.max(score, 0);
    }
}
