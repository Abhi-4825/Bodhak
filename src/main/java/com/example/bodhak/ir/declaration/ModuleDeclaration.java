package com.example.bodhak.ir.declaration;

import com.example.bodhak.ir.DeclarationNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import java.util.List;

/**
 * Represents a module-level container, typically corresponding to a single source file.
 */
public record ModuleDeclaration(
    NodeId nodeId,
    SourceRange sourceRange,
    String name,
    String language,
    String filePath,
    List<ImportDeclaration> imports,
    List<DeclarationNode> declarations,
    String documentation
) implements DeclarationNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
