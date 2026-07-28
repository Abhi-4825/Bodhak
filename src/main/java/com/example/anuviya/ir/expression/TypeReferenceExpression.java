package com.example.anuviya.ir.expression;

import com.example.anuviya.compiler.symbol.QualifiedName;
import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
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
