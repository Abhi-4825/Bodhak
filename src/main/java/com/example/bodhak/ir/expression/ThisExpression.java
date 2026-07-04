package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

/**
 * Represents a reference to the current object instance ('this').
 */
public record ThisExpression(
    NodeId nodeId,
    SourceRange sourceRange
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
