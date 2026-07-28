package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import java.util.List;

/**
 * Represents method or function calls.
 */
public record CallExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode receiver,
    String callableName,
    List<ExpressionNode> arguments,
    CallKind kind
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
