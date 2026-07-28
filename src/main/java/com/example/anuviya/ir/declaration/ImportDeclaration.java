package com.example.anuviya.ir.declaration;

import com.example.anuviya.ir.DeclarationNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

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
