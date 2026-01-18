package com.example.bodhakfrontend.IncrementalPart.Analyzer;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassContribution;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.EntryPointInfo;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.nio.file.Path;
import java.util.*;

public class EntryClassAnalyzer {

    public EntryPointInfo build(List<ClassInfo> classes) {

        Set<EntryPointInfo.ProjectFlavor> flavors = EnumSet.noneOf(EntryPointInfo.ProjectFlavor.class);
        List<EntryPointInfo.Entry> mainEntries = new ArrayList<>();
        List<EntryPointInfo.Entry> testEntries = new ArrayList<>();
        Set<EntryPointInfo.Entry> frameworkRoots = new HashSet<>();

        for (ClassInfo c : classes) {

            var contrib = c.getClassContribution();
            String className = c.getClassName();

            // MAIN
            if (contrib.hasMain()) {
                mainEntries.add(new EntryPointInfo.Entry(className, EntryPointInfo.EntryKind.MAIN));
                flavors.add(EntryPointInfo.ProjectFlavor.CLI);
            }

            // SPRING
            if (contrib.hasSpring()) {
                flavors.add(EntryPointInfo.ProjectFlavor.SPRING_BOOT);

                if (contrib.hasMain()) {
                    mainEntries.add(new EntryPointInfo.Entry(className, EntryPointInfo.EntryKind.SPRING_BOOT));
                } else {
                    frameworkRoots.add(new EntryPointInfo.Entry(className, EntryPointInfo.EntryKind.SPRING_BOOT));
                }
            }

            if (contrib.hasJavaFx()) {
                flavors.add(EntryPointInfo.ProjectFlavor.JAVAFX);

                if (contrib.hasMain()) {
                    mainEntries.add(new EntryPointInfo.Entry(className, EntryPointInfo.EntryKind.JAVAFX));
                } else {
                    frameworkRoots.add(new EntryPointInfo.Entry(className, EntryPointInfo.EntryKind.JAVAFX));
                }
            }

            // TEST
            if (contrib.isTestClass()) {
                flavors.add(EntryPointInfo.ProjectFlavor.TEST);
                testEntries.add(new EntryPointInfo.Entry(className, EntryPointInfo.EntryKind.TEST));
            }
        }

        // choose PRIMARY
        EntryPointInfo.Entry primary = choosePrimary(mainEntries);

        //  SECONDARY
        Set<EntryPointInfo.Entry> secondary = new HashSet<>();
        for (EntryPointInfo.Entry e : mainEntries) {
            if (!e.className().equals(primary.className())) {
                secondary.add(e);
            }
        }
        secondary.addAll(testEntries);

        EntryPointInfo epi =
                new EntryPointInfo(flavors, primary, secondary);

        frameworkRoots.forEach(fr -> epi.getAllRoots().add(fr));

        return epi;
    }

    private EntryPointInfo.Entry choosePrimary(List<EntryPointInfo.Entry> mains) {
        return mains.stream()
                .filter(e -> e.kind() == EntryPointInfo.EntryKind.SPRING_BOOT)
                .findFirst()
                .or(() -> mains.stream()
                        .filter(e -> e.kind() == EntryPointInfo.EntryKind.JAVAFX)
                        .findFirst())
                .orElse(mains.isEmpty() ? null : mains.get(0));
    }

}
