package com.example.anuviya.frontend.python;

import com.example.anuviya.ir.*;
import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.ir.statement.*;
import com.example.anuviya.ir.expression.*;
import com.example.anuviya.compiler.symbol.QualifiedName;
import com.example.anuviya.frontend.IRBuilder;
import com.example.anuviya.model.entity.ModifierKind;
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

    private final List<Path> sourceRoots;

    public PythonIRBuilder() {
        this(Collections.emptyList());
    }

    public PythonIRBuilder(List<Path> sourceRoots) {
        this.sourceRoots = sourceRoots != null ? sourceRoots : Collections.emptyList();
    }

    @Override
    public IRNode build(Path filePath, TSTree tree) {
        if (filePath == null) {
            filePath = Path.of("Unknown.py");
        }
        NodeIdGenerator idGen = new NodeIdGenerator(filePath.toString());

        if (tree == null || tree.getRootNode() == null || tree.getRootNode().isNull()) {
            return new ModuleDeclaration(idGen.nextId(), SourceRange.UNKNOWN, "", "python", filePath.toString(), Collections.emptyList(), Collections.emptyList(), "", Collections.emptyList());
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
        List<StatementNode> moduleStatements = new ArrayList<>();
        collectModuleElements(root, src, declarations, imports, moduleStatements, idGen);

        String namespace = computeNamespace(filePath, sourceRoots);
        if (!namespace.isEmpty()) {
            declarations.add(0, new NamespaceDeclaration(idGen.nextId(), SourceRange.UNKNOWN, namespace));
        }

        return new ModuleDeclaration(idGen.nextId(), range(root), moduleName, "python", filePath.toString(), imports, declarations, "", moduleStatements);
    }

    private void collectModuleElements(TSNode node, byte[] src, List<DeclarationNode> declarations, List<ImportDeclaration> imports, List<StatementNode> moduleStatements, NodeIdGenerator idGen) {
        if (node == null || node.isNull()) return;

        String type = node.getType();

        if ("class_definition".equals(type) || ("decorated_definition".equals(type) && isClassDef(node))) {
            declarations.add(buildTypeDeclaration(node, src, idGen));
            return;
        }

        if ("function_definition".equals(type) || ("decorated_definition".equals(type) && isFuncDef(node))) {
            declarations.add(buildCallableDeclaration(node, src, idGen, CallableScope.MODULE));
            return;
        }

        if ("import_statement".equals(type) || "import_from_statement".equals(type)) {
            imports.addAll(buildImportDeclarations(node, src, idGen));
            return;
        }

        if ("module".equals(type)) {
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child == null || child.isNull()) continue;
                String childType = child.getType();

                if ("class_definition".equals(childType) || ("decorated_definition".equals(childType) && isClassDef(child))) {
                    declarations.add(buildTypeDeclaration(child, src, idGen));
                } else if ("function_definition".equals(childType) || ("decorated_definition".equals(childType) && isFuncDef(child))) {
                    declarations.add(buildCallableDeclaration(child, src, idGen, CallableScope.MODULE));
                } else if ("import_statement".equals(childType) || "import_from_statement".equals(childType)) {
                    imports.addAll(buildImportDeclarations(child, src, idGen));
                } else if ("expression_statement".equals(childType)) {
                    TSNode expr = child.getChildCount() > 0 ? child.getChild(0) : null;
                    if (expr != null && !expr.isNull() && ("assignment".equals(expr.getType()) || "augmented_assignment".equals(expr.getType()))) {
                        TSNode lhs = expr.getChildByFieldName("left");
                        TSNode val = expr.getChildByFieldName("value");
                        if (lhs != null && !lhs.isNull() && "identifier".equals(lhs.getType())) {
                            declarations.add(new VariableDeclaration(
                                idGen.nextId(),
                                range(child),
                                text(src, lhs),
                                Set.of(ModifierKind.PUBLIC),
                                Collections.emptyList(),
                                buildTypeReference("", idGen, range(child)),
                                val != null && !val.isNull() ? buildExpression(val, src, idGen) : null,
                                VariableKind.FIELD
                            ));
                        } else {
                            moduleStatements.add(buildStatement(child, src, idGen, false));
                        }
                    } else {
                        moduleStatements.add(buildStatement(child, src, idGen, false));
                    }
                } else if ("annotated_assignment".equals(childType)) {
                    TSNode lhs = child.getChildByFieldName("left");
                    TSNode val = child.getChildByFieldName("value");
                    if (lhs != null && !lhs.isNull() && "identifier".equals(lhs.getType())) {
                        TSNode typeNode = child.getChildByFieldName("type");
                        declarations.add(new VariableDeclaration(
                            idGen.nextId(),
                            range(child),
                            text(src, lhs),
                            Set.of(ModifierKind.PUBLIC),
                            Collections.emptyList(),
                            buildTypeReference(typeNode != null && !typeNode.isNull() ? text(src, typeNode) : "", idGen, range(child)),
                            val != null && !val.isNull() ? buildExpression(val, src, idGen) : null,
                            VariableKind.FIELD
                        ));
                    } else {
                        moduleStatements.add(buildStatement(child, src, idGen, false));
                    }
                } else if (childType.endsWith("statement")) {
                    moduleStatements.add(buildStatement(child, src, idGen, false));
                }
            }
        } else {
            for (int i = 0; i < node.getChildCount(); i++) {
                collectModuleElements(node.getChild(i), src, declarations, imports, moduleStatements, idGen);
            }
        }
    }

    private boolean isClassDef(TSNode node) {
        TSNode def = node.getChildByFieldName("definition");
        return def != null && !def.isNull() && "class_definition".equals(def.getType());
    }

    private boolean isFuncDef(TSNode node) {
        TSNode def = node.getChildByFieldName("definition");
        return def != null && !def.isNull() && ("function_definition".equals(def.getType()) || "async_function_definition".equals(def.getType()));
    }

    private boolean isAsyncFunc(TSNode node) {
        if ("async_function_definition".equals(node.getType())) return true;
        if ("decorated_definition".equals(node.getType())) {
            TSNode def = node.getChildByFieldName("definition");
            return def != null && !def.isNull() && "async_function_definition".equals(def.getType());
        }
        return false;
    }

    private TypeDeclaration buildTypeDeclaration(TSNode node, byte[] src, NodeIdGenerator idGen) {
        TSNode classNode = "decorated_definition".equals(node.getType())
                ? node.getChildByFieldName("definition")
                : node;

        TSNode nameNode = classNode.getChildByFieldName("name");
        String name = nameNode != null ? text(src, nameNode) : "unknown";

        List<DecoratorNode> decorators = extractDecorators(node, src, idGen);
        Set<ModifierKind> modifiers = new HashSet<>();
        if (name.startsWith("__") && name.endsWith("__")) {
            modifiers.add(ModifierKind.PUBLIC);
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

        String docstring = extractPythonDocstring(body, src);

        return new TypeDeclaration(
            idGen.nextId(), range(node), name, TypeKind.CLASS, modifiers, decorators, extendsTypes, 
            Collections.emptyList(), Collections.emptyList(), members, docstring
        );
    }

    private void collectMembers(TSNode body, byte[] src, List<DeclarationNode> out, NodeIdGenerator idGen) {
        for (int i = 0; i < body.getChildCount(); i++) {
            TSNode child = body.getChild(i);
            if (child == null || child.isNull()) continue;
            String type = child.getType();

            if ("function_definition".equals(type) || ("decorated_definition".equals(type) && isFuncDef(child))) {
                out.add(buildCallableDeclaration(child, src, idGen, CallableScope.TYPE));
            } else if ("class_definition".equals(type) || ("decorated_definition".equals(type) && isClassDef(child))) {
                out.add(buildTypeDeclaration(child, src, idGen));
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
                if (expr != null && !expr.isNull() && ("assignment".equals(expr.getType()) || "augmented_assignment".equals(expr.getType()))) {
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

    private CallableDeclaration buildCallableDeclaration(TSNode node, byte[] src, NodeIdGenerator idGen, CallableScope scope) {
        TSNode fnNode = "decorated_definition".equals(node.getType())
                ? node.getChildByFieldName("definition")
                : node;

        if (fnNode == null || fnNode.isNull()) {
            return new CallableDeclaration(
                idGen.nextId(), range(node), "unknown", Collections.emptySet(), Collections.emptyList(), 
                Collections.emptyList(), buildTypeReference("None", idGen, SourceRange.UNKNOWN), 
                Collections.emptyList(), Collections.emptyList(), null, false, false, false, false, scope, ""
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
            boolean isFirst = true;
            for (int i = 0; i < paramsNode.getChildCount(); i++) {
                TSNode param = paramsNode.getChild(i);
                if (param == null || param.isNull()) continue;
                String pType = param.getType();
                if (",".equals(pType) || "(".equals(pType) || ")".equals(pType)) continue;

                VariableDeclaration varDecl = buildParameter(param, src, idGen);
                if (varDecl != null) {
                    if (isFirst && scope == CallableScope.TYPE && ("self".equals(varDecl.name()) || "cls".equals(varDecl.name()))) {
                        // Skip implicit receiver parameter
                    } else {
                        parameters.add(varDecl);
                    }
                    isFirst = false;
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
                    statements.add(buildStatement(child, src, idGen, true));
                }
            }
            body = new BlockStatement(idGen.nextId(), range(bodyNode), statements);
        }

        boolean isConstructor = name.equals("__init__");
        boolean isAsync = isAsyncFunc(node);

        String docstring = extractPythonDocstring(bodyNode, src);

        return new CallableDeclaration(
            idGen.nextId(), range(node), name, modifiers, decorators, parameters, returnTypeRef, 
            Collections.emptyList(), Collections.emptyList(), body, isConstructor, false, false, isAsync, scope, docstring
        );
    }

    private VariableDeclaration buildParameter(TSNode param, byte[] src, NodeIdGenerator idGen) {
        String pType = param.getType();
        if ("identifier".equals(pType)) {
            return new VariableDeclaration(
                idGen.nextId(),
                range(param),
                text(src, param),
                Collections.emptySet(),
                Collections.emptyList(),
                buildTypeReference("", idGen, range(param)),
                null,
                VariableKind.PARAMETER
            );
        } else if ("typed_parameter".equals(pType)) {
            TSNode pName = param.getChildByFieldName("name");
            TSNode pTypeNode = param.getChildByFieldName("type");
            return new VariableDeclaration(
                idGen.nextId(),
                range(param),
                pName != null && !pName.isNull() ? text(src, pName) : "?",
                Collections.emptySet(),
                Collections.emptyList(),
                buildTypeReference(pTypeNode != null && !pTypeNode.isNull() ? text(src, pTypeNode) : "", idGen, range(param)),
                null,
                VariableKind.PARAMETER
            );
        } else if ("default_parameter".equals(pType)) {
            TSNode nameNode = param.getChildByFieldName("name");
            TSNode valNode = param.getChildByFieldName("value");
            if (nameNode != null && !nameNode.isNull()) {
                if ("typed_parameter".equals(nameNode.getType())) {
                    TSNode pName = nameNode.getChildByFieldName("name");
                    TSNode pTypeNode = nameNode.getChildByFieldName("type");
                    return new VariableDeclaration(
                        idGen.nextId(),
                        range(param),
                        pName != null && !pName.isNull() ? text(src, pName) : "?",
                        Collections.emptySet(),
                        Collections.emptyList(),
                        buildTypeReference(pTypeNode != null && !pTypeNode.isNull() ? text(src, pTypeNode) : "", idGen, range(param)),
                        valNode != null && !valNode.isNull() ? buildExpression(valNode, src, idGen) : null,
                        VariableKind.PARAMETER
                    );
                } else {
                    return new VariableDeclaration(
                        idGen.nextId(),
                        range(param),
                        text(src, nameNode),
                        Collections.emptySet(),
                        Collections.emptyList(),
                        buildTypeReference("", idGen, range(param)),
                        valNode != null && !valNode.isNull() ? buildExpression(valNode, src, idGen) : null,
                        VariableKind.PARAMETER
                    );
                }
            }
        } else if ("dictionary_splat_pattern".equals(pType) || "list_splat_pattern".equals(pType)) {
            TSNode inner = param.getChildCount() > 1 ? param.getChild(1) : null;
            return new VariableDeclaration(
                idGen.nextId(),
                range(param),
                inner != null && !inner.isNull() ? text(src, inner) : text(src, param),
                Collections.emptySet(),
                Collections.emptyList(),
                buildTypeReference("", idGen, range(param)),
                null,
                VariableKind.PARAMETER
            );
        }
        return null;
    }

    private List<ImportDeclaration> buildImportDeclarations(TSNode node, byte[] src, NodeIdGenerator idGen) {
        List<ImportDeclaration> imports = new ArrayList<>();
        String type = node.getType();
        if ("import_statement".equals(type)) {
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child == null || child.isNull()) continue;
                String cType = child.getType();
                if ("dotted_name".equals(cType)) {
                    imports.add(new ImportDeclaration(idGen.nextId(), range(child), text(src, child), "", false, false));
                } else if ("aliased_import".equals(cType)) {
                    TSNode nameNode = child.getChildByFieldName("name");
                    TSNode aliasNode = child.getChildByFieldName("alias");
                    String path = nameNode != null && !nameNode.isNull() ? text(src, nameNode) : "";
                    String alias = aliasNode != null && !aliasNode.isNull() ? text(src, aliasNode) : "";
                    imports.add(new ImportDeclaration(idGen.nextId(), range(child), path, alias, false, false));
                }
            }
        } else if ("import_from_statement".equals(type)) {
            TSNode moduleNode = node.getChildByFieldName("module_name");
            String modulePath = moduleNode != null && !moduleNode.isNull() ? text(src, moduleNode) : "";
            
            boolean isWildcard = false;
            for (int i = 0; i < node.getChildCount(); i++) {
                if ("wildcard_import".equals(node.getChild(i).getType())) {
                    isWildcard = true;
                    break;
                }
            }

            if (isWildcard) {
                imports.add(new ImportDeclaration(idGen.nextId(), range(node), modulePath, "", false, true));
            } else {
                for (int i = 0; i < node.getChildCount(); i++) {
                    TSNode child = node.getChild(i);
                    if (child == null || child.isNull()) continue;
                    String cType = child.getType();
                    if ("dotted_name".equals(cType)) {
                        String fullPath = modulePath.isEmpty() ? text(src, child) : modulePath + "." + text(src, child);
                        imports.add(new ImportDeclaration(idGen.nextId(), range(child), fullPath, "", false, false));
                    } else if ("aliased_import".equals(cType)) {
                        TSNode nameNode = child.getChildByFieldName("name");
                        TSNode aliasNode = child.getChildByFieldName("alias");
                        String targetName = nameNode != null && !nameNode.isNull() ? text(src, nameNode) : "";
                        String alias = aliasNode != null && !aliasNode.isNull() ? text(src, aliasNode) : "";
                        String fullPath = modulePath.isEmpty() ? targetName : modulePath + "." + targetName;
                        imports.add(new ImportDeclaration(idGen.nextId(), range(child), fullPath, alias, false, false));
                    }
                }
            }
        }
        return imports;
    }

    private List<DecoratorNode> extractDecorators(TSNode decoratedNode, byte[] src, NodeIdGenerator idGen) {
        List<DecoratorNode> decorators = new ArrayList<>();
        if (decoratedNode == null || !"decorated_definition".equals(decoratedNode.getType())) return decorators;

        for (int i = 0; i < decoratedNode.getChildCount(); i++) {
            TSNode child = decoratedNode.getChild(i);
            if ("decorator".equals(child.getType())) {
                TSNode target = child.getChildCount() > 1 ? child.getChild(1) : null;
                if (target != null && !target.isNull()) {
                    if ("call".equals(target.getType())) {
                        TSNode function = target.getChildByFieldName("function");
                        TSNode arguments = target.getChildByFieldName("arguments");
                        String name = function != null && !function.isNull() ? text(src, function) : "unknown";
                        List<ExpressionNode> args = new ArrayList<>();
                        if (arguments != null && !arguments.isNull()) {
                            for (int j = 0; j < arguments.getChildCount(); j++) {
                                TSNode arg = arguments.getChild(j);
                                if (arg != null && !arg.isNull() && !",".equals(arg.getType()) && !"(".equals(arg.getType()) && !")".equals(arg.getType())) {
                                    args.add(buildExpression(arg, src, idGen));
                                }
                            }
                        }
                        decorators.add(new DecoratorNode(idGen.nextId(), range(child), name, args));
                    } else {
                        String name = text(src, target);
                        decorators.add(new DecoratorNode(idGen.nextId(), range(child), name, Collections.emptyList()));
                    }
                }
            }
        }
        return decorators;
    }

    private StatementNode buildStatement(TSNode node, byte[] src, NodeIdGenerator idGen, boolean inFunction) {
        String type = node.getType();
        if ("if_statement".equals(type)) {
            return buildIfStatement(node, src, idGen, inFunction);
        } else if ("for_statement".equals(type)) {
            TSNode left = node.getChildByFieldName("left");
            TSNode right = node.getChildByFieldName("right");
            TSNode body = node.getChildByFieldName("body");
            ExpressionNode condExpr = null;
            if (left != null && right != null && !left.isNull() && !right.isNull()) {
                condExpr = new BinaryExpression(
                    idGen.nextId(),
                    range(node),
                    buildExpression(left, src, idGen),
                    "in",
                    buildExpression(right, src, idGen)
                );
            }
            return new LoopStatement(
                idGen.nextId(),
                range(node),
                condExpr,
                body != null && !body.isNull() ? buildStatement(body, src, idGen, inFunction) : null
            );
        } else if ("while_statement".equals(type)) {
            TSNode cond = node.getChildByFieldName("condition");
            TSNode body = node.getChildByFieldName("body");
            return new LoopStatement(
                idGen.nextId(),
                range(node),
                cond != null && !cond.isNull() ? buildExpression(cond, src, idGen) : null,
                body != null && !body.isNull() ? buildStatement(body, src, idGen, inFunction) : null
            );
        } else if ("try_statement".equals(type)) {
            TSNode body = node.getChildByFieldName("body");
            List<StatementNode> catchBlocks = new ArrayList<>();
            StatementNode finallyBlock = null;
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if ("except_clause".equals(child.getType())) {
                    TSNode excBody = child.getChildByFieldName("body");
                    if (excBody != null && !excBody.isNull()) {
                        catchBlocks.add(buildStatement(excBody, src, idGen, inFunction));
                    }
                } else if ("finally_clause".equals(child.getType())) {
                    TSNode finBody = child.getChildByFieldName("body");
                    if (finBody != null && !finBody.isNull()) {
                        finallyBlock = buildStatement(finBody, src, idGen, inFunction);
                    }
                }
            }
            return new TryStatement(
                idGen.nextId(),
                range(node),
                body != null && !body.isNull() ? buildStatement(body, src, idGen, inFunction) : null,
                catchBlocks,
                finallyBlock
            );
        } else if ("return_statement".equals(type)) {
            TSNode expr = null;
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child != null && !child.isNull() && !"return".equals(child.getType())) {
                    expr = child;
                    break;
                }
            }
            return new ReturnStatement(
                idGen.nextId(),
                range(node),
                expr != null ? buildExpression(expr, src, idGen) : null
            );
        } else if ("with_statement".equals(type)) {
            List<ExpressionNode> resources = new ArrayList<>();
            TSNode bodyNode = node.getChildByFieldName("body");
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if ("with_item".equals(child.getType())) {
                    TSNode value = child.getChildByFieldName("value");
                    TSNode as = child.getChildByFieldName("as");
                    ExpressionNode resExpr = value != null && !value.isNull() ? buildExpression(value, src, idGen) : null;
                    if (resExpr != null) {
                        if (as != null && !as.isNull()) {
                            resources.add(new AssignmentExpression(
                                idGen.nextId(),
                                range(child),
                                buildExpression(as, src, idGen),
                                resExpr
                            ));
                        } else {
                            resources.add(resExpr);
                        }
                    }
                }
            }
            return new ResourceStatement(
                idGen.nextId(),
                range(node),
                resources,
                bodyNode != null && !bodyNode.isNull() ? buildStatement(bodyNode, src, idGen, inFunction) : null
            );
        } else if ("raise_statement".equals(type)) {
            TSNode expr = null;
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child != null && !child.isNull() && !"raise".equals(child.getType())) {
                    expr = child;
                    break;
                }
            }
            return new ThrowStatement(
                idGen.nextId(),
                range(node),
                expr != null ? buildExpression(expr, src, idGen) : null
            );
        } else if ("match_statement".equals(type)) {
            TSNode subjectNode = node.getChildByFieldName("subject");
            List<MatchStatement.MatchCase> cases = new ArrayList<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if ("case_block".equals(child.getType())) {
                    TSNode patternNode = child.getChildByFieldName("pattern");
                    TSNode consequenceNode = child.getChildByFieldName("consequence");
                    ExpressionNode patternExpr = patternNode != null && !patternNode.isNull() ? buildExpression(patternNode, src, idGen) : null;
                    StatementNode caseBody = consequenceNode != null && !consequenceNode.isNull() ? buildStatement(consequenceNode, src, idGen, inFunction) : null;
                    cases.add(new MatchStatement.MatchCase(patternExpr, caseBody));
                }
            }
            return new MatchStatement(
                idGen.nextId(),
                range(node),
                subjectNode != null && !subjectNode.isNull() ? buildExpression(subjectNode, src, idGen) : null,
                cases
            );
        } else if ("class_definition".equals(type) || ("decorated_definition".equals(type) && isClassDef(node))) {
            return buildTypeDeclaration(node, src, idGen);
        } else if ("function_definition".equals(type) || ("decorated_definition".equals(type) && isFuncDef(node))) {
            return buildCallableDeclaration(node, src, idGen, CallableScope.LOCAL);
        } else if ("expression_statement".equals(type)) {
            TSNode expr = node.getChildCount() > 0 ? node.getChild(0) : null;
            if (expr != null && !expr.isNull() && ("assignment".equals(expr.getType()) || "augmented_assignment".equals(expr.getType()))) {
                TSNode lhs = expr.getChildByFieldName("left");
                TSNode val = expr.getChildByFieldName("value");
                if (lhs != null && !lhs.isNull() && "identifier".equals(lhs.getType()) && inFunction) {
                    return new LocalVariableStatement(
                        idGen.nextId(),
                        range(node),
                        new VariableDeclaration(
                            idGen.nextId(),
                            range(node),
                            text(src, lhs),
                            Collections.emptySet(),
                            Collections.emptyList(),
                            buildTypeReference("", idGen, range(node)),
                            val != null && !val.isNull() ? buildExpression(val, src, idGen) : null,
                            VariableKind.LOCAL
                        )
                    );
                } else {
                    return new ExpressionStatement(idGen.nextId(), range(node), buildExpression(expr, src, idGen));
                }
            } else {
                return new ExpressionStatement(idGen.nextId(), range(node), expr != null ? buildExpression(expr, src, idGen) : null);
            }
        } else if ("annotated_assignment".equals(type)) {
            TSNode lhs = node.getChildByFieldName("left");
            TSNode val = node.getChildByFieldName("value");
            TSNode typeNode = node.getChildByFieldName("type");
            if (lhs != null && !lhs.isNull() && "identifier".equals(lhs.getType()) && inFunction) {
                return new LocalVariableStatement(
                    idGen.nextId(),
                    range(node),
                    new VariableDeclaration(
                        idGen.nextId(),
                        range(node),
                        text(src, lhs),
                        Collections.emptySet(),
                        Collections.emptyList(),
                        buildTypeReference(typeNode != null && !typeNode.isNull() ? text(src, typeNode) : "", idGen, range(node)),
                        val != null && !val.isNull() ? buildExpression(val, src, idGen) : null,
                        VariableKind.LOCAL
                    )
                );
            } else {
                return new ExpressionStatement(idGen.nextId(), range(node), buildExpression(node, src, idGen));
            }
        } else {
            List<StatementNode> subStmts = new ArrayList<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child.getType().endsWith("statement") || "class_definition".equals(child.getType()) || "function_definition".equals(child.getType()) || "decorated_definition".equals(child.getType())) {
                    subStmts.add(buildStatement(child, src, idGen, inFunction));
                }
            }
            return new BlockStatement(idGen.nextId(), range(node), subStmts);
        }
    }

    private StatementNode buildIfStatement(TSNode node, byte[] src, NodeIdGenerator idGen, boolean inFunction) {
        TSNode cond = node.getChildByFieldName("condition");
        TSNode consequence = node.getChildByFieldName("consequence");
        TSNode alternative = node.getChildByFieldName("alternative");

        StatementNode elseBranch = null;
        if (alternative != null && !alternative.isNull()) {
            if ("elif_clause".equals(alternative.getType())) {
                elseBranch = buildElifClause(alternative, src, idGen, inFunction);
            } else if ("else_clause".equals(alternative.getType())) {
                TSNode elseBody = alternative.getChildByFieldName("body");
                elseBranch = elseBody != null && !elseBody.isNull() ? buildStatement(elseBody, src, idGen, inFunction) : null;
            } else {
                elseBranch = buildStatement(alternative, src, idGen, inFunction);
            }
        }

        return new IfStatement(
            idGen.nextId(),
            range(node),
            cond != null && !cond.isNull() ? buildExpression(cond, src, idGen) : null,
            consequence != null && !consequence.isNull() ? buildStatement(consequence, src, idGen, inFunction) : null,
            elseBranch
        );
    }

    private StatementNode buildElifClause(TSNode node, byte[] src, NodeIdGenerator idGen, boolean inFunction) {
        TSNode cond = node.getChildByFieldName("condition");
        TSNode consequence = node.getChildByFieldName("consequence");
        TSNode alternative = node.getChildByFieldName("alternative");

        StatementNode elseBranch = null;
        if (alternative != null && !alternative.isNull()) {
            if ("elif_clause".equals(alternative.getType())) {
                elseBranch = buildElifClause(alternative, src, idGen, inFunction);
            } else if ("else_clause".equals(alternative.getType())) {
                TSNode elseBody = alternative.getChildByFieldName("body");
                elseBranch = elseBody != null && !elseBody.isNull() ? buildStatement(elseBody, src, idGen, inFunction) : null;
            } else {
                elseBranch = buildStatement(alternative, src, idGen, inFunction);
            }
        }

        return new IfStatement(
            idGen.nextId(),
            range(node),
            cond != null && !cond.isNull() ? buildExpression(cond, src, idGen) : null,
            consequence != null && !consequence.isNull() ? buildStatement(consequence, src, idGen, inFunction) : null,
            elseBranch
        );
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
            if (function != null && "attribute".equals(function.getType())) {
                TSNode objNode = function.getChildByFieldName("object");
                TSNode attrNode = function.getChildByFieldName("attribute");
                ExpressionNode receiver = objNode != null && !objNode.isNull() ? buildExpression(objNode, src, idGen) : null;
                String callableName = attrNode != null && !attrNode.isNull() ? text(src, attrNode) : "unknown";
                return new CallExpression(
                    idGen.nextId(),
                    range(node),
                    receiver,
                    callableName,
                    args,
                    CallKind.INSTANCE
                );
            } else {
                ExpressionNode receiver = function != null && !function.isNull() ? buildExpression(function, src, idGen) : null;
                String callableName = receiver != null ? text(src, function) : "unknown";
                return new CallExpression(
                    idGen.nextId(),
                    range(node),
                    receiver,
                    callableName,
                    args,
                    CallKind.UNKNOWN
                );
            }
        } else if ("attribute".equals(type)) {
            TSNode obj = node.getChildByFieldName("object");
            TSNode attr = node.getChildByFieldName("attribute");
            return new FieldAccessExpression(
                idGen.nextId(),
                range(node),
                obj != null && !obj.isNull() ? buildExpression(obj, src, idGen) : null,
                attr != null && !attr.isNull() ? text(src, attr) : ""
            );
        } else if ("identifier".equals(type)) {
            return new VariableReferenceExpression(idGen.nextId(), range(node), text(src, node));
        } else if ("binary_operator".equals(type) || "comparison_operator".equals(type) || "boolean_operator".equals(type)) {
            TSNode left = node.getChildByFieldName("left");
            TSNode right = node.getChildByFieldName("right");
            if (left == null || left.isNull()) left = node.getChild(0);
            if (right == null || right.isNull()) right = node.getChild(node.getChildCount() - 1);
            String op = "";
            if (node.getChildCount() >= 3) {
                op = text(src, node.getChild(1));
            }
            return new BinaryExpression(
                idGen.nextId(),
                range(node),
                left != null && !left.isNull() ? buildExpression(left, src, idGen) : null,
                op,
                right != null && !right.isNull() ? buildExpression(right, src, idGen) : null
            );
        } else if ("not_operator".equals(type)) {
            TSNode expr = node.getChild(1);
            return new UnaryExpression(
                idGen.nextId(),
                range(node),
                "not",
                expr != null && !expr.isNull() ? buildExpression(expr, src, idGen) : null,
                false
            );
        } else if ("unary_operator".equals(type)) {
            TSNode opNode = node.getChild(0);
            TSNode expr = node.getChild(1);
            return new UnaryExpression(
                idGen.nextId(),
                range(node),
                opNode != null ? text(src, opNode) : "",
                expr != null && !expr.isNull() ? buildExpression(expr, src, idGen) : null,
                false
            );
        } else if ("assignment".equals(type) || "augmented_assignment".equals(type)) {
            TSNode left = node.getChildByFieldName("left");
            TSNode right = node.getChildByFieldName("right");
            if (right == null || right.isNull()) {
                right = node.getChildByFieldName("value");
            }
            String op = "=";
            if ("augmented_assignment".equals(type)) {
                op = node.getChildCount() > 1 ? text(src, node.getChild(1)) : "=";
            }
            return new AssignmentExpression(
                idGen.nextId(),
                range(node),
                left != null && !left.isNull() ? buildExpression(left, src, idGen) : null,
                right != null && !right.isNull() ? buildExpression(right, src, idGen) : null
            );
        } else if ("conditional_expression".equals(type)) {
            TSNode cond = node.getChildByFieldName("condition");
            TSNode consequent = node.getChildByFieldName("consequent");
            TSNode alternative = node.getChildByFieldName("alternative");
            return new ConditionalExpression(
                idGen.nextId(),
                range(node),
                cond != null && !cond.isNull() ? buildExpression(cond, src, idGen) : null,
                consequent != null && !consequent.isNull() ? buildExpression(consequent, src, idGen) : null,
                alternative != null && !alternative.isNull() ? buildExpression(alternative, src, idGen) : null
            );
        } else if ("lambda".equals(type)) {
            TSNode paramsNode = node.getChildByFieldName("parameters");
            TSNode bodyNode = node.getChildByFieldName("body");
            List<VariableDeclaration> parameters = new ArrayList<>();
            if (paramsNode != null && !paramsNode.isNull()) {
                for (int i = 0; i < paramsNode.getChildCount(); i++) {
                    TSNode param = paramsNode.getChild(i);
                    if (param == null || param.isNull() || ",".equals(param.getType())) continue;
                    VariableDeclaration varDecl = buildParameter(param, src, idGen);
                    if (varDecl != null) {
                        parameters.add(varDecl);
                    }
                }
            }
            StatementNode bodyStmt = null;
            if (bodyNode != null && !bodyNode.isNull()) {
                bodyStmt = new BlockStatement(
                    idGen.nextId(),
                    range(bodyNode),
                    List.of(new ExpressionStatement(idGen.nextId(), range(bodyNode), buildExpression(bodyNode, src, idGen)))
                );
            }
            return new LambdaExpression(
                idGen.nextId(),
                range(node),
                parameters,
                bodyStmt
            );
        } else if ("subscript".equals(type)) {
            TSNode value = node.getChildByFieldName("value");
            TSNode subscript = node.getChildByFieldName("subscript");
            return new ArrayAccessExpression(
                idGen.nextId(),
                range(node),
                value != null && !value.isNull() ? buildExpression(value, src, idGen) : null,
                subscript != null && !subscript.isNull() ? buildExpression(subscript, src, idGen) : null
            );
        } else if ("string".equals(type)) {
            return new LiteralExpression(idGen.nextId(), range(node), text(src, node), LiteralKind.STRING);
        } else if ("integer".equals(type) || "float".equals(type)) {
            return new LiteralExpression(idGen.nextId(), range(node), text(src, node), LiteralKind.NUMBER);
        } else if ("true".equals(type) || "false".equals(type)) {
            return new LiteralExpression(idGen.nextId(), range(node), Boolean.parseBoolean(text(src, node)), LiteralKind.BOOLEAN);
        } else if ("none".equals(type)) {
            return new LiteralExpression(idGen.nextId(), range(node), null, LiteralKind.NULL);
        } else if ("list".equals(type) || "set".equals(type) || "tuple".equals(type)) {
            CollectionLiteralExpression.CollectionKind kind = CollectionLiteralExpression.CollectionKind.LIST;
            if ("set".equals(type)) kind = CollectionLiteralExpression.CollectionKind.SET;
            else if ("tuple".equals(type)) kind = CollectionLiteralExpression.CollectionKind.TUPLE;

            List<ExpressionNode> elements = new ArrayList<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child == null || child.isNull()) continue;
                String cType = child.getType();
                if (",".equals(cType) || "[".equals(cType) || "]".equals(cType) || "{".equals(cType) || "}".equals(cType) || "(".equals(cType) || ")".equals(cType)) continue;
                elements.add(buildExpression(child, src, idGen));
            }
            return new CollectionLiteralExpression(idGen.nextId(), range(node), elements, kind);
        } else if ("dictionary".equals(type)) {
            List<DictionaryExpression.Entry> entries = new ArrayList<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child == null || child.isNull()) continue;
                if ("dictionary_key_value_pair".equals(child.getType())) {
                    TSNode keyNode = child.getChildByFieldName("key");
                    TSNode valNode = child.getChildByFieldName("value");
                    if (keyNode != null && valNode != null) {
                        entries.add(new DictionaryExpression.Entry(
                            buildExpression(keyNode, src, idGen),
                            buildExpression(valNode, src, idGen)
                        ));
                    }
                }
            }
            return new DictionaryExpression(idGen.nextId(), range(node), entries);
        } else if ("list_comprehension".equals(type) || "set_comprehension".equals(type) || "dictionary_comprehension".equals(type) || "generator_expression".equals(type)) {
            ComprehensionExpression.ComprehensionKind kind = ComprehensionExpression.ComprehensionKind.LIST;
            if ("set_comprehension".equals(type)) kind = ComprehensionExpression.ComprehensionKind.SET;
            else if ("dictionary_comprehension".equals(type)) kind = ComprehensionExpression.ComprehensionKind.DICT;
            else if ("generator_expression".equals(type)) kind = ComprehensionExpression.ComprehensionKind.GENERATOR;

            TSNode bodyNode = node.getChildByFieldName("body");
            ExpressionNode element = null;
            ExpressionNode key = null;
            
            if ("dictionary_comprehension".equals(type)) {
                TSNode keyNode = node.getChildByFieldName("key");
                TSNode valNode = node.getChildByFieldName("value");
                if (keyNode != null) key = buildExpression(keyNode, src, idGen);
                if (valNode != null) element = buildExpression(valNode, src, idGen);
            } else {
                if (bodyNode != null) element = buildExpression(bodyNode, src, idGen);
            }

            List<ComprehensionExpression.Generator> generators = new ArrayList<>();
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if ("for_in_clause".equals(child.getType())) {
                    collectComprehensionGenerators(child, src, generators, idGen);
                    break;
                }
            }

            return new ComprehensionExpression(idGen.nextId(), range(node), element, key, generators, kind);
        } else if ("await".equals(type)) {
            TSNode expr = node.getChildCount() > 1 ? node.getChild(1) : null;
            return new AwaitExpression(
                idGen.nextId(),
                range(node),
                expr != null ? buildExpression(expr, src, idGen) : null
            );
        } else if ("yield".equals(type)) {
            boolean isYieldFrom = text(src, node).startsWith("yield from");
            TSNode expr = null;
            for (int i = 0; i < node.getChildCount(); i++) {
                TSNode child = node.getChild(i);
                if (child != null && !"yield".equals(child.getType()) && !"from".equals(child.getType())) {
                    expr = child;
                    break;
                }
            }
            return new YieldExpression(
                idGen.nextId(),
                range(node),
                expr != null ? buildExpression(expr, src, idGen) : null,
                isYieldFrom
            );
        } else if ("parenthesized_expression".equals(type)) {
            TSNode inner = node.getChildCount() > 1 ? node.getChild(1) : null;
            return inner != null ? buildExpression(inner, src, idGen) : buildTypeReference(text(src, node), idGen, range(node));
        } else {
            return buildTypeReference(text(src, node), idGen, range(node));
        }
    }

    private void collectComprehensionGenerators(TSNode clause, byte[] src, List<ComprehensionExpression.Generator> out, NodeIdGenerator idGen) {
        if (clause == null || clause.isNull()) return;
        if ("for_in_clause".equals(clause.getType())) {
            TSNode left = clause.getChildByFieldName("left");
            TSNode right = clause.getChildByFieldName("right");
            List<ExpressionNode> conditions = new ArrayList<>();
            
            TSNode body = clause.getChildByFieldName("body");
            while (body != null && !body.isNull() && "if_clause".equals(body.getType())) {
                TSNode cond = body.getChildByFieldName("condition");
                if (cond != null && !cond.isNull()) {
                    conditions.add(buildExpression(cond, src, idGen));
                }
                body = body.getChildByFieldName("body");
            }
            
            out.add(new ComprehensionExpression.Generator(
                left != null ? buildExpression(left, src, idGen) : null,
                right != null ? buildExpression(right, src, idGen) : null,
                conditions
            ));

            if (body != null && !body.isNull() && "for_in_clause".equals(body.getType())) {
                collectComprehensionGenerators(body, src, out, idGen);
            }
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

    private String computeNamespace(Path filePath, List<Path> sourceRoots) {
        if (filePath == null) return "";
        Path absoluteFilePath = filePath.toAbsolutePath().normalize();
        
        for (Path root : sourceRoots) {
            Path absoluteRoot = root.toAbsolutePath().normalize();
            if (absoluteFilePath.startsWith(absoluteRoot)) {
                Path relative = absoluteRoot.relativize(absoluteFilePath);
                return pathToNamespace(relative);
            }
        }
        
        Path cwd = Path.of("").toAbsolutePath().normalize();
        if (absoluteFilePath.startsWith(cwd)) {
            Path relative = cwd.relativize(absoluteFilePath);
            return pathToNamespace(relative);
        }
        
        return "";
    }

    private String pathToNamespace(Path relativePath) {
        String pathStr = relativePath.toString();
        if (pathStr.endsWith(".py")) {
            pathStr = pathStr.substring(0, pathStr.length() - 3);
        }
        
        pathStr = pathStr.replace(java.io.File.separatorChar, '.');
        pathStr = pathStr.replace('/', '.');
        pathStr = pathStr.replace('\\', '.');
        
        if (pathStr.endsWith(".__init__")) {
            pathStr = pathStr.substring(0, pathStr.length() - 9);
        } else if (pathStr.equals("__init__")) {
            pathStr = "";
        }
        
        return pathStr;
    }

    private String extractPythonDocstring(TSNode body, byte[] src) {
        if (body == null || body.isNull() || body.getChildCount() == 0) {
            return "";
        }
        TSNode first = body.getChild(0);
        if ("expression_statement".equals(first.getType()) && first.getChildCount() > 0) {
            TSNode expr = first.getChild(0);
            if ("string".equals(expr.getType())) {
                String raw = text(src, expr);
                if (raw.startsWith("\"\"\"") && raw.endsWith("\"\"\"") && raw.length() >= 6) {
                    return raw.substring(3, raw.length() - 3).trim();
                }
                if (raw.startsWith("'''") && raw.endsWith("'''") && raw.length() >= 6) {
                    return raw.substring(3, raw.length() - 3).trim();
                }
                if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
                    return raw.substring(1, raw.length() - 1).trim();
                }
                if (raw.startsWith("'") && raw.endsWith("'") && raw.length() >= 2) {
                    return raw.substring(1, raw.length() - 1).trim();
                }
                return raw.trim();
            }
        }
        return "";
    }
}
