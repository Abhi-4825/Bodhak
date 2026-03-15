package Analysis.language.python;

import com.example.bodhakfrontend.dependency.CircularDependency;
import org.treesitter.TSNode;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PythonClassDependencyGraphBuilder {

    private final Map<Path, Map<String, Set<String>>> classDependencies = new HashMap<>();
    private final Map<String, Set<String>> reverseClassDependencies = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> classDependenciesGroups = new ConcurrentHashMap<>();

    private final CircularDependency circularDependency = new CircularDependency();

    // ------------------------------------------------
    // BUILD GRAPH
    // ------------------------------------------------

    public Map<Path, Map<String, Set<String>>> buildDependsOnGraph(
            Path filePath,
            TSNode root,
            String source,
            Set<String> sourceClasses
    ) {

        classDependencies.clear();

        buildDependencyGraph(filePath, root, source, sourceClasses);

        getAffectedClassDependencies(classDependencies);

        return classDependencies;
    }

    // ------------------------------------------------
    // BUILD DEPENDENCIES FOR ONE FILE
    // ------------------------------------------------

    public void buildDependencyGraph(
            Path filePath,
            TSNode root,
            String source,
            Set<String> sourceClasses
    ) {

        walk(root, filePath, source, sourceClasses);
    }

    // ------------------------------------------------
    // TREE WALKER
    // ------------------------------------------------

    private void walk(
            TSNode node,
            Path filePath,
            String source,
            Set<String> sourceClasses
    ) {

        if (node.getType().equals("class_definition")) {

            TSNode nameNode = node.getChildByFieldName("name");

            String className = source.substring(
                    nameNode.getStartByte(),
                    nameNode.getEndByte()
            );

            Set<String> dependencies = new HashSet<>();

            extractInheritance(node, source, dependencies, sourceClasses);
            extractImports(node, source, dependencies, sourceClasses);
            extractCalls(node, source, dependencies, sourceClasses);
            extractTypeHints(node, source, dependencies, sourceClasses);

            dependencies.remove(className);

            classDependencies
                    .computeIfAbsent(filePath, k -> new HashMap<>())
                    .put(className, dependencies);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            walk(node.getChild(i), filePath, source, sourceClasses);
        }
    }

    // ------------------------------------------------
    // INHERITANCE
    // class A(B)
    // ------------------------------------------------

    private void extractInheritance(
            TSNode classNode,
            String source,
            Set<String> deps,
            Set<String> sourceClasses
    ) {

        TSNode superNode = classNode.getChildByFieldName("superclasses");

        if (superNode == null) return;

        for (int i = 0; i < superNode.getChildCount(); i++) {

            TSNode child = superNode.getChild(i);

            if (child.getType().equals("identifier")) {

                String parent = source.substring(
                        child.getStartByte(),
                        child.getEndByte()
                );

                if (sourceClasses.contains(parent)) {
                    deps.add(parent);
                }
            }
        }
    }

    // ------------------------------------------------
    // IMPORT DEPENDENCIES
    // from repo import OrderRepo
    // ------------------------------------------------

    private void extractImports(
            TSNode node,
            String source,
            Set<String> deps,
            Set<String> sourceClasses
    ) {

        if (node.getType().equals("import_from_statement")
                || node.getType().equals("import_statement")) {

            for (int i = 0; i < node.getChildCount(); i++) {

                TSNode child = node.getChild(i);

                if (child.getType().equals("identifier")) {

                    String name = source.substring(
                            child.getStartByte(),
                            child.getEndByte()
                    );

                    if (sourceClasses.contains(name)) {
                        deps.add(name);
                    }
                }
            }
        }
    }

    // ------------------------------------------------
    // METHOD CALLS & CONSTRUCTOR CALLS
    // ------------------------------------------------

    private void extractCalls(
            TSNode node,
            String source,
            Set<String> deps,
            Set<String> sourceClasses
    ) {

        if (node.getType().equals("call")) {

            TSNode func = node.getChildByFieldName("function");

            if (func != null) {

                if (func.getType().equals("identifier")) {

                    String name = source.substring(
                            func.getStartByte(),
                            func.getEndByte()
                    );

                    if (sourceClasses.contains(name)) {
                        deps.add(name);
                    }
                }

                else if (func.getType().equals("attribute")) {

                    TSNode objectNode = func.getChild(0);

                    if (objectNode != null) {

                        String objectName = source.substring(
                                objectNode.getStartByte(),
                                objectNode.getEndByte()
                        );

                        if (sourceClasses.contains(objectName)) {
                            deps.add(objectName);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            extractCalls(node.getChild(i), source, deps, sourceClasses);
        }
    }

    // ------------------------------------------------
    // TYPE HINTS
    // repo: Repo
    // ------------------------------------------------

    private void extractTypeHints(
            TSNode node,
            String source,
            Set<String> deps,
            Set<String> sourceClasses
    ) {

        if (node.getType().equals("type")) {

            String typeName = source.substring(
                    node.getStartByte(),
                    node.getEndByte()
            );

            if (sourceClasses.contains(typeName)) {
                deps.add(typeName);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            extractTypeHints(node.getChild(i), source, deps, sourceClasses);
        }
    }

    // ------------------------------------------------
    // REVERSE DEPENDENCIES
    // ------------------------------------------------

    public Map<String, Set<String>> getAffectedClassDependencies(
            Map<Path, Map<String, Set<String>>> classDependenciesToPath
    ) {

        reverseClassDependencies.clear();

        classDependenciesToPath.forEach((path, classMap) -> {

            classMap.forEach((fromClass, dependsOnSet) -> {

                for (String toClass : dependsOnSet) {

                    reverseClassDependencies
                            .computeIfAbsent(toClass, k -> ConcurrentHashMap.newKeySet())
                            .add(fromClass);
                }
            });
        });

        return reverseClassDependencies;
    }

    // ------------------------------------------------
    // INCREMENTAL FILE EVENTS
    // ------------------------------------------------

    public void onFileCreate(Path filePath, TSNode root, String source, Set<String> sourceClasses) {

        buildDependencyGraph(filePath, root, source, sourceClasses);
        getAffectedClassDependencies(classDependencies);
    }

    public void onFileModify(Path filePath, TSNode root, String source, Set<String> sourceClasses) {

        buildDependencyGraph(filePath, root, source, sourceClasses);
        getAffectedClassDependencies(classDependencies);
    }

    public void onFileDelete(Path filePath) {

        classDependencies.remove(filePath);
        getAffectedClassDependencies(classDependencies);
    }

    // ------------------------------------------------
    // GETTERS
    // ------------------------------------------------

    public Map<Path, Map<String, Set<String>>> getClassDependenciesToPath() {
        return classDependencies;
    }

    public Map<String, Set<String>> getReverseClassDependencies() {
        return reverseClassDependencies;
    }

    // ------------------------------------------------
    // DEPENDS ON GRAPH
    // ------------------------------------------------

    public Map<String, Set<String>> getDependsOn() {

        Map<String, Set<String>> dependsOn = new HashMap<>();

        for (Map<String, Set<String>> perFileDeps : classDependencies.values()) {

            for (Map.Entry<String, Set<String>> entry : perFileDeps.entrySet()) {

                dependsOn
                        .computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                        .addAll(entry.getValue());
            }
        }

        return dependsOn;
    }

    // ------------------------------------------------
    // CIRCULAR DEPENDENCIES
    // ------------------------------------------------

    public Set<Set<String>> getClassDependenciesGroups() {

        classDependenciesGroups.clear();

        Map<Path, Map<String, Set<String>>> perFileDeps = getClassDependenciesToPath();

        for (Map.Entry<Path, Map<String, Set<String>>> entry : perFileDeps.entrySet()) {

            for (Map.Entry<String, Set<String>> e : entry.getValue().entrySet()) {

                classDependenciesGroups
                        .computeIfAbsent(e.getKey(), k -> new HashSet<>())
                        .addAll(e.getValue());
            }
        }

        return circularDependency.findCircularDependency(classDependenciesGroups);
    }
}
