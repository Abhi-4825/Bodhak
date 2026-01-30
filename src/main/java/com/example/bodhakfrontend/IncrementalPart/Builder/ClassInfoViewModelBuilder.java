package com.example.bodhakfrontend.IncrementalPart.Builder;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.IncrementalPart.model.incrementalModel.ClassInfoViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassInfoViewModelBuilder {
    private final ClassDependecygraphBuilder classDependecygraphBuilder;
    public Map<String, ClassInfoViewModel> classInfoViewModelMap = new HashMap<String,ClassInfoViewModel>();

    public ClassInfoViewModelBuilder(ClassDependecygraphBuilder classDependecygraphBuilder) {
        this.classDependecygraphBuilder = classDependecygraphBuilder;
    }


    public Map<String,ClassInfoViewModel> initialBuild(ProjectInfo projectInfo){
        List<ClassInfo> classes=projectInfo.getClassInfos();
        for(ClassInfo classInfo:classes){
            classInfoViewModelMap.computeIfAbsent(classInfo.getClassName(),key->new ClassInfoViewModel(classInfo));
        }
        return  classInfoViewModelMap;
    }

    // get the map

    public Map<String,ClassInfoViewModel> getClassInfoViewModelMap(){
        return classInfoViewModelMap;
    }

    // on File update
    public void onFileCreate(List<ClassInfo> classes){
       for(ClassInfo classInfo:classes){
           classInfoViewModelMap.computeIfAbsent(classInfo.getClassName(),key->new ClassInfoViewModel(classInfo));
       }
       updateUsedByDeps();
    }

    // on File Modify
    public void onFileModify(
            List<ClassInfo> oldClasses,
            List<ClassInfo> newClasses
    ) {
        Map<String, ClassInfo> oldMap = new HashMap<>();
        for (ClassInfo c : oldClasses) {
            oldMap.put(c.getClassName(), c);
        }

        Map<String, ClassInfo> newMap = new HashMap<>();
        for (ClassInfo c : newClasses) {
            newMap.put(c.getClassName(), c);
        }

        // 1 REMOVE deleted classes
        for (String oldName : oldMap.keySet()) {
            if (!newMap.containsKey(oldName)) {
                classInfoViewModelMap.remove(oldName);
            }
        }

        // 2 UPDATE existing + ADD new
        for (ClassInfo newClass : newClasses) {
            classInfoViewModelMap
                    .computeIfAbsent(
                            newClass.getClassName(),
                            k -> new ClassInfoViewModel(newClass)
                    )
                    .updateFrom(newClass);

        }
        updateUsedByDeps();





    }
    // on file delete
    public void onFileDelete(List<ClassInfo> classes){
         for(ClassInfo classInfo:classes){
             classInfoViewModelMap.remove(classInfo.getClassName());
         }
         updateUsedByDeps();
    }

    // update all classes used by Dependencies
    private void updateUsedByDeps(){
        Map<String, Set<String>> usedByDepsMap = classDependecygraphBuilder.getReverseClassDependencies();
        classInfoViewModelMap.forEach((className,vm)->{
            Set<String> usedByDeps = usedByDepsMap.getOrDefault(className,Set.of());
            vm.getUsedBy().clear();
            vm.getUsedBy().addAll(usedByDeps);
        });

    }




}
