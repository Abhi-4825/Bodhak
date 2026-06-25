package com.example.bodhakfrontend.engine.optimization.Suggestions;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.MemberInfo;
import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.engine.optimization.Model.GAResult;
import com.example.bodhakfrontend.engine.optimization.Model.Genes;
import com.example.bodhakfrontend.engine.optimization.Model.Metrics;
import com.example.bodhakfrontend.engine.optimization.Model.RefactoringSuggestion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RefactoringSuggestionEngine {

    public List<RefactoringSuggestion> generateSuggestions(GAResult result, AnalysisContext context, Metrics metrics) {
        List<RefactoringSuggestion> suggestions = new ArrayList<>();
        for(Genes genes:new HashSet<>(result.bestChromosome().getGenesList())){
            switch (genes){
                case DECOMPOSE_LONG_FUNCTION ->  suggestions.addAll(findLongFunctions(context,metrics));
                case SPLIT_CLASS -> suggestions.addAll(findLargestClasses(context,metrics));
                case EXTRACT_INTERFACE -> {}
                case REDUCE_CIRCULAR_DEPENDENCY -> suggestions.addAll(findCircularDependencies(context));
                case INLINE_METHOD -> suggestions.addAll(findTinyMethods(context));
                case IMPROVE_MODULARITY ->{}
                case REMOVE_UNUSED_CODE ->{}
            }
        }
        return  suggestions;
    }

    private List<RefactoringSuggestion> findLongFunctions(AnalysisContext context,Metrics metrics) {

        List<RefactoringSuggestion> suggestions = new ArrayList<>();
        double threshold=metrics.getAverageMethodLoc();
        for(EntityInfo clazz:context.getEntities()){
            for(MemberInfo methodInfo:clazz.getMembers()){
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


    private List<RefactoringSuggestion> findLargestClasses(AnalysisContext context,Metrics metrics) {
         List<RefactoringSuggestion> suggestions = new ArrayList<>();
         double threshold=metrics.getAverageLoc();
         for(EntityInfo clazz:context.getEntities()){
             if(clazz.getLinesOfCode()>threshold){
                 suggestions.add(new RefactoringSuggestion(clazz,null,clazz + " has "+clazz.getLinesOfCode()+ " lines","Split Large Class","Consider separating responsibilities into multiple classes"));
             }
         }

         return suggestions.stream().sorted((a,b)->Integer.compare(
                 b.getClazz().getLinesOfCode(),a.getClazz().getLinesOfCode()
         )).limit(5).toList();

    }

    private List<RefactoringSuggestion>  findCircularDependencies(AnalysisContext context){
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

       for(EntityInfo clazz:context.getEntities()){
           if(!clazz.getCircularGroups().isEmpty()) {
               suggestions.add(new RefactoringSuggestion(clazz,null,clazz+" has circular Dependencies","Fix Circular dependencies", "Consider introducing an interface or restructuring dependencies"));
           }
       }
      return suggestions;
    }

    private List<RefactoringSuggestion> findTinyMethods(AnalysisContext context){
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

        for(EntityInfo clazz:context.getEntities()){
            for(MemberInfo methodInfo:clazz.getMembers()){
                if(methodInfo.getStatementCount()<=3){
                    suggestions.add(new RefactoringSuggestion(clazz,methodInfo,"Method is very small (" + methodInfo.getStatementCount() + " LOC)","Inline Method", "Consider inlining this method into the caller"));
                }
            }
        }
     return suggestions.stream().limit(5).toList();

    }


}
