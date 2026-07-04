package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
