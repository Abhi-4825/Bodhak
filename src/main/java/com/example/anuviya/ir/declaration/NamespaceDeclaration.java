package com.example.anuviya.ir.declaration;

import com.example.anuviya.ir.DeclarationNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;

/**
 * Represents packages, namespaces, or module scopes (e.g. Java's 'package com.example').
 */
public record NamespaceDeclaration(
    NodeId nodeId,
    SourceRange sourceRange,
    String name
) implements DeclarationNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
