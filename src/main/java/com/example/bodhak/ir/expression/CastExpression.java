package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
