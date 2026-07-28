package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents a field/member access expression (e.g. receiver.fieldName).
 */
public record FieldAccessExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode receiver,
    String fieldName
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
