package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

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
