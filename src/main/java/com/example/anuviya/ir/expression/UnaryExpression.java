package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents a unary operation expression (e.g. ++i or !flag).
 */
public record UnaryExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    String operator,
    ExpressionNode expression,
    boolean isPostfix
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
