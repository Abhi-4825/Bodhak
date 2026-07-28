package com.example.anuviya.analyzer.growth.rule;

import com.example.anuviya.model.diagnostic.AnalysisCategory;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.diagnostic.AnalysisIssue;
import com.example.anuviya.model.diagnostic.AnalysisSeverity;
import com.example.anuviya.analyzer.growth.algorithm.SccAnalyzer;
import com.example.anuviya.analyzer.growth.model.SccCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonolithicClusterGrowthRule
        implements GrowthRule {

    private static final int CLUSTER_THRESHOLD = 10;

    private final SccAnalyzer analyzer =
            new SccAnalyzer();

    @Override
    public List<AnalysisIssue> analyze(
            AnalysisContext context
    ) {

        List<AnalysisIssue> issues =
                new ArrayList<>();

        List<SccCluster> clusters =
                analyzer.analyze(context);

        for (SccCluster cluster : clusters) {

            if (cluster.size() < CLUSTER_THRESHOLD) {
                continue;
            }

            issues.add(
                    createIssue(cluster)
            );
        }

        return issues;
    }

    private AnalysisIssue createIssue(
            SccCluster cluster
    ) {

        return new AnalysisIssue(

                "Monolithic Cluster Growth Risk",

                "A strongly connected cluster containing "
                        + cluster.size()
                        + " entities was detected. "
                        + "Changes inside this region may "
                        + "propagate across the entire cluster, "
                        + "making future evolution and "
                        + "modularization difficult.",

                determineSeverity(
                        cluster.size()
                ),

                AnalysisCategory.SCALABILITY,

                new ArrayList<>(
                        cluster.nodes()
                ),

                Map.of(
                        "clusterSize",
                        (double) cluster.size()
                ),

                List.of(
                        "Reduce bidirectional dependencies.",
                        "Extract shared services behind interfaces.",
                        "Introduce clearer architectural boundaries.",
                        "Split the cluster into smaller modules."
                ),
                Map.of(
                        "growthFactor",
                        "PROJECT_SIZE",

                        "riskType",
                        "MONOLITHIC_CLUSTER",

                        "clusterId",
                        cluster.clusterId(),

                        "riskLevel",
                        determineSeverity(
                                cluster.size()
                        ).name()
                )


        );
    }

    private AnalysisSeverity determineSeverity(
            int clusterSize
    ) {

        if (clusterSize >= 20) {
            return AnalysisSeverity.HIGH;
        }

        if (clusterSize >= 10) {
            return AnalysisSeverity.MEDIUM;
        }

        return AnalysisSeverity.LOW;
    }
}
