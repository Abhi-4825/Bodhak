package com.example.anuviya.ir.statement;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
import java.util.List;

/**
 * Represents scoped resource management lifecycle (e.g. try-with-resources, with-statement, synchronized-block).
 */
public record ResourceStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    List<ExpressionNode> resources,
    StatementNode body
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
