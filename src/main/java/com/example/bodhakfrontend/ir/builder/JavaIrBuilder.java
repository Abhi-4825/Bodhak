package com.example.bodhakfrontend.ir.builder;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.plugin.Parser;
import com.example.bodhakfrontend.ir.model.*;
import com.example.bodhakfrontend.ir.spi.IrBuilder;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;

import java.nio.file.Path;
import java.util.*;

/**
 * Converts Java source files (via JavaParser) into the language-neutral BIR.
 *
 * <p><b>Mapping table:</b></p>
 * <pre>
 *  Java AST node                 →  BIR operation
 *  ──────────────────────────────────────────────────────
 *  ForStmt / ForEachStmt         →  IrLoop
 *  WhileStmt / DoStmt            →  IrLoop
 *  IfStmt / SwitchStmt           →  IrConditional
 *  MethodCallExpr                →  IrCall  (classified by heuristic)
 *  ObjectCreationExpr            →  IrAllocation
 *  ReturnStmt                    →  IrReturn
 *  add/remove/contains/…         →  IrCollectionOperation  (via MethodCallExpr name)
 * </pre>
 *
 * <p>This class is stateless and thread-safe. Each call to {@link #build} is
 * independent.</p>
 */
public class JavaIrBuilder implements IrBuilder {

    // ── Call-type classification sets ─────────────────────────────────────────

    /** Known JPA / ORM / JDBC type name fragments. */
    private static final Set<String> DB_TYPES = Set.of(
            "JpaRepository", "CrudRepository", "MongoRepository", "PagingAndSortingRepository",
            "Repository", "EntityManager", "JdbcTemplate", "NamedParameterJdbcTemplate",
            "HibernateTemplate", "Session", "Query", "TypedQuery"
    );

    /** Known HTTP / messaging client type name fragments. */
    private static final Set<String> NETWORK_TYPES = Set.of(
            "RestTemplate", "HttpClient", "WebClient", "OkHttpClient",
            "Feign", "FeignClient", "HttpURLConnection", "URLConnection",
            "AsyncHttpClient", "CloseableHttpClient", "HttpGet", "HttpPost",
            "KafkaTemplate", "RabbitTemplate", "SqsClient", "SnsClient"
    );

    /** Known file-system type name fragments. */
    private static final Set<String> FILE_IO_TYPES = Set.of(
            "Files", "File", "FileInputStream", "FileOutputStream",
            "BufferedReader", "BufferedWriter", "FileReader", "FileWriter",
            "RandomAccessFile", "PrintWriter", "Scanner", "Path", "Paths",
            "FileChannel", "DataInputStream", "DataOutputStream"
    );

    /** Known cache type name fragments. */
    private static final Set<String> CACHE_TYPES = Set.of(
            "Cache", "Caffeine", "CaffeineSpec", "LoadingCache",
            "RedisTemplate", "StringRedisTemplate", "Jedis", "Lettuce",
            "MemcachedClient", "EhCache"
    );

    /** Known collection method names that map to {@link CollectionOperationType}. */
    private static final Map<String, CollectionOperationType> COLLECTION_OPS;
    static {
        Map<String, CollectionOperationType> m = new HashMap<>();
        m.put("add",        CollectionOperationType.ADD);
        m.put("addAll",     CollectionOperationType.ADD);
        m.put("put",        CollectionOperationType.ADD);
        m.put("putAll",     CollectionOperationType.ADD);
        m.put("offer",      CollectionOperationType.ADD);
        m.put("push",       CollectionOperationType.ADD);
        m.put("remove",     CollectionOperationType.REMOVE);
        m.put("removeAll",  CollectionOperationType.REMOVE);
        m.put("poll",       CollectionOperationType.REMOVE);
        m.put("pop",        CollectionOperationType.REMOVE);
        m.put("delete",     CollectionOperationType.REMOVE);
        m.put("get",        CollectionOperationType.LOOKUP);
        m.put("getOrDefault", CollectionOperationType.LOOKUP);
        m.put("peek",       CollectionOperationType.LOOKUP);
        m.put("find",       CollectionOperationType.LOOKUP);
        m.put("contains",   CollectionOperationType.CONTAINS);
        m.put("containsKey",CollectionOperationType.CONTAINS);
        m.put("containsValue", CollectionOperationType.CONTAINS);
        m.put("includes",   CollectionOperationType.CONTAINS);
        m.put("forEach",    CollectionOperationType.ITERATE);
        m.put("stream",     CollectionOperationType.ITERATE);
        m.put("iterator",   CollectionOperationType.ITERATE);
        COLLECTION_OPS = Collections.unmodifiableMap(m);
    }

    // ── Dependencies ──────────────────────────────────────────────────────────

    private final Parser<CompilationUnit> parser;

    public JavaIrBuilder(Parser<CompilationUnit> parser) {
        this.parser = parser;
    }

    // ── IrBuilder impl ────────────────────────────────────────────────────────

    @Override
    public IrProject build(AnalysisContext context) {
        List<IrEntity> entities = new ArrayList<>();

        for (var entity : context.getEntities()) {
            if (!"java".equals(entity.getLanguage())) continue;

            Path sourceFile = entity.getSourceFile().toPath();
            CompilationUnit cu = parser.parse(sourceFile);
            if (cu == null) continue;

            String entityName = entity.getEntityName();

            // Find the matching type declaration by FQN
            cu.findAll(TypeDeclaration.class).forEach(typeDecl -> {
                // Build IR entity for each top-level and nested class
                String fqn = resolveFqn(cu, typeDecl);
                List<IrMethod> irMethods = buildMethods(typeDecl);
                if (!irMethods.isEmpty()) {
                    entities.add(new IrEntity(fqn, irMethods));
                }
            });
        }

        return new IrProject(Collections.unmodifiableList(entities));
    }

    // ── Method-level IR ───────────────────────────────────────────────────────

    private List<IrMethod> buildMethods(TypeDeclaration<?> typeDecl) {
        List<IrMethod> methods = new ArrayList<>();

        typeDecl.getMethods().forEach(method -> {
            List<IrOperation> ops = buildOps(method.getBody().map(b -> (Iterable<Statement>) b.getStatements()::iterator).orElse(Collections.emptyList()), 0);
            methods.add(new IrMethod(method.getNameAsString(), Collections.unmodifiableList(ops)));
        });

        typeDecl.getConstructors().forEach(ctor -> {
            List<IrOperation> ops = buildOps(ctor.getBody().getStatements()::iterator, 0);
            methods.add(new IrMethod("<init>", Collections.unmodifiableList(ops)));
        });

        return methods;
    }

    // ── Statement → IrOperation ───────────────────────────────────────────────

    private List<IrOperation> buildOps(Iterable<Statement> statements, int loopDepth) {
        List<IrOperation> ops = new ArrayList<>();
        for (Statement stmt : statements) {
            ops.addAll(visitStatement(stmt, loopDepth));
        }
        return ops;
    }

    private List<IrOperation> visitStatement(Statement stmt, int loopDepth) {
        List<IrOperation> ops = new ArrayList<>();

        if (stmt instanceof ForStmt forStmt) {
            List<IrOperation> body = forStmt.getBody() instanceof BlockStmt bs
                    ? buildOps(bs.getStatements()::iterator, loopDepth + 1)
                    : visitStatement(forStmt.getBody(), loopDepth + 1);
            // Also inspect the update/init for method calls
            ops.add(new IrLoop(loopDepth + 1, Collections.unmodifiableList(body)));

        } else if (stmt instanceof ForEachStmt forEachStmt) {
            List<IrOperation> body = forEachStmt.getBody() instanceof BlockStmt bs
                    ? buildOps(bs.getStatements()::iterator, loopDepth + 1)
                    : visitStatement(forEachStmt.getBody(), loopDepth + 1);
            ops.add(new IrLoop(loopDepth + 1, Collections.unmodifiableList(body)));

        } else if (stmt instanceof WhileStmt whileStmt) {
            List<IrOperation> body = whileStmt.getBody() instanceof BlockStmt bs
                    ? buildOps(bs.getStatements()::iterator, loopDepth + 1)
                    : visitStatement(whileStmt.getBody(), loopDepth + 1);
            ops.add(new IrLoop(loopDepth + 1, Collections.unmodifiableList(body)));

        } else if (stmt instanceof DoStmt doStmt) {
            List<IrOperation> body = doStmt.getBody() instanceof BlockStmt bs
                    ? buildOps(bs.getStatements()::iterator, loopDepth + 1)
                    : visitStatement(doStmt.getBody(), loopDepth + 1);
            ops.add(new IrLoop(loopDepth + 1, Collections.unmodifiableList(body)));

        } else if (stmt instanceof IfStmt ifStmt) {
            List<IrOperation> thenOps = ifStmt.getThenStmt() instanceof BlockStmt bs
                    ? buildOps(bs.getStatements()::iterator, loopDepth)
                    : visitStatement(ifStmt.getThenStmt(), loopDepth);
            // Merge else branch into the same IrConditional body
            ifStmt.getElseStmt().ifPresent(elseStmt -> thenOps.addAll(visitStatement(elseStmt, loopDepth)));
            ops.add(new IrConditional(Collections.unmodifiableList(thenOps)));

        } else if (stmt instanceof SwitchStmt switchStmt) {
            List<IrOperation> body = new ArrayList<>();
            for (SwitchEntry entry : switchStmt.getEntries()) {
                body.addAll(buildOps(entry.getStatements()::iterator, loopDepth));
            }
            ops.add(new IrConditional(Collections.unmodifiableList(body)));

        } else if (stmt instanceof ReturnStmt) {
            ops.add(new IrReturn());

        } else if (stmt instanceof ExpressionStmt exprStmt) {
            ops.addAll(visitExpression(exprStmt.getExpression(), loopDepth));

        } else if (stmt instanceof BlockStmt blockStmt) {
            ops.addAll(buildOps(blockStmt.getStatements()::iterator, loopDepth));

        } else if (stmt instanceof TryStmt tryStmt) {
            // Flatten try/catch/finally into the same level for IR purposes
            ops.addAll(buildOps(tryStmt.getTryBlock().getStatements()::iterator, loopDepth));
            tryStmt.getCatchClauses().forEach(c ->
                    ops.addAll(buildOps(c.getBody().getStatements()::iterator, loopDepth)));
            tryStmt.getFinallyBlock().ifPresent(fb ->
                    ops.addAll(buildOps(fb.getStatements()::iterator, loopDepth)));
        }
        // ThrowStmt, BreakStmt, ContinueStmt etc. are intentionally ignored;
        // they are control-flow hints, not semantic operations.

        return ops;
    }

    // ── Expression → IrOperation ──────────────────────────────────────────────

    private List<IrOperation> visitExpression(Expression expr, int loopDepth) {
        List<IrOperation> ops = new ArrayList<>();

        if (expr instanceof MethodCallExpr callExpr) {
            String methodName = callExpr.getNameAsString();
            String scope = callExpr.getScope().map(Object::toString).orElse("");

            // 1. Check collection operations first (more specific)
            CollectionOperationType colOp = COLLECTION_OPS.get(methodName);
            if (colOp != null) {
                ops.add(new IrCollectionOperation(colOp));
            } else {
                // 2. Classify by scope type name
                CallType callType = classifyCall(scope, methodName);
                String target = scope.isEmpty() ? methodName : scope + "." + methodName;
                ops.add(new IrCall(target, callType));
            }

            // 3. Recurse into lambda arguments (e.g. forEach lambdas)
            callExpr.getArguments().forEach(arg -> {
                if (arg instanceof LambdaExpr lambda) {
                    if (lambda.getBody() instanceof BlockStmt bs) {
                        ops.addAll(buildOps(bs.getStatements()::iterator, loopDepth));
                    } else if (lambda.getBody() instanceof ExpressionStmt es) {
                        ops.addAll(visitExpression(es.getExpression(), loopDepth));
                    }
                }
            });

        } else if (expr instanceof ObjectCreationExpr newExpr) {
            ops.add(new IrAllocation(newExpr.getTypeAsString()));

        } else if (expr instanceof AssignExpr assignExpr) {
            // Visit the value side of an assignment for embedded calls/allocations
            ops.addAll(visitExpression(assignExpr.getValue(), loopDepth));

        } else if (expr instanceof VariableDeclarationExpr varDecl) {
            varDecl.getVariables().forEach(v ->
                    v.getInitializer().ifPresent(init -> ops.addAll(visitExpression(init, loopDepth))));

        } else if (expr instanceof ConditionalExpr ternary) {
            // Ternary: treat both branches as conditional body
            List<IrOperation> body = new ArrayList<>();
            body.addAll(visitExpression(ternary.getThenExpr(), loopDepth));
            body.addAll(visitExpression(ternary.getElseExpr(), loopDepth));
            if (!body.isEmpty()) {
                ops.add(new IrConditional(Collections.unmodifiableList(body)));
            }
        }

        return ops;
    }

    // ── Heuristic call classifier ─────────────────────────────────────────────

    /**
     * Classifies a method call into a {@link CallType} using heuristic type-name matching.
     *
     * <p>Matching is intentionally loose: it checks whether the scope string
     * <em>contains</em> (case-insensitive) any known fragment. This covers common
     * naming patterns like {@code userRepo.findAll()}, {@code restTemplate.getForObject()},
     * {@code Files.readAllLines()}, etc.</p>
     */
    private CallType classifyCall(String scope, String methodName) {
        if (scope.isEmpty()) return CallType.LOCAL;

        String scopeLower = scope.toLowerCase();

        for (String dbType : DB_TYPES) {
            if (scopeLower.contains(dbType.toLowerCase())) return CallType.DATABASE;
        }
        for (String netType : NETWORK_TYPES) {
            if (scopeLower.contains(netType.toLowerCase())) return CallType.NETWORK;
        }
        for (String fileType : FILE_IO_TYPES) {
            if (scopeLower.contains(fileType.toLowerCase())) return CallType.FILE_IO;
        }
        for (String cacheType : CACHE_TYPES) {
            if (scopeLower.contains(cacheType.toLowerCase())) return CallType.CACHE;
        }

        return CallType.LOCAL;
    }

    // ── FQN resolution ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String resolveFqn(CompilationUnit cu, TypeDeclaration<?> typeDecl) {
        String pkg = cu.getPackageDeclaration()
                .map(p -> p.getNameAsString() + ".")
                .orElse("");
        return pkg + typeDecl.getNameAsString();
    }
}
