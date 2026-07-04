package com.example.bodhak.frontend.python;

import com.example.bodhak.ir.*;
import com.example.bodhak.ir.declaration.*;
import com.example.bodhak.ir.statement.*;
import com.example.bodhak.ir.expression.*;
import com.example.bodhak.compiler.symbol.QualifiedName;
import com.example.bodhak.frontend.IRBuilder;
import com.example.bodhak.model.entity.ModifierKind;
import org.treesitter.TSNode;
import org.treesitter.TSTree;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Parses Tree-sitter TSTree into the normalized, language-independent IRNode structure for Python.
 */
public class PythonIRBuilder implements IRBuilder<TSTree> {

    @Override
    public IRNode build(Path filePath, TSTree tree) {
        if (filePath == null) {
            filePath = Path.of("Unknown.py");
        }
        NodeIdGenerator idGen = new NodeIdGenerator(filePath.toString());

        if (tree == null || tree.getRootNode() == null || tree.getRootNode().isNull()) {
            return new ModuleDeclaration(idGen.nextId(), SourceRange.UNKNOWN, "", "python", filePath.toString(), Collections.emptyList(), Collections.emptyList(), "");
        }
        
        byte[] src;
        try {
            src = Files.readAllBytes(filePath);
        } catch (IOException e) {
            src = new byte[0];
        }

        TSNode root = tree.getRootNode();
        String moduleName = filePath.getFileName().toString().replace(".py", "");
        List<ImportDeclaration> imports = new ArrayList<>();
        List<DeclarationNode> declarations = new ArrayList<>();
        collectDeclarations(root, src, declarations, imports, idGen);

        return new ModuleDeclaration(idGen.nextId(), range(root), moduleName, "python", filePath.toString(), imports, declarations, "");
    }

    private void collectDeclarations(TSNode node, byte[] src, List<DeclarationNode> out, List<ImportDeclaration> imports, NodeIdGenerator idGen) {
        if (node == null || node.isNull()) return;

        String type = node.getType();

        if ("class_definition".equals(type) || ("decorated_definition".equals(type) && isClassDef(node))) {
            out.add(buildTypeDeclaration(node, src, idGen));
            return;
        }

        if ("function_definition".equals(type) || ("decorated_definition".equals(type) && isFuncDef(node))) {
            out.add(buildCallableDeclaration(node, src, idGen));
            return;
        }

        if ("import_statement".equals(type) || "import_from_statement".equals(type)) {
            imports.addAll(buildImportDeclarations(node, src, idGen));
            return;
        }

        // Recurse module-level children
        for (int i = 0; i < node.getChildCount(); i++) {
            collectDeclarations(node.getChild(i), src, out, imports, idGen);
        }
    }

    private boolean isClassDef(TSNode node) {
        TSNode def = node.getChildByFieldName("definition");
        return def != null && !def.isNull() && "class_definition".equals(def.getType());
    }

    private boolean isFuncDef(TSNode node) {
        TSNode def = node.getChildByFieldName("definition");
        return def != null &&  !def.isNull() && "function_definition".equals(def.getType());
    }

    private TypeDeclaration buildTypeDeclaration(TSNode node, byte[] src, NodeIdGenerator idGen) {
        TSNode classNode = "decorated_definition".equals(node.getType())
                ? node.getChildByFieldName("definition")
                : node;

        TSNode nameNode = classNode.getChildByFieldName("name");
        String name = nameNode != null ? text(src, nameNode) : "unknown";

        List<DecoratorNode> decorators = extractDecorators(node, src, idGen);
        Set<ModifierKind> modifiers = new HashSet<>();
        if (name.startsWith("__")) {
            modifiers.add(ModifierKind.PRIVATE);
        } else if (name.startsWith("_")) {
            modifiers.add(ModifierKind.PRIVATE);
        } else {
            modifiers.add(ModifierKind.PUBLIC);
        }

        List<TypeReferenceExpression> extendsTypes = new ArrayList<>();
        TSNode superclasses = classNode.getChildByFieldName("superclasses");
        if (superclasses != null && !superclasses.isNull()) {
            for (int i = 0; i < superclasses.getChildCount(); i++) {
                TSNode child = superclasses.getChild(i);
                if ("identifier".equals(child.getType()) || "attribute".equals(child.getType())) {
                    extendsTypes.add(buildTypeReference(text(src, child), idGen, range(child)));
                }
            }
        }

        List<DeclarationNode> members = new ArrayList<>();
        TSNode body = classNode.getChildByFieldName("body");
        if (body != null && !body.isNull()) {
            collectMembers(body, src, members, idGen);
        }

        return new TypeDeclaration(
            idGen.nextId(), range(node), name, TypeKind.CLASS, modifiers, decorators, extendsTypes, 
            Collections.emptyList(), Collections.emptyList(), members
        );
    }

    private void collectMembers(TSNode body, byte[] src, List<DeclarationNode> out, NodeIdGenerator idGen) {
        for (int i = 0; i < body.getChildCount(); i++) {
            TSNode child = body.getChild(i);
            String type = child.getType();

            if ("function_definition".equals(type) || ("decorated_definition".equals(type) && isFuncDef(child))) {
                out.add(buildCallableDeclaration(child, src, idGen));
            } else if ("annotated_assignment".equals(type)) {
                TSNode lhs = child.getChildByFieldName("left");
                TSNode val = child.getChildByFieldName("value");
                if (lhs != null && !lhs.isNull() && "identifier".equals(lhs.getType())) {
                    TSNode typeNode = child.getChildByFieldName("type");
                    out.add(new VariableDeclaration(
                        idGen.nextId(),
                        range(child),
                        text(src, lhs),
                        Set.of(ModifierKind.PUBLIC),
                        Collections.emptyList(),
                        buildTypeReference(typeNode != null && !typeNode.isNull() ? text(src, typeNode) : "", idGen, range(child)),
                        val != null && !val.isNull() ? buildExpression(val, src, idGen) : null,
                        VariableKind.FIELD
                    ));
                }
            } else if ("expression_statement".equals(type) && child.getChildCount() > 0) {
                TSNode expr = child.getChild(0);
                if (expr != null && !expr.isNull() && "assignment".equals(expr.getType())) {
                    TSNode lhs = expr.getChildByFieldName("left");
                    TSNode val = expr.getChildByFieldName("value");
                    if (lhs != null && !lhs.isNull() && "identifier".equals(lhs.getType())) {
                        out.add(new VariableDeclaration(
                            idGen.nextId(),
                            range(child),
                            text(src, lhs),
                            Set.of(ModifierKind.PUBLIC),
                            Collections.emptyList(),
                            buildTypeReference("", idGen, range(child)),
                            val != null && !val.isNull() ? buildExpression(val, src, idGen) : null,
                            VariableKind.FIELD
                        ));
                    }
                }
            }
        }
    }

    private CallableDeclaration buildCallableDeclaration(TSNode node, byte[] src, NodeIdGenerator idGen) {
        TSNode fnNode = "decorated_definition".equals(node.getType())
                ? node.getChildByFieldName("definition")
                : node;

        if (fnNode == null || fnNode.isNull()) {
            return new CallableDeclaration(
                idGen.nextId(), range(node), "unknown", Collections.emptySet(), Collections.emptyList(), 
                Collections.emptyList(), buildTypeReference("None", idGen, SourceRange.UNKNOWN), 
                Collections.emptyList(), Collections.emptyList(), null, false, false, false
            );
        }

        TSNode nameNode = fnNode.getChildByFieldName("name");
        String name = nameNode != null && !nameNode.isNull() ? text(src, nameNode) : "unknown";

        List<DecoratorNode> decorators = extractDecorators(node, src, idGen);
        Set<ModifierKind> modifiers = new HashSet<>();
        if (name.startsWith("__") && name.endsWith("__")) {
            modifiers.add(ModifierKind.PUBLIC);
        } else if (name.startsWith("_")) {
            modifiers.add(ModifierKind.PRIVATE);
        } else {
            modifiers.add(ModifierKind.PUBLIC);
        }

        for (DecoratorNode dec : decorators) {
            if (dec.name().contains("staticmethod") || dec.name().contains("classmethod")) {
                modifiers.add(ModifierKind.STATIC);
            }
        }

        List<VariableDeclaration> parameters = new ArrayList<>();
        TSNode paramsNode = fnNode.getChildByFieldName("parameters");
        if (paramsNode != null && !paramsNode.isNull()) {
            for (int i = 0; i < paramsNode.getChildCount(); i++) {
                TSNode param = paramsNode.getChild(i);
                if (param == null || param.isNull()) continue;
                String pType = param.getType();
                if ("identifier".equals(pType)) {
                    parameters.add(new VariableDeclaration(
                        idGen.nextId(),
                        range(param),
                        text(src, param),
                        Collections.emptySet(),
                        Collections.emptyList(),
                        buildTypeReference("", idGen, range(param)),
                        null,
                        VariableKind.PARAMETER
                    ));
                } else if ("typed_parameter".equals(pType)) {
                    TSNode pName = param.getChildByFieldName("name") != null && !param.getChildByFieldName("name").isNull()
                            ? param.getChildByFieldName("name") : param.getChild(0);
                    TSNode pTypeNode = param.getChildByFieldName("type");
                    parameters.add(new VariableDeclaration(
                        idGen.nextId(),
                        range(param),
                        pName != null && !pName.isNull() ? text(src, pName) : "?",
                        Collections.emptySet(),
                        Collections.emptyList(),
                        buildTypeReference(pTypeNode != null && !pTypeNode.isNull() ? text(src, pTypeNode) : "", idGen, range(param)),
                        null,
                        VariableKind.PARAMETER
                    ));
                }
            }
        }

        TSNode retTypeNode = fnNode.getChildByFieldName("return_type");
        String returnType = retTypeNode != null && !retTypeNode.isNull() ? text(src, retTypeNode) : "None";
        TypeReferenceExpression returnTypeRef = buildTypeReference(returnType, idGen, retTypeNode != null ? range(retTypeNode) : SourceRange.UNKNOWN);

        TSNode bodyNode = fnNode.getChildByFieldName("body");
        BlockStatement body = null;
        if (bodyNode != null && !bodyNode.isNull()) {
            List<StatementNode> statements = new ArrayList<>();
            for (int i = 0; i < bodyNode.getChildCount(); i++) {
                TSNode child = bodyNode.getChild(i);
                if (child != null && !child.isNull()) {
                    statements.add(buildStatement(child, src, idGen));
                }
            }
            body = new BlockStatement(idGen.nextId(), range(bodyNode), statements);
        }

        return new CallableDeclaration(
            idGen.nextId(), range(node), name, modifiers, decorators, parameters, returnTypeRef, 
            Collections.emptyList(), Collections.emptyList(), body, false, false, false
        );
    }

    private List<ImportDeclaration> buildImportDeclarations(TSNode node, byte[] src, NodeIdGenerator idGen) {
        List<ImportDeclaration> imports = new ArrayList<>();
        String type = node.getType();
        if ("import_statement".equals(type)) {
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if ("dotted_name".equals(child.getType()) || "aliased_import".equals(child.getType())) {
                    imports.add(new ImportDeclaration(idGen.nextId(), range(child), text(src, child), "", false));
                }
            }
        } else if ("import_from_statement".equals(type)) {
            TSNode moduleNode = node.getChildByFieldName("module_name");
            String modulePath = moduleNode != null && !moduleNode.isNull() ? text(src, moduleNode) : "";
            boolean isWildcard = text(src, node).contains("*");
            imports.add(new ImportDeclaration(idGen.nextId(), range(node), modulePath, "", false, isWildcard));
        }
        return imports;
    }

    private List<DecoratorNode> extractDecorators(TSNode decoratedNode, byte[] src, NodeIdGenerator idGen) {
        List<DecoratorNode> decorators = new ArrayList<>();
        if (decoratedNode == null || !"decorated_definition".equals(decoratedNode.getType())) return decorators;

        for (int i = 0; i < decoratedNode.getChildCount(); i++) {
            TSNode child = decoratedNode.getChild(i);
            if ("decorator".equals(child.getType())) {
                String name = text(src, child).replace("@", "").trim();
                decorators.add(new DecoratorNode(idGen.nextId(), range(child), name, Collections.emptyList()));
            }
        }
        return decorators;
    }

    private StatementNode buildStatement(TSNode node, byte[] src, NodeIdGenerator idGen) {
        String type = node.getType();
        if ("if_statement".equals(type)) {
            TSNode cond = node.getChildByFieldName("condition");
            TSNode consequence = node.getChildByFieldName("consequence");
            TSNode alternative = node.getChildByFieldName("alternative");
            return new IfStatement(
                idGen.nextId(),
                range(node),
                cond != null && !cond.isNull() ? buildExpression(cond, src, idGen) : null,
                consequence != null && !consequence.isNull() ? buildStatement(consequence, src, idGen) : null,
                alternative != null && !alternative.isNull() ? buildStatement(alternative, src, idGen) : null
            );
        } else if ("for_statement".equals(type) || "while_statement".equals(type)) {
            TSNode cond = node.getChildByFieldName("condition");
            TSNode body = node.getChildByFieldName("body");
            return new LoopStatement(
                idGen.nextId(),
                range(node),
                cond != null && !cond.isNull() ? buildExpression(cond, src, idGen) : null,
                body != null && !body.isNull() ? buildStatement(body, src, idGen) : null
            );
        } else if ("try_statement".equals(type)) {
            TSNode body = node.getChildByFieldName("body");
            TSNode handler = node.getChildByFieldName("handler");
            List<StatementNode> catchBlocks = new ArrayList<>();
            if (handler != null && !handler.isNull()) {
                catchBlocks.add(buildStatement(handler, src, idGen));
            }
            return new TryStatement(
                idGen.nextId(),
                range(node),
                body != null && !body.isNull() ? buildStatement(body, src, idGen) : null,
                catchBlocks,
                null
            );
        } else if ("return_statement".equals(type)) {
            TSNode expr = node.getChildCount() > 0 ? node.getChild(0) : null;
            return new ReturnStatement(
                idGen.nextId(),
                range(node),
                expr != null ? buildExpression(expr, src, idGen) : null
            );
        } else {
            // Default statement
            List<StatementNode> subStmts = new ArrayList<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child.getType().endsWith("statement")) {
                    subStmts.add(buildStatement(child, src, idGen));
                }
            }
            return new BlockStatement(idGen.nextId(), range(node), subStmts);
        }
    }

    private ExpressionNode buildExpression(TSNode node, byte[] src, NodeIdGenerator idGen) {
        String type = node.getType();
        if ("call".equals(type)) {
            TSNode function = node.getChildByFieldName("function");
            TSNode arguments = node.getChildByFieldName("arguments");
            List<ExpressionNode> args = new ArrayList<>();
            if (arguments != null && !arguments.isNull()) {
                for (int i = 0; i < arguments.getChildCount(); i++) {
                    TSNode arg = arguments.getChild(i);
                    if (arg != null && !arg.isNull() && !",".equals(arg.getType()) && !"(".equals(arg.getType()) && !")".equals(arg.getType())) {
                        args.add(buildExpression(arg, src, idGen));
                    }
                }
            }
            ExpressionNode receiver = function != null && !function.isNull() ? buildExpression(function, src, idGen) : null;
            String callableName = receiver != null ? receiver.toString() : "unknown";
            return new CallExpression(
                idGen.nextId(),
                range(node),
                receiver,
                callableName,
                args,
                CallKind.UNKNOWN
            );
        } else if ("identifier".equals(type) || "attribute".equals(type)) {
            return new VariableReferenceExpression(idGen.nextId(), range(node), text(src, node));
        } else {
            return buildTypeReference(text(src, node), idGen, range(node));
        }
    }

    private TypeReferenceExpression buildTypeReference(String typeName, NodeIdGenerator idGen, SourceRange sr) {
        QualifiedName qn = QualifiedName.parse(typeName);
        return new TypeReferenceExpression(idGen.nextId(), sr, qn, Collections.emptyList(), 0, Variance.INVARIANT, Collections.emptyList());
    }

    private SourceRange range(TSNode node) {
        if (node == null || node.isNull()) return SourceRange.UNKNOWN;
        return new SourceRange(
            node.getStartPoint().getRow() + 1,
            node.getStartPoint().getColumn() + 1,
            node.getEndPoint().getRow() + 1,
            node.getEndPoint().getColumn() + 1
        );
    }

    private String text(byte[] src, TSNode node) {
        if (node == null || node.isNull()) return "";
        int start = node.getStartByte();
        int end = node.getEndByte();
        if (start >= 0 && end <= src.length && start <= end) {
            return new String(src, start, end - start, java.nio.charset.StandardCharsets.UTF_8).trim();
        }
        return "";
    }
}
