package com.example.anuviya.ir.declaration;

import com.example.anuviya.ir.DeclarationNode;
import com.example.anuviya.ir.DecoratorNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.StatementNode;
import com.example.anuviya.ir.expression.TypeReferenceExpression;
import com.example.anuviya.model.entity.ModifierKind;
import java.util.List;
import java.util.Set;

/**
 * Represents type declarations (e.g. classes, interfaces, enums, structs, records).
 */
public record TypeDeclaration(
    NodeId nodeId,
    SourceRange sourceRange,
    String name,
    TypeKind typeKind,
    Set<ModifierKind> modifiers,
    List<DecoratorNode> decorators,
    List<TypeReferenceExpression> extendsTypes,
    List<TypeReferenceExpression> implementsTypes,
    List<TypeReferenceExpression> permitsTypes,
    List<DeclarationNode> members,
    String documentation
) implements DeclarationNode, StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
