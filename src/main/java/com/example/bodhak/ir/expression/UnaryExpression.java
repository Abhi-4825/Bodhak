package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
