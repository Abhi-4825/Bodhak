package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents a class literal expression (e.g. User.class or type(User)).
 */
public record ClassLiteralExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    TypeReferenceExpression type
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
