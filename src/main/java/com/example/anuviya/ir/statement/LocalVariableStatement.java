package com.example.anuviya.ir.statement;

import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
import com.example.anuviya.ir.declaration.VariableDeclaration;

/**
 * Represents local variable declaration statements within code blocks.
 */
public record LocalVariableStatement(
    NodeId nodeId,
    SourceRange sourceRange,
    VariableDeclaration declaration
) implements StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
