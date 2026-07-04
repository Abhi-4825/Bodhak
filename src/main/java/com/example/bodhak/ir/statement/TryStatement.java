package com.example.bodhak.ir.statement;

import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;
import java.util.List;

/**
 * Represents exception handling blocks (try-catch-finally).
 */
public record TryStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    StatementNode tryBlock,
    List<StatementNode> catchBlocks,
    StatementNode finallyBlock
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
