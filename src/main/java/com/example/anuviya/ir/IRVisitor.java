package com.example.anuviya.ir;

import com.example.anuviya.ir.declaration.*;
import com.example.anuviya.ir.statement.*;
import com.example.anuviya.ir.expression.*;

/**
 * Visitor interface for walking the IR tree with default recursive traversals.
 */
public interface IRVisitor {
    default void visit(NamespaceDeclaration node) {}
    
    default void visit(ModuleDeclaration node) {
        node.declarations().forEach(d -> d.accept(this));
        node.imports().forEach(imp -> imp.accept(this));
        if (node.moduleStatements() != null) {
            node.moduleStatements().forEach(s -> s.accept(this));
        }
    }
    
    default void visit(ImportDeclaration node) {}
    
    default void visit(TypeDeclaration node) {
        node.decorators().forEach(dec -> dec.accept(this));
        node.extendsTypes().forEach(et -> et.accept(this));
        node.implementsTypes().forEach(it -> it.accept(this));
        node.permitsTypes().forEach(pt -> pt.accept(this));
        node.members().forEach(m -> m.accept(this));
    }
    
    default void visit(CallableDeclaration node) {
        node.decorators().forEach(dec -> dec.accept(this));
        node.parameters().forEach(p -> p.accept(this));
        node.throwsTypes().forEach(tt -> tt.accept(this));
        if (node.returnType() != null) {
            node.returnType().accept(this);
        }
        if (node.body() != null) {
            node.body().accept(this);
        }
    }
    
    default void visit(VariableDeclaration node) {
        node.decorators().forEach(dec -> dec.accept(this));
        if (node.type() != null) {
            node.type().accept(this);
        }
        if (node.initializer() != null) {
            node.initializer().accept(this);
        }
    }
    
    default void visit(DecoratorNode node) {
        node.arguments().forEach(a -> a.accept(this));
    }
    
    default void visit(BlockStatement node) {
        node.statements().forEach(s -> s.accept(this));
    }
    
    default void visit(ExpressionStatement node) {
        if (node.expression() != null) {
            node.expression().accept(this);
        }
    }
    
    default void visit(IfStatement node) {
        if (node.condition() != null) {
            node.condition().accept(this);
        }
        if (node.thenBranch() != null) {
            node.thenBranch().accept(this);
        }
        if (node.elseBranch() != null) {
            node.elseBranch().accept(this);
        }
    }
    
    default void visit(LoopStatement node) {
        if (node.condition() != null) {
            node.condition().accept(this);
        }
        if (node.body() != null) {
            node.body().accept(this);
        }
    }
    
    default void visit(TryStatement node) {
        if (node.tryBlock() != null) {
            node.tryBlock().accept(this);
        }
        node.catchBlocks().forEach(c -> c.accept(this));
        if (node.finallyBlock() != null) {
            node.finallyBlock().accept(this);
        }
    }
    
    default void visit(ReturnStatement node) {
        if (node.expression() != null) {
            node.expression().accept(this);
        }
    }
    
    default void visit(LocalVariableStatement node) {
        if (node.declaration() != null) {
            node.declaration().accept(this);
        }
    }
    
    default void visit(CallExpression node) {
        if (node.receiver() != null) {
            node.receiver().accept(this);
        }
        node.arguments().forEach(a -> a.accept(this));
    }
    
    default void visit(ObjectCreationExpression node) {
        if (node.type() != null) {
            node.type().accept(this);
        }
        node.genericArguments().forEach(ga -> ga.accept(this));
        node.arguments().forEach(a -> a.accept(this));
    }
    
    default void visit(TypeReferenceExpression node) {
        node.typeArguments().forEach(ta -> ta.accept(this));
        node.bounds().forEach(b -> b.accept(this));
    }
    
    default void visit(VariableReferenceExpression node) {}

    default void visit(ThisExpression node) {}

    default void visit(SuperExpression node) {}

    default void visit(ClassLiteralExpression node) {
        if (node.type() != null) {
            node.type().accept(this);
        }
    }

    default void visit(AnnotationExpression node) {
        node.arguments().forEach(a -> a.accept(this));
    }

    default void visit(FieldAccessExpression node) {
        if (node.receiver() != null) {
            node.receiver().accept(this);
        }
    }

    default void visit(AssignmentExpression node) {
        if (node.target() != null) {
            node.target().accept(this);
        }
        if (node.value() != null) {
            node.value().accept(this);
        }
    }

    default void visit(LiteralExpression node) {}

    default void visit(LambdaExpression node) {
        node.parameters().forEach(p -> p.accept(this));
        if (node.body() != null) {
            node.body().accept(this);
        }
    }

    default void visit(BinaryExpression node) {
        if (node.left() != null) {
            node.left().accept(this);
        }
        if (node.right() != null) {
            node.right().accept(this);
        }
    }

    default void visit(UnaryExpression node) {
        if (node.expression() != null) {
            node.expression().accept(this);
        }
    }

    default void visit(ConditionalExpression node) {
        if (node.condition() != null) {
            node.condition().accept(this);
        }
        if (node.thenExpr() != null) {
            node.thenExpr().accept(this);
        }
        if (node.elseExpr() != null) {
            node.elseExpr().accept(this);
        }
    }

    default void visit(CastExpression node) {
        if (node.targetType() != null) {
            node.targetType().accept(this);
        }
        if (node.expression() != null) {
            node.expression().accept(this);
        }
    }

    default void visit(ArrayAccessExpression node) {
        if (node.array() != null) {
            node.array().accept(this);
        }
        if (node.index() != null) {
            node.index().accept(this);
        }
    }

    default void visit(ArrayCreationExpression node) {
        if (node.type() != null) {
            node.type().accept(this);
        }
        node.dimensions().forEach(d -> d.accept(this));
        if (node.initializer() != null) {
            node.initializer().accept(this);
        }
    }

    default void visit(ThrowStatement node) {
        if (node.expression() != null) {
            node.expression().accept(this);
        }
    }

    default void visit(MatchStatement node) {
        if (node.subject() != null) {
            node.subject().accept(this);
        }
        if (node.cases() != null) {
            for (MatchStatement.MatchCase c : node.cases()) {
                if (c.pattern() != null) {
                    c.pattern().accept(this);
                }
                if (c.body() != null) {
                    c.body().accept(this);
                }
            }
        }
    }

    default void visit(ResourceStatement node) {
        if (node.resources() != null) {
            node.resources().forEach(r -> r.accept(this));
        }
        if (node.body() != null) {
            node.body().accept(this);
        }
    }

    default void visit(AwaitExpression node) {
        if (node.expression() != null) {
            node.expression().accept(this);
        }
    }

    default void visit(YieldExpression node) {
        if (node.expression() != null) {
            node.expression().accept(this);
        }
    }

    default void visit(ComprehensionExpression node) {
        if (node.element() != null) {
            node.element().accept(this);
        }
        if (node.key() != null) {
            node.key().accept(this);
        }
        if (node.generators() != null) {
            for (ComprehensionExpression.Generator gen : node.generators()) {
                if (gen.variable() != null) {
                    gen.variable().accept(this);
                }
                if (gen.iterable() != null) {
                    gen.iterable().accept(this);
                }
                if (gen.conditions() != null) {
                    gen.conditions().forEach(c -> c.accept(this));
                }
            }
        }
    }

    default void visit(CollectionLiteralExpression node) {
        if (node.elements() != null) {
            node.elements().forEach(e -> e.accept(this));
        }
    }

    default void visit(DictionaryExpression node) {
        if (node.entries() != null) {
            for (DictionaryExpression.Entry entry : node.entries()) {
                if (entry.key() != null) {
                    entry.key().accept(this);
                }
                if (entry.value() != null) {
                    entry.value().accept(this);
                }
            }
        }
    }
}
