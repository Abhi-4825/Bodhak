package com.example.bodhakfrontend.languages.python.extractor;

import com.example.bodhakfrontend.core.plugin.DependencyResolver;
import com.example.bodhakfrontend.core.plugin.Parser;
import org.treesitter.TSNode;
import org.treesitter.TSTree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PythonDependencyResolver implements DependencyResolver {

    private final Parser<TSTree> parser;

    public PythonDependencyResolver(Parser<TSTree> parser) {
        this.parser = parser;
    }

    @Override
    public Map<String, Set<String>> resolve(Path filePath, Set<String> knownNames) {
        Map<String, Set<String>> result = new HashMap<>();
        Path normalized = filePath.toAbsolutePath().normalize();
        TSTree tree = parser.parse(normalized);
        if (tree == null) return result;

        try {
            byte[] source = Files.readAllBytes(normalized);
            String modulePrefix = PythonEntityNameExtractor.resolveModulePrefix(normalized);
            
            // Everything loosely in the module root creates dependencies for the module itself
            Set<String> moduleDeps = new HashSet<>();
            
            if (tree.getRootNode() != null && !tree.getRootNode().isNull()) {
                processNode(tree.getRootNode(), source, modulePrefix, modulePrefix, knownNames, result, moduleDeps);
            }
            
            if (!modulePrefix.isEmpty()) {
                moduleDeps.remove(modulePrefix);
                result.computeIfAbsent(modulePrefix, k -> new HashSet<>()).addAll(moduleDeps);
            }
        } catch (Exception e) {
            System.err.println("PythonDependencyResolver error: " + e.getMessage());
        }
        return result;
    }

    private void processNode(TSNode node, byte[] src, String modulePrefix, String currentScopeFqn,
                             Set<String> knownNames, Map<String, Set<String>> out, Set<String> currentDeps) {
        if (node == null || node.isNull()) return;
                             
        String type = node.getType();

        if ("class_definition".equals(type) || "function_definition".equals(type)) {
            TSNode nameNode = node.getChildByFieldName("name");
            if (nameNode == null) return;
            
            String simpleName = text(src, nameNode);
            String fqn = modulePrefix.isEmpty() ? simpleName : modulePrefix + "." + simpleName;

            Set<String> deps = new HashSet<>();

            if ("class_definition".equals(type)) {
                TSNode args = node.getChildByFieldName("superclasses");
                if (args != null) collectIdentifiers(args, src, deps, knownNames);
            } else {
                TSNode params = node.getChildByFieldName("parameters");
                if (params != null) collectAnnotations(params, src, deps, knownNames);
                TSNode retType = node.getChildByFieldName("return_type");
                if (retType != null) collectIdentifiers(retType, src, deps, knownNames);
            }

            TSNode body = node.getChildByFieldName("body");
            if (body != null) {
                collectBodyDeps(body, src, deps, knownNames);
                
                // recurse for nested definitions
                processNode(body, src, modulePrefix, fqn, knownNames, out, deps);
            }

            deps.remove(fqn);
            out.computeIfAbsent(fqn, k -> new HashSet<>()).addAll(deps);
            return;
        }
        
        // Imports in module scope add to module deps
        if ("import_statement".equals(type) || "import_from_statement".equals(type)) {
             collectIdentifiers(node, src, currentDeps, knownNames);
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            processNode(node.getChild(i), src, modulePrefix, currentScopeFqn, knownNames, out, currentDeps);
        }
    }

    private void collectBodyDeps(TSNode body, byte[] src, Set<String> deps, Set<String> knownNames) {
        for (int i = 0; i < body.getChildCount(); i++) {
            TSNode child = body.getChild(i);
            String type = child.getType();

            if ("annotated_assignment".equals(type)) {
                TSNode annotation = child.getChildByFieldName("type");
                if (annotation != null) collectIdentifiers(annotation, src, deps, knownNames);
            }
            
            collectCalls(child, src, deps, knownNames);
        }
    }

    private void collectAnnotations(TSNode params, byte[] src, Set<String> deps, Set<String> knownNames) {
        for (int i = 0; i < params.getChildCount(); i++) {
            TSNode p = params.getChild(i);
            if ("typed_parameter".equals(p.getType()) || "typed_default_parameter".equals(p.getType())) {
                TSNode typeNode = p.getChildByFieldName("type");
                if (typeNode != null) collectIdentifiers(typeNode, src, deps, knownNames);
            }
        }
    }

    private void collectIdentifiers(TSNode node, byte[] src, Set<String> deps, Set<String> knownNames) {
        if (node == null || node.isNull()) return;
        if ("identifier".equals(node.getType())) {
            String name = text(src, node);
            matchToSource(name, knownNames, deps);
            return;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectIdentifiers(node.getChild(i), src, deps, knownNames);
        }
    }

    private void collectCalls(TSNode node, byte[] src, Set<String> deps, Set<String> knownNames) {
        if (node == null || node.isNull()) return;
        if ("call".equals(node.getType())) {
            TSNode fn = node.getChildByFieldName("function");
            if (fn != null) {
                String name = text(src, fn);
                matchToSource(name, knownNames, deps);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            collectCalls(node.getChild(i), src, deps, knownNames);
        }
    }

    private void matchToSource(String simpleName, Set<String> knownNames, Set<String> deps) {
        if (knownNames.contains(simpleName)) {
            deps.add(simpleName);
            return;
        }
        for (String fqn : knownNames) {
            if (fqn.endsWith("." + simpleName)) {
                deps.add(fqn);
                break;
            }
        }
    }

    private String text(byte[] src, TSNode node) {
        if (node == null || node.isNull()) return "";
        return new String(src, node.getStartByte(), node.getEndByte() - node.getStartByte()).trim();
    }
}
