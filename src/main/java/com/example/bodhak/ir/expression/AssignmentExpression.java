package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
