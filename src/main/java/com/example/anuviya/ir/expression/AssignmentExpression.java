package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents an assignment expression (target = value).
 */
public record AssignmentExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode target,
    ExpressionNode value
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
