package com.example.bodhakfrontend.Nic.Builder;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.Nic.Model.Metrics;

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

        for (ClassInfo classes : projectInfo.getClassInfos()) {

            int classLoc = classes.getLinesOfCode();

            maxLoc = Math.max(maxLoc, classLoc);
            minLoc = Math.min(minLoc, classLoc);

            totalLines += classLoc;
            totalFanIn += classes.getUsedBy().size();

            totalMethods += classes.getMethods().size();

            uniqueCircularDeps.addAll(classes.getCircularDependencyGroups());

            for (MethodInfo method : classes.getMethods()) {

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
                deps.add(classes.getClassName() + " -> " + dep);
            }
        }

        int totalDependencies = deps.size();
        int totalCircularDependencies = uniqueCircularDeps.size();
        int totalUnusedClasses = projectInfo.getUnusedClassInfos().size();

        int totalClasses = projectInfo.getClassInfos().size();
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
