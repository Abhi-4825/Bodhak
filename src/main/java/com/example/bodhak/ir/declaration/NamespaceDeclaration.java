package com.example.bodhak.ir.declaration;

import com.example.bodhak.ir.DeclarationNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;

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
