package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

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
