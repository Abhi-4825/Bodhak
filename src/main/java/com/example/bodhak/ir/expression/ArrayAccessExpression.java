package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

/**
 * Represents an array index access expression (e.g. array[index]).
 */
public record ArrayAccessExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode array,
    ExpressionNode index
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
