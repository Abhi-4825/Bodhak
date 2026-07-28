package com.example.anuviya.ir.statement;

import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
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
