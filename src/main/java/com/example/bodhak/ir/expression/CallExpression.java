package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
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
