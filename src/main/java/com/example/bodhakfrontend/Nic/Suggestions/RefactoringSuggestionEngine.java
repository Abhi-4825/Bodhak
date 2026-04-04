package com.example.bodhakfrontend.Nic.Suggestions;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Class.MethodInfo;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;
import com.example.bodhakfrontend.Nic.Model.GAResult;
import com.example.bodhakfrontend.Nic.Model.Genes;
import com.example.bodhakfrontend.Nic.Model.Metrics;
import com.example.bodhakfrontend.Nic.Model.RefactoringSuggestion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RefactoringSuggestionEngine {

    public List<RefactoringSuggestion> generateSuggestions(GAResult result, ProjectInfo projectInfo, Metrics metrics) {
        List<RefactoringSuggestion> suggestions = new ArrayList<>();
        for(Genes genes:new HashSet<>(result.bestChromosome().getGenesList())){
            switch (genes){
                case DECOMPOSE_LONG_FUNCTION ->  suggestions.addAll(findLongFunctions(projectInfo,metrics));
                case SPLIT_CLASS -> suggestions.addAll(findLargestClasses(projectInfo,metrics));
                case EXTRACT_INTERFACE -> {}
                case REDUCE_CIRCULAR_DEPENDENCY -> suggestions.addAll(findCircularDependencies(projectInfo));
                case INLINE_METHOD -> suggestions.addAll(findTinyMethods(projectInfo));
                case IMPROVE_MODULARITY ->{}
                case REMOVE_UNUSED_CODE ->{}
            }
        }
        return  suggestions;
    }

    private List<RefactoringSuggestion> findLongFunctions(ProjectInfo projectInfo,Metrics metrics) {

        List<RefactoringSuggestion> suggestions = new ArrayList<>();
        double threshold=metrics.getAverageMethodLoc();
        for(ClassInfo clazz:projectInfo.getClassInfos()){
            for(MethodInfo methodInfo:clazz.getMethods()){
                if(methodInfo.getStatementCount()>threshold){
                    suggestions.add(new RefactoringSuggestion(clazz,methodInfo,"Method/Function is "+methodInfo.getStatementCount() + " LOC long","Break Large Functions/Methods", "Split this method into smaller helper methods"));
                }
            }
        }

        return suggestions.stream()
                .sorted((a, b) -> Integer.compare(
                       b.getMethod().getStatementCount(),a.getMethod().getStatementCount()
                ))
                .limit(5)
                .toList();


    }


    private List<RefactoringSuggestion> findLargestClasses(ProjectInfo projectInfo,Metrics metrics) {
         List<RefactoringSuggestion> suggestions = new ArrayList<>();
         double threshold=metrics.getAverageLoc();
         for(ClassInfo clazz:projectInfo.getClassInfos()){
             if(clazz.getLinesOfCode()>threshold){
                 suggestions.add(new RefactoringSuggestion(clazz,null,clazz + " has "+clazz.getLinesOfCode()+ " lines","Split Large Class","Consider separating responsibilities into multiple classes"));
             }
         }

         return suggestions.stream().sorted((a,b)->Integer.compare(
                 b.getClazz().getLinesOfCode(),a.getClazz().getLinesOfCode()
         )).limit(5).toList();

    }

    private List<RefactoringSuggestion>  findCircularDependencies(ProjectInfo projectInfo){
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

       for(ClassInfo clazz:projectInfo.getClassesInCycles()){
           suggestions.add(new RefactoringSuggestion(clazz,null,clazz+" has circular Dependencies","Fix Circular dependencies", "Consider introducing an interface or restructuring dependencies"));
       }
      return suggestions;
    }

    private List<RefactoringSuggestion> findTinyMethods(ProjectInfo projectInfo){
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

        for(ClassInfo clazz:projectInfo.getClassInfos()){
            for(MethodInfo methodInfo:clazz.getMethods()){
                if(methodInfo.getStatementCount()<=3){
                    suggestions.add(new RefactoringSuggestion(clazz,methodInfo,"Method is very small (" + methodInfo.getStatementCount() + " LOC)","Inline Method", "Consider inlining this method into the caller"));
                }
            }
        }
     return suggestions.stream().limit(5).toList();

    }


}
