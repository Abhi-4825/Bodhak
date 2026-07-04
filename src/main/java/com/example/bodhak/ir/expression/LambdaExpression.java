package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;
import com.example.bodhak.ir.declaration.VariableDeclaration;
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
