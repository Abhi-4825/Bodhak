package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents a type cast expression (e.g. (TargetType) expression).
 */
public record CastExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    TypeReferenceExpression targetType,
    ExpressionNode expression
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
