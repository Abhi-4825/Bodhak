package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents reference to variables or fields (e.g. usage of 'x' or 'this.x').
 */
public record VariableReferenceExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    String variableName
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
