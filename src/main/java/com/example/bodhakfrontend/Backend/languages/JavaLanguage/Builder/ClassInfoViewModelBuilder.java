package com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder;

import com.example.bodhakfrontend.Backend.ClassGraphBuilder;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.incrementalModel.ClassInfoViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassInfoViewModelBuilder {

    private final ClassGraphBuilder dependencyGraphBuilder;

    private final Map<String, ClassInfoViewModel> vmMap = new HashMap<>();

    public ClassInfoViewModelBuilder(
            ClassGraphBuilder dependencyGraphBuilder
    ) {
        this.dependencyGraphBuilder = dependencyGraphBuilder;
    }

    /* ==========================================================
       INITIAL BUILD
       ========================================================== */

    public Map<String, ClassInfoViewModel> initialBuild(List<ClassInfo> classes) {
        for (ClassInfo info : classes) {
            vmMap.computeIfAbsent(
                    info.getClassName(),
                    k -> new ClassInfoViewModel(info)
            );
        }

        refreshDependencies();
        return vmMap;
    }

    public Map<String, ClassInfoViewModel> getClassInfoViewModelMap() {
        return vmMap;
    }


    public void onFileCreate(List<ClassInfo> classes) {
        for (ClassInfo info : classes) {
            vmMap.computeIfAbsent(
                    info.getClassName(),
                    k -> new ClassInfoViewModel(info)
            );
        }

        refreshDependencies();
    }



    public void onFileModify(
            List<ClassInfo> oldClasses,
            List<ClassInfo> newClasses
    ) {
        Map<String, ClassInfo> newMap = new HashMap<>();
        for (ClassInfo c : newClasses) {
            newMap.put(c.getClassName(), c);
        }

        for (ClassInfo old : oldClasses) {
            if (!newMap.containsKey(old.getClassName())) {
                vmMap.remove(old.getClassName());
            }
        }


        for (ClassInfo info : newClasses) {
            vmMap.computeIfAbsent(
                    info.getClassName(),
                    k -> new ClassInfoViewModel(info)
            ).updateFrom(info);
        }

        refreshDependencies();
    }



    public void onFileDelete(List<ClassInfo> classes) {
        for (ClassInfo info : classes) {
            vmMap.remove(info.getClassName());
        }

        refreshDependencies();
    }



    private void refreshDependencies() {

        // dependsOn map (forward)
        Map<String, Set<String>> dependsOnMap =
                dependencyGraphBuilder.getDependsOn();

        // usedBy map (reverse)
        Map<String, Set<String>> usedByMap =
                dependencyGraphBuilder.getReverseClassDependencies();

        vmMap.forEach((className, vm) -> {

            // ---- dependsOn ----
            Set<String> deps =
                    dependsOnMap.getOrDefault(className, Set.of());
            vm.getDependsOn().clear();
            vm.getDependsOn().addAll(deps);

            // ---- usedBy ----
            Set<String> users =
                    usedByMap.getOrDefault(className, Set.of());
            vm.getUsedBy().clear();
            vm.getUsedBy().addAll(users);
        });
    }





}
