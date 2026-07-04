package com.example.bodhak.analyzer.optimization.engine;

import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.ir.declaration.CallableDeclaration;
import com.example.bodhak.analyzer.optimization.model.Genes;
import com.example.bodhak.analyzer.optimization.model.Metrics;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LookUpClasses {

    public record ClasseSuggestion(EntityInfo classInfo , Set<Genes> forSuggestion) { }

    public Set<ClasseSuggestion> getClassSuggestion(List<EntityInfo> classes, Metrics metrics) {
        Set<ClasseSuggestion> suggestions = new HashSet<>();
        // threshold for methodLoc
        double methodLocThreshold=Math.max(20,metrics.getAverageMethodLoc()*1.5);
        double classLocThreshold=Math.max(250,metrics.getAverageLoc()*1.5);
        double depsThreshold=Math.max(10,((double) metrics.getTotalDependency() /classes.size())*1.5 );
        double FanInThreshold=Math.max(5,((double) metrics.getTotalFanIn()/classes.size()) *1.5);
        double smallMethodThreshold=Math.min(5,metrics.getAverageMethodLoc()*0.5);


        for (EntityInfo classInfo : classes) {
            Set<Genes> forSuggestion=new HashSet<>();
            for(CallableDeclaration methodInfo : classInfo.getStructure().callableDeclarations()) {
                int stmtCount = 0;
                if (methodInfo.body() instanceof com.example.bodhak.ir.statement.BlockStatement bs) {
                    stmtCount = bs.statements().size();
                }
                if(stmtCount>methodLocThreshold) {
                    forSuggestion.add(Genes.DECOMPOSE_LONG_FUNCTION);
                }
                else if(stmtCount<smallMethodThreshold) {
                    forSuggestion.add(Genes.INLINE_METHOD);
                }

            }
            int depsCount=classInfo.getDependsOn().size()+classInfo.getUsedBy().size();
            if(depsCount>depsThreshold) {
                forSuggestion.add(Genes.IMPROVE_MODULARITY);
            }
            if (classInfo.getUsedBy().size()>FanInThreshold) {
                forSuggestion.add(Genes.EXTRACT_INTERFACE);
            }
            // split class is not handled yet


            // circular dependencies

            if(!classInfo.getCircularGroups().isEmpty()){
                forSuggestion.add(Genes.REDUCE_CIRCULAR_DEPENDENCY);
            }
            if (!forSuggestion.isEmpty()) {
                suggestions.add(new ClasseSuggestion(classInfo, forSuggestion));
            }
        }
         return suggestions;
    }
}
