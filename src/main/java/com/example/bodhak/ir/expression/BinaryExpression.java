package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

/**
 * Represents a binary operation expression (e.g. left + right).
 */
public record BinaryExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode left,
    String operator,
    ExpressionNode right
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
