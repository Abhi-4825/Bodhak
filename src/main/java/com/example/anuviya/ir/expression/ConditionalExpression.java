package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents a conditional ternary expression (condition ? thenExpr : elseExpr).
 */
public record ConditionalExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode condition,
    ExpressionNode thenExpr,
    ExpressionNode elseExpr
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
