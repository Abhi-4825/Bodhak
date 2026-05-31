package com.example.bodhakfrontend.languages.python.extractor;

import com.example.bodhakfrontend.core.model.entity.*;
import com.example.bodhakfrontend.core.plugin.EntityInfoBuilder;
import com.example.bodhakfrontend.core.plugin.Parser;
import org.treesitter.TSNode;
import org.treesitter.TSTree;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PythonEntityInfoBuilder implements EntityInfoBuilder {

    private final Parser<TSTree> parser;

    public PythonEntityInfoBuilder(Parser<TSTree> parser) {
        this.parser = parser;
    }

    @Override
    public List<EntityInfo> build(Path filePath) {
        List<EntityInfo> result = new ArrayList<>();
        Path normalized = filePath.toAbsolutePath().normalize();
        if (!normalized.toString().endsWith(".py")) return result;

        TSTree tree = parser.parse(normalized);
        if (tree == null) return result;

        try {
            byte[] source = Files.readAllBytes(normalized);
            String modulePrefix = PythonEntityNameExtractor.resolveModulePrefix(normalized);

            // Create the module entity
            EntityContribution moduleContrib = new EntityContribution(new HashSet<>());
            if (isMainScript(tree.getRootNode(), source)) {
                moduleContrib.getTags().add("has_main");
            }
            
            // Collect subclasses/functions
            if (tree.getRootNode() != null && !tree.getRootNode().isNull()) {
                collectEntities(tree.getRootNode(), source, normalized, modulePrefix, modulePrefix, result);
            }
            
            // Create Module Entity if we have a module prefix
            if (!modulePrefix.isEmpty()) {
                TSNode root = tree.getRootNode();
                int loc = (root != null && !root.isNull()) ? (root.getEndPoint().getRow() + 1) : 0;
                EntityInfo moduleEntry = new EntityInfo(
                        modulePrefix,
                        PythonEntityNameExtractor.resolveModulePrefix(normalized.getParent()), // parent namespace
                        normalized.toFile(),
                        moduleContrib.hasTag("has_main") ? EntityKind.SCRIPT : EntityKind.MODULE,
                        "python",
                        new HashSet<>(), // fields
                        new ArrayList<>(), // module level functions are extracted as separate Function entities
                        new HashSet<>(), // decorators
                        new HashSet<>(), new HashSet<>(), new HashSet<>(), // resolving handled by Engine
                        false, false, true,
                        loc, 1, 1,
                        moduleContrib
                );
                result.add(moduleEntry);
            }

        } catch (Exception e) {
            System.err.println("PythonEntityInfoBuilder error at " + filePath + ": " + e.getMessage());
        }
        return result;
    }
    
    private boolean isMainScript(TSNode root, byte[] src) {
        if (root == null || root.isNull()) return false;
        // Look for if __name__ == '__main__'
        for (int i = 0; i < root.getChildCount(); i++) {
            TSNode child = root.getChild(i);
            if ("if_statement".equals(child.getType())) {
                TSNode condition = child.getChildByFieldName("condition");
                if (condition != null && text(src, condition).contains("__name__") && text(src, condition).contains("__main__")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void collectEntities(TSNode node, byte[] src, Path filePath,
                                 String modulePrefix, String parentFqn,
                                 List<EntityInfo> out) {
        if (node == null || node.isNull()) return;
                                 
        String type = node.getType();
        
        if ("class_definition".equals(type) || "decorated_definition".equals(type) && "class_definition".equals(getDefNode(node).getType())) {
            
            TSNode classNode = "decorated_definition".equals(type) ? getDefNode(node) : node;
            TSNode nameNode = classNode.getChildByFieldName("name");
            if (nameNode == null) return;

            String simpleName = text(src, nameNode);
            String fqn = modulePrefix.isEmpty() ? simpleName : modulePrefix + "." + simpleName;

            Set<String> decorators = extractDecorators(type.equals("decorated_definition") ? node : null, src);
            Set<String> fields = extractFields(classNode, src);
            List<MemberInfo> members = extractMethods(classNode, src, fqn, filePath.toFile());

            Set<String> tags = new HashSet<>();
            if (decorators.contains("dataclass")) tags.add("is_dataclass");
            if (decorators.contains("app.route") || decorators.contains("api.get") || decorators.contains("api.post")) {
                tags.add("flask_route");
                tags.add("framework_root");
            }
            if (decorators.contains("django.views")) tags.add("django_view");

            int loc = classNode.getEndPoint().getRow() - classNode.getStartPoint().getRow() + 1;
            int beginLine = classNode.getStartPoint().getRow() + 1;
            int beginColumn = classNode.getStartPoint().getColumn() + 1;

            EntityInfo info = new EntityInfo(
                    fqn,
                    modulePrefix,
                    filePath.toFile(),
                    EntityKind.CLASS,
                    "python",
                    fields,
                    members,
                    decorators,
                    new HashSet<>(), new HashSet<>(), new HashSet<>(),
                    false, false, !simpleName.startsWith("_"),
                    loc, beginLine, beginColumn,
                    new EntityContribution(tags)
            );
            out.add(info);

            TSNode body = classNode.getChildByFieldName("body");
            if (body != null) {
                collectEntities(body, src, filePath, modulePrefix, fqn, out);
            }
            return;
        }
        
        if ("function_definition".equals(type) || "decorated_definition".equals(type) && "function_definition".equals(getDefNode(node).getType())) {
            
            TSNode fnNode = "decorated_definition".equals(type) ? getDefNode(node) : node;
            TSNode nameNode = fnNode.getChildByFieldName("name");
            if (nameNode == null) return;
            
            String simpleName = text(src, nameNode);
            // If it's inside a class, it's extracted as a member of the class, so we skip it here.
            // We only want top-level functions or functions inside functions (rare).
            // Actually, we called this function on the module body, so these are top-level functions.
            // If we are inside a class, `parentFqn` is the class name, we should skip emitting them as top-level EntityInfos.
            if (!parentFqn.equals(modulePrefix)) {
                return;
            }

            String fqn = modulePrefix.isEmpty() ? simpleName : modulePrefix + "." + simpleName;
            
            Set<String> decorators = extractDecorators(type.equals("decorated_definition") ? node : null, src);
            
            Set<String> tags = new HashSet<>();
            if (simpleName.startsWith("test_")) {
                tags.add("pytest_test");
            }
            if (decorators.contains("app.route") || decorators.contains("api.get") || decorators.contains("api.post")) {
                tags.add("flask_route");
                tags.add("framework_root");
            }

            int loc = fnNode.getEndPoint().getRow() - fnNode.getStartPoint().getRow() + 1;
            int beginLine = fnNode.getStartPoint().getRow() + 1;
            int beginColumn = fnNode.getStartPoint().getColumn() + 1;

            // Extract single member to represent the function itself
            MemberInfo fnMember = createMember(fnNode, src, fqn, filePath.toFile(), decorators);

            EntityInfo info = new EntityInfo(
                    fqn,
                    modulePrefix,
                    filePath.toFile(),
                    EntityKind.FUNCTION,
                    "python",
                    new HashSet<>(),
                    List.of(fnMember),
                    decorators,
                    new HashSet<>(), new HashSet<>(), new HashSet<>(),
                    false, false, !simpleName.startsWith("_"),
                    loc, beginLine, beginColumn,
                    new EntityContribution(tags)
            );
            out.add(info);
            return;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            collectEntities(node.getChild(i), src, filePath, modulePrefix, parentFqn, out);
        }
    }

    private TSNode getDefNode(TSNode decoratedNode) {
        return decoratedNode.getChildByFieldName("definition");
    }

    private Set<String> extractDecorators(TSNode decoratedNode, byte[] src) {
        Set<String> decorators = new HashSet<>();
        if (decoratedNode == null) return decorators;
        
        for (int i = 0; i < decoratedNode.getChildCount(); i++) {
            TSNode child = decoratedNode.getChild(i);
            if ("decorator".equals(child.getType())) {
                decorators.add(text(src, child).replace("@", "").trim());
            }
        }
        return decorators;
    }

    private Set<String> extractFields(TSNode classNode, byte[] src) {
        Set<String> fields = new HashSet<>();
        TSNode body = classNode.getChildByFieldName("body");
        if (body == null) return fields;
        for (int i = 0; i < body.getChildCount(); i++) {
            TSNode child = body.getChild(i);
            if ("annotated_assignment".equals(child.getType())) {
                TSNode lhs = child.getChildByFieldName("left");
                if (lhs != null && "identifier".equals(lhs.getType())) {
                    fields.add(text(src, lhs));
                }
            }
            if ("expression_statement".equals(child.getType()) && child.getChildCount() > 0) {
                TSNode expr = child.getChild(0);
                if ("assignment".equals(expr.getType())) {
                    TSNode lhs = expr.getChildByFieldName("left");
                    if (lhs != null && "identifier".equals(lhs.getType())) {
                        fields.add(text(src, lhs));
                    }
                }
            }
        }
        return fields;
    }

    private List<MemberInfo> extractMethods(TSNode classNode, byte[] src, String className, File file) {
        List<MemberInfo> methods = new ArrayList<>();
        TSNode body = classNode.getChildByFieldName("body");
        if (body == null) return methods;
        for (int i = 0; i < body.getChildCount(); i++) {
            TSNode child = body.getChild(i);
            if (!"function_definition".equals(child.getType()) &&
                    !"decorated_definition".equals(child.getType())) continue;

            TSNode fnNode = "decorated_definition".equals(child.getType())
                    ? child.getChildByFieldName("definition")
                    : child;
            if (fnNode == null || !"function_definition".equals(fnNode.getType())) continue;

            Set<String> decorators = extractDecorators("decorated_definition".equals(child.getType()) ? child : null, src);
            methods.add(createMember(fnNode, src, className, file, decorators));
        }
        return methods;
    }
    
    private MemberInfo createMember(TSNode fnNode, byte[] src, String className, File file, Set<String> decorators) {
        TSNode nameNode = fnNode.getChildByFieldName("name");
        String methodName = nameNode != null ? text(src, nameNode) : "unknown";
        TSNode retTypeNode = fnNode.getChildByFieldName("return_type");
        String returnType = retTypeNode != null ? text(src, retTypeNode) : "None";

        List<ParameterInfo> params = new ArrayList<>();
        TSNode paramsNode = fnNode.getChildByFieldName("parameters");
        if (paramsNode != null) {
            for (int j = 0; j < paramsNode.getChildCount(); j++) {
                TSNode param = paramsNode.getChild(j);
                String pType = param.getType();
                if ("identifier".equals(pType)) {
                    params.add(new ParameterInfo(text(src, param), ""));
                } else if ("typed_parameter".equals(pType)) {
                    TSNode pName = param.getChildByFieldName("name") != null
                            ? param.getChildByFieldName("name") : param.getChild(0);
                    TSNode pTypeNode = param.getChildByFieldName("type");
                    params.add(new ParameterInfo(
                            pName != null ? text(src, pName) : "?",
                            pTypeNode != null ? text(src, pTypeNode) : ""
                    ));
                }
            }
        }

        List<ModifierKind> modifiers = new ArrayList<>();
        if (methodName.startsWith("__") && methodName.endsWith("__")) {
            modifiers.add(ModifierKind.PUBLIC);
        } else if (methodName.startsWith("_")) {
            modifiers.add(ModifierKind.PRIVATE);
        } else {
            modifiers.add(ModifierKind.PUBLIC);
        }
        if (decorators.stream().anyMatch(d -> d.contains("staticmethod") || d.contains("classmethod"))) {
            modifiers.add(ModifierKind.STATIC);
        }

        int startLine = fnNode.getStartPoint().getRow() + 1;
        int startColumn = fnNode.getStartPoint().getColumn() + 1;
        int endLine = fnNode.getEndPoint().getRow() + 1;

        TSNode fnBody = fnNode.getChildByFieldName("body");
        int stmtCount = fnBody != null ? fnBody.getChildCount() : 0;
        
        MemberKind kind = methodName.equals("__init__") ? MemberKind.CONSTRUCTOR : MemberKind.FUNCTION;

        return new MemberInfo(
                kind, methodName, returnType, params,
                new ArrayList<>(), // called methods
                modifiers, startLine, endLine, startColumn, stmtCount, file
        );
    }

    private String text(byte[] src, TSNode node) {
        if (node == null || node.isNull()) return "";
        return new String(src, node.getStartByte(), node.getEndByte() - node.getStartByte()).trim();
    }

    @Override
    public void invalidate(Path filePath) {
        parser.invalidate(filePath);
    }
}
