package com.example.bodhak.ir.expression;

import com.example.bodhak.compiler.symbol.QualifiedName;
import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import java.util.List;

/**
 * Represents a reference to a type name (e.g. inside variable declaration type or cast).
 */
public record TypeReferenceExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    QualifiedName qualifiedName,
    List<TypeReferenceExpression> typeArguments,
    int arrayRank,
    Variance variance,
    List<TypeReferenceExpression> bounds
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
