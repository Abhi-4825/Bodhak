package com.example.anuviya.ir.statement;

import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
import java.util.List;

/**
 * Represents a sequence/block of statements.
 */
public record BlockStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    List<StatementNode> statements
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
