package com.example.anuviya.ir.declaration;

import com.example.anuviya.ir.DeclarationNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
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
    String documentation,
    List<StatementNode> moduleStatements
) implements DeclarationNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
