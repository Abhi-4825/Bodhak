package com.example.anuviya.ir.statement;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
import java.util.List;

/**
 * Represents a match statement (pattern matching, e.g. switch or match/case).
 */
public record MatchStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode subject,
    List<MatchCase> cases
) implements StatementNode {

    public record MatchCase(
        ExpressionNode pattern, // null indicates wildcard/default case
        StatementNode body
    ) {}

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
