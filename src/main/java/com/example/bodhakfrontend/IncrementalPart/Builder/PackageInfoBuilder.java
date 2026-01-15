package com.example.bodhakfrontend.IncrementalPart.Builder;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;

import com.example.bodhakfrontend.IncrementalPart.model.Package.PackageInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Package.PackageWarning;
import com.example.bodhakfrontend.dependency.CircularDependency;
import java.util.*;

public class PackageInfoBuilder {
    private final ClassInfoBuilder classInfoToPathMapBuilder;
    private final CircularDependency circularDependency=new CircularDependency();
    //List<PackageInfo>
    private final List<PackageInfo> packageInfos=new ArrayList<>();
    // pkg----> pkg dependencies
    Map<String,Set<String>> pkgdependencies=new HashMap<>();
    // pkg <----- pkg
    Map<String,Set<String>> revPkgDeps=new HashMap<>();

    // circular depends
    Set<Set<String>> allCircularDependencies=new HashSet<>();

    public PackageInfoBuilder(ClassInfoBuilder classInfoToPathMapBuilder) {
        this.classInfoToPathMapBuilder = classInfoToPathMapBuilder;
    }

    private List<ClassInfo> getClassInfos(){
        return classInfoToPathMapBuilder.getListOfClassInfo();
    }
    private Map<String,ClassInfo> getClassInfoMap(List<ClassInfo> classInfos){
        return classInfoToPathMapBuilder.getClassMap(classInfos);
    }
    private Map<String,Set<ClassInfo>> getPackageInfoMap(List<ClassInfo> classInfos){
        return classInfoToPathMapBuilder.getPkgToClassInfo(classInfos);
    }

    //map classname--classinfo
    private Map<String,ClassInfo> classInfoMap;

    public List<PackageInfo> build(
            List<ClassInfo> classInfos
    ) {
        if(packageInfos!=null){
            packageInfos.clear();
        }
        Map<String,Set<ClassInfo>> pkgToClasses=getPackageInfoMap(classInfos);
        classInfoMap=getClassInfoMap(classInfos);
        findPackageDependencies(classInfos);
        updateCircularDependenciesGroups();
        for(String pkg: pkgToClasses.keySet()){
            //getPkgname
            String packageName=pkg;
            //set classes
            Set<ClassInfo> classes=pkgToClasses.get(pkg);

            // depends on/used by
            Set<String> dependsOn =
                    pkgdependencies.getOrDefault(packageName, Set.of());

            Set<String> usedBy =
                    revPkgDeps.getOrDefault(packageName, Set.of());


            // find circular dependencies
            Set<Set<String>> circularGroups=new HashSet<>();
            for(Set<String> groups:allCircularDependencies){
                if(groups.contains(packageName)){
                    circularGroups.add(groups);
                }
            }

            // part of circular depends
            boolean circularDependencyFound =!circularGroups.isEmpty();


            // set of warnings

            Set<PackageWarning> warnings=new HashSet<>();

            boolean isDefaultPkg =
                    packageName == null || packageName.isBlank();

            if (isDefaultPkg) {
                warnings.add(PackageWarning.DEFAULT_PACKAGE);
            }
            // coupling
            if (!packageName.equals("default")) {
                int coupling = dependsOn.size() + usedBy.size();
                if (coupling > 6) {
                    warnings.add(PackageWarning.HIGH_COUPLING);
                }
            }

            // god package
            if (classes.size() > 20) {
                warnings.add(PackageWarning.GOD_PACKAGE);
            }



            // hub
            if (usedBy.size() >= 4
                    && dependsOn.size() <= 1) {
                warnings.add(PackageWarning.HUB_PACKAGE);
            }




            packageInfos.add(new PackageInfo(packageName,classes,dependsOn,usedBy,circularGroups,circularDependencyFound,warnings));



        }





     return packageInfos;
    }
     //depends on
     private void findPackageDependencies(List<ClassInfo> classInfos) {

         pkgdependencies.clear();
         revPkgDeps.clear();

         for (ClassInfo c : classInfos) {

             String fromPkg = c.getPackageName();

             for (String depClass : c.getDependsOn()) {

                 ClassInfo depInfo = classInfoMap.get(depClass);
                 if (depInfo == null) continue;

                 String toPkg = depInfo.getPackageName();

                 if (fromPkg.equals(toPkg)) continue;

                 // fromPkg → toPkg
                 pkgdependencies
                         .computeIfAbsent(fromPkg, k -> new HashSet<>())
                         .add(toPkg);

                 // reverse
                 revPkgDeps
                         .computeIfAbsent(toPkg, k -> new HashSet<>())
                         .add(fromPkg);
             }
         }
     }




    // find circular dependecies
    private void updateCircularDependenciesGroups(){
        allCircularDependencies.clear();
        allCircularDependencies=circularDependency.findCircularDependency(pkgdependencies);

    }






}
