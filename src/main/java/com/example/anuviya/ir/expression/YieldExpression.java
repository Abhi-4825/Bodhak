package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents yielding a value from a generator function (e.g. yield, yield from).
 */
public record YieldExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode expression,
    boolean isYieldFrom
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
