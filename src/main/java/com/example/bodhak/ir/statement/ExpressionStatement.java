package com.example.bodhak.ir.statement;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;

/**
 * Represents a statement containing a single expression.
 */
public record ExpressionStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode expression
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
