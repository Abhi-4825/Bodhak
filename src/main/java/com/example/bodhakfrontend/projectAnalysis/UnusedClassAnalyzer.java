package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Models.PackageAnalysis.EntryPointInfo;

import java.util.*;

public class UnusedClassAnalyzer {

    public List<UnusedClassInfo> analyze(
            List<ClassInfo> classes,
            ClassDependencyInfo dependencyInfo,
            EntryPointInfo entryPointInfo
    ) {

        List<UnusedClassInfo> result = new ArrayList<>();
// Collect all roots
        Set<String> roots = new HashSet<>();
        if (entryPointInfo.getPrimaryEntry() != null) {
            roots.add(entryPointInfo.getPrimaryEntry());
        }
        roots.addAll(entryPointInfo.getSecondaryEntries());
        roots.addAll(entryPointInfo.getFrameworkRoots());

        //Reachability analysis
        Set<String> reachable=findReachableClasses(roots,dependencyInfo.getClassDependencies());
        Set<String > cycleClasses=dependencyInfo.getClassesInCycles();


        // class Classification
        for (ClassInfo cls : classes) {
            String name = cls.getName();
            DependencyNode dependencyNode=dependencyInfo.getClassInfo().get(name);


            boolean isReachable=reachable.contains(name);

            boolean isFrameworkClass=isFrameworkAnnotated(cls);
            boolean hasLogic=hasBehavior(cls);


            UsageStatus status;
            String reason;
            // for cycles
            boolean inCycle=cycleClasses.contains(name);
            boolean reachableCycle=inCycle && reachable.contains(name);
            if(isReachable || reachableCycle){
                status = UsageStatus.USED;
                reason = "Possibly reachable from entry Point or cycle";
            }
            else if(isFrameworkClass && hasLogic){
                status = UsageStatus.FRAMEWORK_REACHABLE;
                reason = "Framework-managed class";
            }
            else if(isFrameworkClass){
                status = UsageStatus.SUSPICIOUS;
                reason = "Framework annotation but no behavior detected";

            }
            else{
                status = UsageStatus.UNUSED;
                reason = "Possibly No reference or frameWork Usage";

            }
            if(status != UsageStatus.USED && status != UsageStatus.FRAMEWORK_REACHABLE){
                result.add(
                        new  UnusedClassInfo(
                                name,dependencyNode, cls.getPkg(), cls.getLinesOfCode(), status,reason
                        )
                );
            }




        }
        return result;
        }


        private Set<String> findReachableClasses(Set<String> roots, Map<String, Set<String>> classDependencies) {
        Set<String> visited = new HashSet<>();
            Deque<String> stack = new ArrayDeque<>(roots);
        while(!stack.isEmpty()){
            String current=stack.pop();
            if(!visited.add(current)){continue;}
            for(String dep:classDependencies.getOrDefault(current,Set.of())){
            stack.push(dep);}

        }
        return visited;

        }
    // ---------- Framework detection ----------
    private boolean isFrameworkAnnotated(ClassInfo cls) {
        return cls.hasAnyAnnotation(
                "RestController",
                "Controller",
                "Service",
                "Component",
                "Repository",
                "Configuration",
                "Entity",
                "Scheduled",
                "EventListener"
        );
    }
    private boolean hasBehavior(ClassInfo cls) {
        return cls.getMethodCount()>0 ||
                cls.getFieldCount()>0  ||
                cls.hasAnyAnnotation(
                        "Bean",
                        "Autowired",
                        "Value",
                        "PostConstruct"
                );
    }

    }


