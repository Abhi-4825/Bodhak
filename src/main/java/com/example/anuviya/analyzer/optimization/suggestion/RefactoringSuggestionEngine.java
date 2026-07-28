package com.example.anuviya.analyzer.optimization.suggestion;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.ir.declaration.CallableDeclaration;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.analyzer.optimization.model.GAResult;
import com.example.anuviya.analyzer.optimization.model.Genes;
import com.example.anuviya.analyzer.optimization.model.Metrics;
import com.example.anuviya.analyzer.optimization.model.RefactoringSuggestion;

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
            for(CallableDeclaration methodInfo:clazz.getStructure().callableDeclarations()){
                int stmtCount = 0;
                if (methodInfo.body() instanceof com.example.anuviya.ir.statement.BlockStatement bs) {
                    stmtCount = bs.statements().size();
                }
                if(stmtCount>threshold){
                    suggestions.add(new RefactoringSuggestion(clazz,methodInfo,"Method/Function is "+stmtCount + " LOC long","Break Large Functions/Methods", "Split this method into smaller helper methods"));
                }
            }
        }

        return suggestions.stream()
                .sorted((a, b) -> {
                    int bCount = 0;
                    if (b.getMethod().body() instanceof com.example.anuviya.ir.statement.BlockStatement bbs) {
                        bCount = bbs.statements().size();
                    }
                    int aCount = 0;
                    if (a.getMethod().body() instanceof com.example.anuviya.ir.statement.BlockStatement abs) {
                        aCount = abs.statements().size();
                    }
                    return Integer.compare(bCount, aCount);
                })
                .limit(5)
                .toList();


    }


    private List<RefactoringSuggestion> findLargestClasses(AnalysisContext context,Metrics metrics) {
         List<RefactoringSuggestion> suggestions = new ArrayList<>();
         double threshold=metrics.getAverageLoc();
         for(EntityInfo clazz:context.getEntities()){
             if(clazz.getLinesOfCode()>threshold){
                 suggestions.add(new RefactoringSuggestion(clazz,null,clazz + " has "+clazz.getLinesOfCode()+ " lines","Split Class","Consider separating responsibilities into multiple classes"));
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
            for(CallableDeclaration methodInfo:clazz.getStructure().callableDeclarations()){
                int stmtCount = 0;
                if (methodInfo.body() instanceof com.example.anuviya.ir.statement.BlockStatement bs) {
                    stmtCount = bs.statements().size();
                }
                if(stmtCount<=3){
                    suggestions.add(new RefactoringSuggestion(clazz,methodInfo,"Method is very small (" + stmtCount + " LOC)","Inline Method", "Consider inlining this method into the caller"));
                }
            }
        }
     return suggestions.stream().limit(5).toList();

    }


}
