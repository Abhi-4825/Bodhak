package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
