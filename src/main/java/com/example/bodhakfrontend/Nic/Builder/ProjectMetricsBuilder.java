package com.example.bodhakfrontend.Nic.Builder;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.Nic.Model.Metrics;

import java.util.HashSet;
import java.util.Set;

public class ProjectMetricsBuilder {

    public Metrics getProjectMetrics(ProjectInfo projectInfo)
    {
        long totalLines = 0;
        int totalMethods = 0;
        long totalMethodsLoc = 0;
        long totalFanIn = 0;
        Set<String> deps = new HashSet<String>();
        Set<Set<String>> uniqueCircularDeps=new HashSet<>();
        for(ClassInfo classes:projectInfo.getClassInfos()){

            totalLines += classes.getLinesOfCode();
            totalFanIn+=classes.getUsedBy().size();

            totalMethods +=classes.getMethods().size();

            uniqueCircularDeps.addAll(classes.getCircularDependencyGroups());

            for(MethodInfo method:classes.getMethods()){
                totalMethodsLoc+=method.getStatementCount();
            }

            for(String dep:classes.getDependsOn()){
                deps.add(classes.getClassName()+ " -> " +dep);
            }

        }
        int totalDependencies = deps.size();
        int totalCircularDependencies = uniqueCircularDeps.size();
        int totalUnusedClasses = projectInfo.getUnusedClassInfos().size();
        double avgLoc= totalLines/ (double) projectInfo.getClassInfos().size();
        long totalClass=(long)projectInfo.getClassInfos().size();

        double avgMethodLength= totalMethods==0?0: (double) totalMethodsLoc /totalMethods;
      return new Metrics(totalClass,totalLines,avgLoc,totalMethods,avgMethodLength,totalDependencies,totalCircularDependencies,totalUnusedClasses,totalFanIn);

    }







}
