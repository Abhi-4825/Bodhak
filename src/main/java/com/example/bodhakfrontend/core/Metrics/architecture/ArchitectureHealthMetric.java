package com.example.bodhakfrontend.core.Metrics.architecture;


import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.Metrics.Metric;
import com.example.bodhakfrontend.engine.GraphSnapshot;

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
