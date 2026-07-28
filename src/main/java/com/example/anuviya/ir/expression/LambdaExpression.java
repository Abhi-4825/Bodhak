package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
import com.example.anuviya.ir.declaration.VariableDeclaration;
import java.util.List;

/**
 * Represents a lambda / anonymous function expression.
 */
public record LambdaExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    List<VariableDeclaration> parameters,
    StatementNode body
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
