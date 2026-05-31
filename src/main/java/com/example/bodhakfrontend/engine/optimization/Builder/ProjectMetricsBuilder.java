package com.example.bodhakfrontend.engine.optimization.Builder;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.MemberInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.optimization.Model.Metrics;

import java.util.HashSet;
import java.util.Set;

public class ProjectMetricsBuilder {

    public Metrics getProjectMetrics(ProjectInfo projectInfo) {

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

        for (EntityInfo classes : projectInfo.getEntities()) {

            int classLoc = classes.getLinesOfCode();

            maxLoc = Math.max(maxLoc, classLoc);
            minLoc = Math.min(minLoc, classLoc);

            totalLines += classLoc;
            totalFanIn += classes.getUsedBy().size();

            totalMethods += classes.getMembers().size();

            uniqueCircularDeps.addAll(classes.getCircularGroups());

            for (MemberInfo method : classes.getMembers()) {

                int methodLoc = method.getStatementCount();

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
        int totalUnusedClasses = projectInfo.getUnusedEntities().size();

        int totalClasses = projectInfo.getEntities().size();
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
