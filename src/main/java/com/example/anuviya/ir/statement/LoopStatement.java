package com.example.anuviya.ir.statement;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;

/**
 * Represents iteration loops (e.g. for, while, do-while loops).
 */
public record LoopStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode condition,
    StatementNode body
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
