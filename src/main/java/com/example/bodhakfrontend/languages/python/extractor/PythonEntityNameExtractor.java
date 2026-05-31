package com.example.bodhakfrontend.languages.python.extractor;

import com.example.bodhakfrontend.core.plugin.EntityNameExtractor;
import com.example.bodhakfrontend.core.plugin.Parser;
import org.treesitter.TSNode;
import org.treesitter.TSTree;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class PythonEntityNameExtractor implements EntityNameExtractor {

    private final Parser<TSTree> parser;

    public PythonEntityNameExtractor(Parser<TSTree> parser) {
        this.parser = parser;
    }

    @Override
    public Set<String> extractNames(Path filePath) {
        Set<String> names = new HashSet<>();
        Path normalized = filePath.toAbsolutePath().normalize();
        TSTree tree = parser.parse(normalized);
        if (tree == null) return names;

        try {
            byte[] source = Files.readAllBytes(normalized);
            String modulePrefix = resolveModulePrefix(normalized);
            
            // The file itself counts as a module entity.
            if (!modulePrefix.isEmpty()) {
                names.add(modulePrefix);
            }
            
            collectEntityNames(tree.getRootNode(), source, modulePrefix, names);
        } catch (Exception e) {
            System.err.println("PythonEntityNameExtractor error: " + e.getMessage());
        }
        return names;
    }

    private void collectEntityNames(TSNode node, byte[] source, String modulePrefix, Set<String> out) {
        String type = node.getType();
        
        if ("class_definition".equals(type) || "function_definition".equals(type)) {
            TSNode nameNode = node.getChildByFieldName("name");
            if (nameNode != null) {
                String simpleName = new String(source, nameNode.getStartByte(), nameNode.getEndByte() - nameNode.getStartByte());
                out.add(modulePrefix.isEmpty() ? simpleName : modulePrefix + "." + simpleName);
            }
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            collectEntityNames(node.getChild(i), source, modulePrefix, out);
        }
    }

    public static String resolveModulePrefix(Path filePath) {
        Path p = filePath;
        StringBuilder parts = new StringBuilder();
        
        String filename = p.getFileName().toString();
        if (filename.endsWith(".py")) filename = filename.substring(0, filename.length() - 3);
        
        if ("__init__".equals(filename) || "__main__".equals(filename)) {
            p = p.getParent();
            if (p == null) return "";
            filename = p.getFileName().toString();
            p = p.getParent();
        } else {
            parts.insert(0, filename);
            p = p.getParent();
        }
        
        while (p != null && Files.exists(p.resolve("__init__.py"))) {
            String pkg = p.getFileName().toString();
            parts.insert(0, pkg + (parts.length() > 0 ? "." : ""));
            p = p.getParent();
        }
        return parts.toString();
    }
}
