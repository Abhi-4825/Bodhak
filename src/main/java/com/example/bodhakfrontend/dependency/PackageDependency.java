package com.example.bodhakfrontend.dependency;

import com.example.bodhakfrontend.Models.PackageInfo;
import com.example.bodhakfrontend.Models.ClassDependencyInfo;
import com.example.bodhakfrontend.Models.PackageWarning;
import com.example.bodhakfrontend.Models.DependencyNode;

import java.util.*;
public class PackageDependency {
    CircularDependency  circularDependency=new CircularDependency();

    public Map<String, PackageInfo> buildPackageInfo(
            ClassDependencyInfo classDependencyInfo
    ) {

        Map<String, PackageInfo> result = new HashMap<>();

        // 1️⃣ Package → Classes
        Map<String, Set<String>> packageClasses = new HashMap<>();

        for (DependencyNode node : classDependencyInfo.getClassInfo().values()) {
            packageClasses
                    .computeIfAbsent(node.getPackageName(), k -> new HashSet<>())
                    .add(node.getClassName());
        }

        // 2️⃣ Package dependencies (forward + reverse)
        Map<String, Set<String>> pkgDependsOn = new HashMap<>();
        Map<String, Set<String>> pkgUsedBy = new HashMap<>();

        classDependencyInfo.getClassDependencies().forEach((fromClass, deps) -> {
            String fromPkg = classDependencyInfo.getClassInfo()
                    .get(fromClass).getPackageName();

            for (String toClass : deps) {
                DependencyNode toNode = classDependencyInfo.getClassInfo().get(toClass);
                if (toNode == null) continue;

                String toPkg = toNode.getPackageName();

                if (!fromPkg.equals(toPkg)) {
                    pkgDependsOn
                            .computeIfAbsent(fromPkg, k -> new HashSet<>())
                            .add(toPkg);

                    pkgUsedBy
                            .computeIfAbsent(toPkg, k -> new HashSet<>())
                            .add(fromPkg);
                }
            }



        } );


        // 3️⃣ Build PackageInfo objects
        for (String pkg : packageClasses.keySet()) {

            result.put(
                    pkg,
                    new PackageInfo(
                            pkg,
                            packageClasses.get(pkg),
                            pkgDependsOn.getOrDefault(pkg, Set.of()),
                            pkgUsedBy.getOrDefault(pkg, Set.of())
                    )
            );
        }
        Set<Set<String>> cycles = circularDependency.findCircularDependency(pkgDependsOn);
        for(Set<String> cycle : cycles){
            for(String fromPkg : cycle){
                PackageInfo fromPkgInfo = result.get(fromPkg);
                if(fromPkgInfo!=null){
                    fromPkgInfo.setPartOfCycle(true);
                    fromPkgInfo.getCircularDependencies().add(cycle);
                }
            }
        }

        detectPackageWarnings(result);

        return result;
    }
    private void detectPackageWarnings(
            Map<String, PackageInfo> packages
    ) {
        for (PackageInfo pkg : packages.values()) {

            // default
            if (pkg.getPackageName().equals("default")) {
                pkg.getWarnings().add(PackageWarning.DEFAULT_PACKAGE);
            }
            // coupling
            if (!pkg.getPackageName().equals("default")) {
                int coupling = pkg.getDependsOn().size() + pkg.getUsedBy().size();
                if (coupling > 6) {
                    pkg.getWarnings().add(PackageWarning.HIGH_COUPLING);
                }
            }

            // god package
            if (pkg.getClasses().size() > 20) {
                pkg.getWarnings().add(PackageWarning.GOD_PACKAGE);
            }



            // hub
            if (pkg.getUsedBy().size() >= 4
                    && pkg.getDependsOn().size() <= 1) {
                pkg.getWarnings().add(PackageWarning.HUB_PACKAGE);
            }
        }
    }


}

