package com.example.bodhak.ir.statement;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;

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
