package com.example.bodhak.ir.statement;

import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;
import com.example.bodhak.ir.declaration.VariableDeclaration;

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
