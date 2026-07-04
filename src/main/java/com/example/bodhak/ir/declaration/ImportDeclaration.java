package com.example.bodhak.ir.declaration;

import com.example.bodhak.ir.DeclarationNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

/**
 * Represents import statements (e.g. Java 'import java.util.List' or Python 'import os').
 */
public record ImportDeclaration(
    NodeId nodeId,
    SourceRange sourceRange,
    String path,
    String alias,
    boolean isStatic,
    boolean isWildcard
) implements DeclarationNode {

    public ImportDeclaration(NodeId nodeId, SourceRange sourceRange, String path, String alias, boolean isStatic) {
        this(nodeId, sourceRange, path, alias, isStatic, false);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
