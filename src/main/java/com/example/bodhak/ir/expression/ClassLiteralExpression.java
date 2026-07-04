package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
