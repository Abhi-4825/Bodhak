package com.example.anuviya.frontend.java;

import com.example.anuviya.ir.*;
import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.ir.statement.*;
import com.example.anuviya.ir.expression.*;
import com.example.anuviya.compiler.symbol.QualifiedName;
import com.example.anuviya.frontend.IRBuilder;
import com.example.anuviya.model.entity.ModifierKind;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parses JavaParser CompilationUnit AST into the normalized, language-independent IRNode structure.
 */
public class JavaIRBuilder implements IRBuilder<CompilationUnit> {

    @Override
    public IRNode build(Path filePath, CompilationUnit cu) {
        if (filePath == null) {
            filePath = Path.of("Unknown.java");
        }
        NodeIdGenerator idGen = new NodeIdGenerator(filePath.toString());
        
        if (cu == null) {
            return new ModuleDeclaration(idGen.nextId(), SourceRange.UNKNOWN, "", "java", filePath.toString(), Collections.emptyList(), Collections.emptyList(), "", Collections.emptyList());
        }

        String moduleName = filePath.getFileName().toString().replace(".java", "");
        List<ImportDeclaration> imports = new ArrayList<>();
        List<DeclarationNode> declarations = new ArrayList<>();

        // Package declaration -> NamespaceDeclaration
        cu.getPackageDeclaration().ifPresent(pd -> {
            declarations.add(new NamespaceDeclaration(idGen.nextId(), range(pd), pd.getNameAsString()));
        });

        // Imports -> ImportDeclaration
        cu.getImports().forEach(im -> {
            imports.add(new ImportDeclaration(idGen.nextId(), range(im), im.getNameAsString(), "", im.isStatic(), im.isAsterisk()));
        });

        // Types (Classes, Interfaces, Enums, Records) -> TypeDeclaration
        cu.getTypes().forEach(t -> {
            declarations.add(buildTypeDeclaration(t, idGen));
        });

        return new ModuleDeclaration(idGen.nextId(), range(cu), moduleName, "java", filePath.toString(), imports, declarations, "", Collections.emptyList());
    }

    private TypeDeclaration buildTypeDeclaration(com.github.javaparser.ast.body.TypeDeclaration<?> t, NodeIdGenerator idGen) {
        String name = t.getNameAsString();
        
        Set<ModifierKind> modifiers = t.getModifiers().stream()
                .map(m -> ModifierKind.from(m.getKeyword()))
                .collect(Collectors.toSet());

        List<DecoratorNode> decorators = buildDecorators(t.getAnnotations(), idGen);

        TypeKind typeKind = TypeKind.CLASS;
        List<TypeReferenceExpression> extendsTypes = new ArrayList<>();
        List<TypeReferenceExpression> implementsTypes = new ArrayList<>();
        List<TypeReferenceExpression> permitsTypes = new ArrayList<>();

        if (t instanceof ClassOrInterfaceDeclaration cid) {
            cid.getExtendedTypes().forEach(et -> extendsTypes.add(buildTypeReference(et, idGen)));
            cid.getImplementedTypes().forEach(it -> implementsTypes.add(buildTypeReference(it, idGen)));
            if (cid.isInterface()) {
                typeKind = TypeKind.INTERFACE;
            }
        } else if (t instanceof EnumDeclaration ed) {
            typeKind = TypeKind.ENUM;
        } else if (t instanceof RecordDeclaration rd) {
            typeKind = TypeKind.RECORD;
        }

        List<DeclarationNode> members = new ArrayList<>();
        t.getMembers().forEach(member -> {
            if (member instanceof FieldDeclaration fd) {
                fd.getVariables().forEach(v -> {
                    members.add(new VariableDeclaration(
                        idGen.nextId(),
                        range(fd),
                        v.getNameAsString(),
                        fd.getModifiers().stream().map(m -> ModifierKind.from(m.getKeyword())).collect(Collectors.toSet()),
                        buildDecorators(fd.getAnnotations(), idGen),
                        buildTypeReference(v.getType(), idGen),
                        v.getInitializer().map(init -> buildExpression(init, idGen)).orElse(null),
                        VariableKind.FIELD
                    ));
                });
            } else if (member instanceof MethodDeclaration md) {
                members.add(buildCallableDeclaration(md, idGen));
            } else if (member instanceof ConstructorDeclaration cd) {
                members.add(buildCallableDeclaration(cd, idGen));
            } else if (member instanceof com.github.javaparser.ast.body.TypeDeclaration<?> nestedType) {
                members.add(buildTypeDeclaration(nestedType, idGen));
            }
        });

        String documentation = t.getComment().map(com.github.javaparser.ast.comments.Comment::getContent).map(this::cleanJavaComment).orElse("");

        return new TypeDeclaration(
            idGen.nextId(), range(t), name, typeKind, modifiers, decorators, extendsTypes, implementsTypes, permitsTypes, members, documentation
        );
    }

    private CallableDeclaration buildCallableDeclaration(MethodDeclaration md, NodeIdGenerator idGen) {
        String name = md.getNameAsString();
        
        Set<ModifierKind> modifiers = md.getModifiers().stream()
                .map(m -> ModifierKind.from(m.getKeyword()))
                .collect(Collectors.toSet());

        List<DecoratorNode> decorators = buildDecorators(md.getAnnotations(), idGen);

        List<VariableDeclaration> parameters = md.getParameters().stream().map(p -> 
            new VariableDeclaration(
                idGen.nextId(),
                range(p),
                p.getNameAsString(),
                Collections.emptySet(),
                buildDecorators(p.getAnnotations(), idGen),
                buildTypeReference(p.getType(), idGen),
                null,
                VariableKind.PARAMETER
            )
        ).collect(Collectors.toList());

        TypeReferenceExpression returnType = buildTypeReference(md.getType(), idGen);
        
        List<TypeReferenceExpression> throwsTypes = md.getThrownExceptions().stream()
            .map(t -> buildTypeReference(t, idGen))
            .collect(Collectors.toList());
            
        List<String> genericParameters = md.getTypeParameters().stream()
            .map(tp -> tp.getNameAsString())
            .collect(Collectors.toList());

        BlockStatement body = md.getBody().map(b -> buildBlockStatement(b, idGen)).orElse(null);

        boolean isAbstract = md.isAbstract();
        boolean isDefault = md.isDefault();

        String documentation = md.getComment().map(com.github.javaparser.ast.comments.Comment::getContent).map(this::cleanJavaComment).orElse("");

        return new CallableDeclaration(
            idGen.nextId(), range(md), name, modifiers, decorators, parameters, returnType, 
            throwsTypes, genericParameters, body, false, isAbstract, isDefault, false, CallableScope.TYPE, documentation
        );
    }

    private CallableDeclaration buildCallableDeclaration(ConstructorDeclaration cd, NodeIdGenerator idGen) {
        String name = cd.getNameAsString();
        
        Set<ModifierKind> modifiers = cd.getModifiers().stream()
                .map(m -> ModifierKind.from(m.getKeyword()))
                .collect(Collectors.toSet());

        List<DecoratorNode> decorators = buildDecorators(cd.getAnnotations(), idGen);

        List<VariableDeclaration> parameters = cd.getParameters().stream().map(p -> 
            new VariableDeclaration(
                idGen.nextId(),
                range(p),
                p.getNameAsString(),
                Collections.emptySet(),
                buildDecorators(p.getAnnotations(), idGen),
                buildTypeReference(p.getType(), idGen),
                null,
                VariableKind.PARAMETER
            )
        ).collect(Collectors.toList());

        TypeReferenceExpression returnType = buildTypeReference("void", idGen, SourceRange.UNKNOWN);
        
        List<TypeReferenceExpression> throwsTypes = cd.getThrownExceptions().stream()
            .map(t -> buildTypeReference(t, idGen))
            .collect(Collectors.toList());
            
        List<String> genericParameters = cd.getTypeParameters().stream()
            .map(tp -> tp.getNameAsString())
            .collect(Collectors.toList());

        BlockStatement body = buildBlockStatement(cd.getBody(), idGen);

        String documentation = cd.getComment().map(com.github.javaparser.ast.comments.Comment::getContent).map(this::cleanJavaComment).orElse("");

        return new CallableDeclaration(
            idGen.nextId(), range(cd), name, modifiers, decorators, parameters, returnType, 
            throwsTypes, genericParameters, body, true, false, false, false, CallableScope.TYPE, documentation
        );
    }

    private BlockStatement buildBlockStatement(BlockStmt b, NodeIdGenerator idGen) {
        List<StatementNode> statements = b.getStatements().stream()
                .map(s -> buildStatement(s, idGen))
                .collect(Collectors.toList());
        return new BlockStatement(idGen.nextId(), range(b), statements);
    }

    private StatementNode buildStatement(Statement s, NodeIdGenerator idGen) {
        if (s instanceof BlockStmt bs) {
            return buildBlockStatement(bs, idGen);
        } else if (s instanceof IfStmt is) {
            return new IfStatement(
                idGen.nextId(),
                range(is),
                buildExpression(is.getCondition(), idGen),
                buildStatement(is.getThenStmt(), idGen),
                is.getElseStmt().map(es -> buildStatement(es, idGen)).orElse(null)
            );
        } else if (s instanceof WhileStmt ws) {
            return new LoopStatement(idGen.nextId(), range(ws), buildExpression(ws.getCondition(), idGen), buildStatement(ws.getBody(), idGen));
        } else if (s instanceof ForStmt fs) {
            return new LoopStatement(idGen.nextId(), range(fs), fs.getCompare().map(c -> buildExpression(c, idGen)).orElse(null), buildStatement(fs.getBody(), idGen));
        } else if (s instanceof ForEachStmt fes) {
            return new LoopStatement(idGen.nextId(), range(fes), buildExpression(fes.getIterable(), idGen), buildStatement(fes.getBody(), idGen));
        } else if (s instanceof SwitchStmt ss) {
            List<MatchStatement.MatchCase> cases = new ArrayList<>();
            ss.getEntries().forEach(entry -> {
                ExpressionNode pattern = entry.getLabels().isEmpty() ? null : buildExpression(entry.getLabels().get(0), idGen);
                List<StatementNode> bodyStmts = entry.getStatements().stream().map(st -> buildStatement(st, idGen)).collect(Collectors.toList());
                StatementNode body = new BlockStatement(idGen.nextId(), range(entry), bodyStmts);
                cases.add(new MatchStatement.MatchCase(pattern, body));
            });
            return new MatchStatement(idGen.nextId(), range(ss), buildExpression(ss.getSelector(), idGen), cases);
        } else if (s instanceof ThrowStmt ts) {
            return new ThrowStatement(idGen.nextId(), range(ts), buildExpression(ts.getExpression(), idGen));
        } else if (s instanceof DoStmt ds) {
            return new LoopStatement(idGen.nextId(), range(ds), buildExpression(ds.getCondition(), idGen), buildStatement(ds.getBody(), idGen));
        } else if (s instanceof SynchronizedStmt ss) {
            return new ResourceStatement(idGen.nextId(), range(ss), List.of(buildExpression(ss.getExpression(), idGen)), buildBlockStatement(ss.getBody(), idGen));
        } else if (s instanceof TryStmt ts) {
            List<StatementNode> catches = ts.getCatchClauses().stream()
                    .map(cc -> buildStatement(cc.getBody(), idGen))
                    .collect(Collectors.toList());
            return new TryStatement(
                idGen.nextId(),
                range(ts),
                buildBlockStatement(ts.getTryBlock(), idGen),
                catches,
                ts.getFinallyBlock().map(fb -> buildBlockStatement(fb, idGen)).orElse(null)
            );
        } else if (s instanceof ReturnStmt rs) {
            return new ReturnStatement(idGen.nextId(), range(rs), rs.getExpression().map(expr -> buildExpression(expr, idGen)).orElse(null));
        } else if (s instanceof ExpressionStmt es
                && es.getExpression() instanceof com.github.javaparser.ast.expr.VariableDeclarationExpr vde) {
            List<StatementNode> decls = new ArrayList<>();
            for (com.github.javaparser.ast.body.VariableDeclarator v : vde.getVariables()) {
                TypeReferenceExpression typeRef = buildTypeReference(v.getType(), idGen);
                ExpressionNode initializer = v.getInitializer()
                        .map(init -> buildExpression(init, idGen)).orElse(null);
                
                VariableDeclaration varDecl = new VariableDeclaration(
                    idGen.nextId(),
                    range(v),
                    v.getNameAsString(),
                    Collections.emptySet(),
                    Collections.emptyList(),
                    typeRef,
                    initializer,
                    VariableKind.LOCAL
                );
                decls.add(new LocalVariableStatement(idGen.nextId(), range(vde), varDecl));
            }
            if (decls.size() == 1) {
                return decls.get(0);
            } else {
                return new BlockStatement(idGen.nextId(), range(s), decls);
            }
        } else if (s instanceof ExpressionStmt es) {
            return new ExpressionStatement(idGen.nextId(), range(es), buildExpression(es.getExpression(), idGen));
        } else {
            return new BlockStatement(idGen.nextId(), range(s), Collections.emptyList());
        }
    }

    private ExpressionNode buildExpression(Expression e, NodeIdGenerator idGen) {
        if (e instanceof MethodCallExpr mce) {
            ExpressionNode receiver = mce.getScope().map(s -> buildExpression(s, idGen)).orElse(new ThisExpression(idGen.nextId(), SourceRange.UNKNOWN));
            List<ExpressionNode> args = mce.getArguments().stream().map(arg -> buildExpression(arg, idGen)).collect(Collectors.toList());
            
            CallKind kind = CallKind.INSTANCE;
            if (receiver instanceof SuperExpression) {
                kind = CallKind.SUPER;
            } else if (receiver instanceof ThisExpression) {
                kind = CallKind.THIS;
            }
            return new CallExpression(idGen.nextId(), range(mce), receiver, mce.getNameAsString(), args, kind);
            
        } else if (e instanceof ObjectCreationExpr oce) {
            List<ExpressionNode> args = oce.getArguments().stream().map(arg -> buildExpression(arg, idGen)).collect(Collectors.toList());
            TypeReferenceExpression typeRef = buildTypeReference(oce.getType(), idGen);
            
            List<TypeReferenceExpression> genericArgs = new ArrayList<>();
            oce.getType().getTypeArguments().ifPresent(gArgs -> {
                gArgs.forEach(ga -> genericArgs.add(buildTypeReference(ga, idGen)));
            });
            
            boolean isAnonymous = oce.getAnonymousClassBody().isPresent();
            return new ObjectCreationExpression(idGen.nextId(), range(oce), typeRef, genericArgs, args, isAnonymous);
            
        } else if (e instanceof com.github.javaparser.ast.expr.VariableDeclarationExpr vde) {
            List<ExpressionNode> children = new ArrayList<>();
            vde.getVariables().forEach(v -> {
                v.getInitializer().ifPresent(init -> children.add(buildExpression(init, idGen)));
                children.add(buildTypeReference(v.getType(), idGen));
            });
            return new CallExpression(idGen.nextId(), range(vde), new TypeReferenceExpression(idGen.nextId(), SourceRange.UNKNOWN, QualifiedName.of("var_decl"), Collections.emptyList(), 0, Variance.INVARIANT, Collections.emptyList()), "var", children, CallKind.UNKNOWN);
            
        } else if (e instanceof com.github.javaparser.ast.expr.AssignExpr ae) {
            return new AssignmentExpression(idGen.nextId(), range(ae), buildExpression(ae.getTarget(), idGen), buildExpression(ae.getValue(), idGen));
            
        } else if (e instanceof com.github.javaparser.ast.expr.EnclosedExpr ee) {
            return buildExpression(ee.getInner(), idGen);
            
        } else if (e instanceof com.github.javaparser.ast.expr.CastExpr ce) {
            return new CastExpression(idGen.nextId(), range(ce), buildTypeReference(ce.getType(), idGen), buildExpression(ce.getExpression(), idGen));
            
        } else if (e instanceof com.github.javaparser.ast.expr.FieldAccessExpr fae) {
            return new FieldAccessExpression(idGen.nextId(), range(fae), buildExpression(fae.getScope(), idGen), fae.getNameAsString());
            
        } else if (e instanceof com.github.javaparser.ast.expr.NameExpr ne) {
            return new VariableReferenceExpression(idGen.nextId(), range(ne), ne.getNameAsString());
            
        } else if (e instanceof com.github.javaparser.ast.expr.LiteralExpr le) {
            LiteralKind kind = LiteralKind.STRING;
            Object val = le.toString();
            if (le instanceof com.github.javaparser.ast.expr.IntegerLiteralExpr ile) {
                kind = LiteralKind.NUMBER;
                try { val = Integer.parseInt(ile.getValue()); } catch (Exception ex) {}
            } else if (le instanceof com.github.javaparser.ast.expr.DoubleLiteralExpr dle) {
                kind = LiteralKind.NUMBER;
                try { val = Double.parseDouble(dle.getValue()); } catch (Exception ex) {}
            } else if (le instanceof com.github.javaparser.ast.expr.BooleanLiteralExpr ble) {
                kind = LiteralKind.BOOLEAN;
                val = ble.getValue();
            } else if (le instanceof com.github.javaparser.ast.expr.NullLiteralExpr nle) {
                kind = LiteralKind.NULL;
                val = null;
            } else if (le instanceof com.github.javaparser.ast.expr.CharLiteralExpr cle) {
                kind = LiteralKind.CHAR;
                val = cle.getValue();
            } else if (le instanceof com.github.javaparser.ast.expr.StringLiteralExpr sle) {
                kind = LiteralKind.STRING;
                val = sle.getValue();
            }
            return new LiteralExpression(idGen.nextId(), range(le), val, kind);
            
        } else if (e instanceof com.github.javaparser.ast.expr.BinaryExpr be) {
            return new BinaryExpression(idGen.nextId(), range(be), buildExpression(be.getLeft(), idGen), be.getOperator().asString(), buildExpression(be.getRight(), idGen));
            
        } else if (e instanceof com.github.javaparser.ast.expr.UnaryExpr ue) {
            return new UnaryExpression(idGen.nextId(), range(ue), ue.getOperator().asString(), buildExpression(ue.getExpression(), idGen), ue.isPostfix());
            
        } else if (e instanceof com.github.javaparser.ast.expr.ConditionalExpr ce) {
            return new ConditionalExpression(idGen.nextId(), range(ce), buildExpression(ce.getCondition(), idGen), buildExpression(ce.getThenExpr(), idGen), buildExpression(ce.getElseExpr(), idGen));
            
        } else if (e instanceof com.github.javaparser.ast.expr.ArrayAccessExpr aae) {
            return new ArrayAccessExpression(idGen.nextId(), range(aae), buildExpression(aae.getName(), idGen), buildExpression(aae.getIndex(), idGen));
            
        } else if (e instanceof com.github.javaparser.ast.expr.ArrayCreationExpr ace) {
            TypeReferenceExpression typeRef = buildTypeReference(ace.getElementType(), idGen);
            List<ExpressionNode> dimensions = ace.getLevels().stream()
                .map(l -> l.getDimension().map(d -> buildExpression(d, idGen)).orElse(new LiteralExpression(idGen.nextId(), SourceRange.UNKNOWN, 0, LiteralKind.NUMBER)))
                .collect(Collectors.toList());
            ExpressionNode init = ace.getInitializer().map(i -> buildExpression(i, idGen)).orElse(null);
            return new ArrayCreationExpression(idGen.nextId(), range(ace), typeRef, dimensions, init);
            
        } else if (e instanceof com.github.javaparser.ast.expr.LambdaExpr le) {
            List<VariableDeclaration> params = new ArrayList<>();
            le.getParameters().forEach(p -> params.add(new VariableDeclaration(
                idGen.nextId(), range(p), p.getNameAsString(), Collections.emptySet(), Collections.emptyList(), buildTypeReference(p.getType(), idGen), null, VariableKind.PARAMETER
            )));
            StatementNode body = buildStatement(le.getBody(), idGen);
            return new LambdaExpression(idGen.nextId(), range(le), params, body);
            
        } else if (e instanceof com.github.javaparser.ast.expr.InstanceOfExpr io) {
            return new BinaryExpression(idGen.nextId(), range(io), buildExpression(io.getExpression(), idGen), "instanceof", buildTypeReference(io.getType(), idGen));
            
        } else if (e instanceof com.github.javaparser.ast.expr.SwitchExpr se) {
            List<ExpressionNode> args = new ArrayList<>();
            args.add(buildExpression(se.getSelector(), idGen));
            se.getEntries().forEach(entry -> {
                ExpressionNode pattern = entry.getLabels().isEmpty() ? null : buildExpression(entry.getLabels().get(0), idGen);
                entry.getStatements().forEach(st -> args.add(new LiteralExpression(idGen.nextId(), range(st), st.toString(), LiteralKind.STRING)));
            });
            return new CallExpression(idGen.nextId(), range(se), null, "switch", args, CallKind.UNKNOWN);
            
        } else if (e instanceof com.github.javaparser.ast.expr.MethodReferenceExpr mre) {
            ExpressionNode scope = buildExpression(mre.getScope(), idGen);
            String methodName = mre.getIdentifier();
            List<VariableDeclaration> params = List.of(new VariableDeclaration(
                idGen.nextId(), range(mre), "arg0", Collections.emptySet(), Collections.emptyList(), buildTypeReference("", idGen, range(mre)), null, VariableKind.PARAMETER
            ));
            ExpressionNode call = new CallExpression(
                idGen.nextId(), range(mre), scope, methodName, List.of(new VariableReferenceExpression(idGen.nextId(), range(mre), "arg0")), CallKind.INSTANCE
            );
            StatementNode body = new BlockStatement(idGen.nextId(), range(mre), List.of(new ExpressionStatement(idGen.nextId(), range(mre), call)));
            return new LambdaExpression(idGen.nextId(), range(mre), params, body);
            
        } else {
            return new TypeReferenceExpression(idGen.nextId(), range(e), QualifiedName.parse(e.toString()), Collections.emptyList(), 0, Variance.INVARIANT, Collections.emptyList());
        }
    }

    private TypeReferenceExpression buildTypeReference(com.github.javaparser.ast.type.Type type, NodeIdGenerator idGen) {
        if (type == null) return null;
        SourceRange sr = range(type);
        QualifiedName qn = QualifiedName.parse(type.asString());
        
        List<TypeReferenceExpression> typeArgs = new ArrayList<>();
        if (type instanceof com.github.javaparser.ast.type.ClassOrInterfaceType cit) {
            cit.getTypeArguments().ifPresent(args -> {
                args.forEach(arg -> typeArgs.add(buildTypeReference(arg, idGen)));
            });
        }
        
        int arrayRank = 0;
        if (type instanceof com.github.javaparser.ast.type.ArrayType at) {
            arrayRank = at.getArrayLevel();
        }
        
        Variance variance = Variance.INVARIANT;
        List<TypeReferenceExpression> bounds = new ArrayList<>();
        if (type instanceof com.github.javaparser.ast.type.WildcardType wt) {
            if (wt.getExtendedType().isPresent()) {
                variance = Variance.COVARIANT;
                bounds.add(buildTypeReference(wt.getExtendedType().get(), idGen));
            } else if (wt.getSuperType().isPresent()) {
                variance = Variance.CONTRAVARIANT;
                bounds.add(buildTypeReference(wt.getSuperType().get(), idGen));
            }
        }
        
        return new TypeReferenceExpression(idGen.nextId(), sr, qn, typeArgs, arrayRank, variance, bounds);
    }

    private TypeReferenceExpression buildTypeReference(String typeName, NodeIdGenerator idGen, SourceRange sr) {
        QualifiedName qn = QualifiedName.parse(typeName);
        return new TypeReferenceExpression(idGen.nextId(), sr, qn, Collections.emptyList(), 0, Variance.INVARIANT, Collections.emptyList());
    }

    private List<DecoratorNode> buildDecorators(List<AnnotationExpr> annotations, NodeIdGenerator idGen) {
        return annotations.stream().map(ann -> {
            List<ExpressionNode> args = new ArrayList<>();
            if (ann instanceof com.github.javaparser.ast.expr.NormalAnnotationExpr nae) {
                nae.getPairs().forEach(p -> args.add(buildExpression(p.getValue(), idGen)));
            } else if (ann instanceof com.github.javaparser.ast.expr.SingleMemberAnnotationExpr smae) {
                args.add(buildExpression(smae.getMemberValue(), idGen));
            }
            return new DecoratorNode(idGen.nextId(), range(ann), ann.getNameAsString(), args);
        }).collect(Collectors.toList());
    }

    private SourceRange range(com.github.javaparser.ast.Node node) {
        if (node.getBegin().isPresent() && node.getEnd().isPresent()) {
            return new SourceRange(
                node.getBegin().get().line,
                node.getBegin().get().column,
                node.getEnd().get().line,
                node.getEnd().get().column
            );
        }
        return SourceRange.UNKNOWN;
    }

    private String cleanJavaComment(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String[] lines = raw.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("/**")) trimmed = trimmed.substring(3);
            else if (trimmed.endsWith("*/")) trimmed = trimmed.substring(0, trimmed.length() - 2);
            else if (trimmed.startsWith("*")) trimmed = trimmed.substring(1);
            
            trimmed = trimmed.trim();
            if (!trimmed.isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(trimmed);
            }
        }
        return sb.toString();
    }
}
