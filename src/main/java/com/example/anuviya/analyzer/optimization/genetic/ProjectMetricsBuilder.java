package com.example.anuviya.analyzer.optimization.genetic;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.ir.declaration.CallableDeclaration;
import com.example.anuviya.analyzer.optimization.model.Metrics;

import java.util.HashSet;
import java.util.Set;

public class ProjectMetricsBuilder {

    public Metrics getProjectMetrics(AnalysisContext context) {

        long totalLines = 0;
        int totalMethods = 0;
        long totalMethodsLoc = 0;
        long totalFanIn = 0;
        int totalClassdeps=0;

        int maxLoc = Integer.MIN_VALUE;
        int minLoc = Integer.MAX_VALUE;

        int maxMethodsLoc = Integer.MIN_VALUE;
        int minMethodsLoc = Integer.MAX_VALUE;

        int maxDeps = Integer.MIN_VALUE;
        int minDeps = Integer.MAX_VALUE;

        Set<String> deps = new HashSet<>();
        Set<Set<String>> uniqueCircularDeps = new HashSet<>();

        for (EntityInfo classes : context.getEntities()) {

            int classLoc = classes.getLinesOfCode();

            maxLoc = Math.max(maxLoc, classLoc);
            minLoc = Math.min(minLoc, classLoc);

            totalLines += classLoc;
            totalFanIn += classes.getUsedBy().size();

            totalMethods += classes.getStructure().callableDeclarations().size();

            uniqueCircularDeps.addAll(classes.getCircularGroups());

            for (CallableDeclaration method : classes.getStructure().callableDeclarations()) {

                int methodLoc = 0;
                if (method.body() instanceof com.example.anuviya.ir.statement.BlockStatement bs) {
                    methodLoc = bs.statements().size();
                }

                maxMethodsLoc = Math.max(maxMethodsLoc, methodLoc);
                minMethodsLoc = Math.min(minMethodsLoc, methodLoc);

                totalMethodsLoc += methodLoc;
            }

            int classDeps = classes.getDependsOn().size();
            totalClassdeps += classDeps;

            maxDeps = Math.max(maxDeps, classDeps);
            minDeps = Math.min(minDeps, classDeps);

            for (String dep : classes.getDependsOn()) {
                deps.add(classes.getEntityName() + " -> " + dep);
            }
        }

        int totalDependencies = deps.size();
        int totalCircularDependencies = uniqueCircularDeps.size();
        int totalUnusedClasses = (int) context.getEntities().stream()
                .filter(e -> e.getUsedBy().isEmpty())
                .count();

        int totalClasses = context.getEntities().size();
        double avgDeps=totalClasses == 0? 0:
                totalClassdeps/(double)totalClasses;

        double avgLoc = totalClasses == 0 ? 0 :
                totalLines / (double) totalClasses;

        double avgMethodLength = totalMethods == 0 ? 0 :
                (double) totalMethodsLoc / totalMethods;


        return new Metrics(
                totalClasses,
                totalLines,
                avgLoc,
                totalMethods,
                avgMethodLength,
                avgDeps,
                totalDependencies,
                totalCircularDependencies,
                totalUnusedClasses,
                totalFanIn,
                maxLoc,
                minLoc,
                maxMethodsLoc,
                minMethodsLoc,
                maxDeps,
                minDeps
        );
    }







}
