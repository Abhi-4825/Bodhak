package com.example.bodhakfrontend.IncrementalPart.Builder;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;

import com.example.bodhakfrontend.IncrementalPart.model.Package.PackageInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Package.PackageWarning;
import com.example.bodhakfrontend.dependency.CircularDependency;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PackageInfoBuilder {
    private final ClassInfoBuilder classInfoToPathMapBuilder;
    private final CircularDependency circularDependency=new CircularDependency();
    //List<PackageInfo>
    private final Map<String,PackageInfo> packageInfoMap =new ConcurrentHashMap<>();
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
    // Map<Packagename,packageINfo>

    public void buildAll(List<ClassInfo> classInfos){
        if(packageInfoMap !=null){
            packageInfoMap.clear();
        }
         buildFileWise(classInfos);
    }

    private void buildFileWise(
            List<ClassInfo> classInfos
    ) {

        Map<String,Set<ClassInfo>> pkgToClasses=getPackageInfoMap(classInfos);
        classInfoMap=getClassInfoMap(classInfos);
        computePackageDependencies(classInfos);
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



          PackageInfo packageInfo=new PackageInfo(packageName);
            packageInfo.getClasses().addAll(classes);
            packageInfo.getDependsOn().addAll(dependsOn);
            packageInfo.getUsedBy().addAll(usedBy);
            packageInfo.setCircularDependencies(circularGroups);
            Set<PackageWarning> packageWarnings=getWarnings(packageInfo);
            packageInfo.setWarnings(packageWarnings);


            packageInfoMap.put(packageName,packageInfo);



        }
    }


    // warnings
    private Set<PackageWarning> getWarnings(PackageInfo packageInfo){
        Set<PackageWarning> warnings=new HashSet<>();

        boolean isDefaultPkg =
                packageInfo.getPackageName() == null || packageInfo.getPackageName().isBlank();

        if (isDefaultPkg) {
            warnings.add(PackageWarning.DEFAULT_PACKAGE);
        }
        // coupling
        if (!packageInfo.getPackageName().equals("default")) {
            int coupling = packageInfo.getDependsOn().size() + packageInfo.getUsedBy().size();
            if (coupling > 6) {
                warnings.add(PackageWarning.HIGH_COUPLING);
            }
        }

        // god package
        if (packageInfo.getClasses().size() > 20) {
            warnings.add(PackageWarning.GOD_PACKAGE);
        }



        // hub
        if (packageInfo.getUsedBy().size() >= 4
                && packageInfo.getDependsOn().size() <= 1) {
            warnings.add(PackageWarning.HUB_PACKAGE);
        }
        return warnings;

    }




     //depends on
     private void computePackageDependencies(List<ClassInfo> classInfos) {
         pkgdependencies.clear();
         revPkgDeps.clear();
         classInfoMap = getClassInfoMap(classInfos);
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
        for (Set<String> cycle : allCircularDependencies) {
            for (String pkg : cycle) {
                PackageInfo pi = packageInfoMap.get(pkg);
                if (pi != null) {
                    pi.setCircularDependencies(
                            Set.of(cycle)
                    );
                }
            }
        }


    }

    // get PackageMap
    public Map<String,PackageInfo> getPackageInfoMap(){
        return packageInfoMap;
    }






    // update packageInfo
    public void onFileCreate(List<ClassInfo> newClasses) {
        if(newClasses==null || newClasses.isEmpty()) return;
        String packageName=newClasses.get(0).getPackageName();

        PackageInfo packageInfo= packageInfoMap.computeIfAbsent(packageName, k->new PackageInfo(k));
          //add classes
        packageInfo.getClasses().addAll(newClasses);
         //update dependencies
        updatePkgDependenciesForClasses(newClasses);
        Set<String> dependsOn =
                pkgdependencies.getOrDefault(packageName, Set.of());
        packageInfo.getDependsOn().clear();
        packageInfo.getDependsOn().addAll(dependsOn);

        Set<String> usedBy =
                revPkgDeps.getOrDefault(packageName, Set.of());
        packageInfo.getUsedBy().clear();
        packageInfo.getUsedBy().addAll(usedBy);

        // warnig
        packageInfo.setWarnings(getWarnings(packageInfo));
        //circular dependencies
        updateCircularDependenciesGroups();
    }
    private void updatePkgDependenciesForClasses(List<ClassInfo> classes) {
        for (ClassInfo c : classes) {
            String fromPkg = c.getPackageName();

            for (String depClass : c.getDependsOn()) {
                ClassInfo depInfo = classInfoMap.get(depClass);
                if (depInfo == null) continue;

                String toPkg = depInfo.getPackageName();
                if (fromPkg.equals(toPkg)) continue;

                pkgdependencies
                        .computeIfAbsent(fromPkg, k -> new HashSet<>())
                        .add(toPkg);

                revPkgDeps
                        .computeIfAbsent(toPkg, k -> new HashSet<>())
                        .add(fromPkg);
            }
        }
    }



    // on File Delete
    public void onFileDelete(List<ClassInfo> removedClasses) {
        if(removedClasses==null || removedClasses.isEmpty()) return;
        String packageName=removedClasses.get(0).getPackageName();
        PackageInfo packageInfo= packageInfoMap.get(packageName);
        if(packageInfo==null) return;
        //remove class
        packageInfo.getClasses().removeAll(removedClasses);

        // if pkg is empty we remove it
        if (packageInfo.getClasses().isEmpty()) {
            packageInfoMap.remove(packageName);
            pkgdependencies.remove(packageName);
            revPkgDeps.remove(packageName);
        }


        //rebuild pkg dependencies
        computePackageDependencies(getClassInfos());
        Set<String> dependsOn =
                pkgdependencies.getOrDefault(packageName, Set.of());
        packageInfo.getDependsOn().clear();
        packageInfo.getDependsOn().addAll(dependsOn);

        Set<String> usedBy =
                revPkgDeps.getOrDefault(packageName, Set.of());
          packageInfo.getUsedBy().clear();
          packageInfo.getUsedBy().addAll(usedBy);
        // warning
        packageInfo.setWarnings(getWarnings(packageInfo));
        //circular dependencies
        updateCircularDependenciesGroups();

    }



    // on File Modify
          public void onFileUpdate(List<ClassInfo>oldClasses,List<ClassInfo> updatedClasses) {
            if(updatedClasses==null || updatedClasses.isEmpty()) return;
            onFileDelete(oldClasses);
            onFileCreate(updatedClasses);
          }


}
