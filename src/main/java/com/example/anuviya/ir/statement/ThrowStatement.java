package com.example.anuviya.ir.statement;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;

/**
 * Represents throwing/raising an exception.
 */
public record ThrowStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode expression
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
