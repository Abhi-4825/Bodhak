package com.example.bodhak.ir.statement;

import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;
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
