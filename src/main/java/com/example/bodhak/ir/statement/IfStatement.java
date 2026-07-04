package com.example.bodhak.ir.statement;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;

/**
 * Represents conditional branches (e.g. if-else statements).
 */
public record IfStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode condition,
    StatementNode thenBranch,
    StatementNode elseBranch
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
