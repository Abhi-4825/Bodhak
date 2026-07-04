package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
