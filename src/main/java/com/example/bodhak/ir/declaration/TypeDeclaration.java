package com.example.bodhak.ir.declaration;

import com.example.bodhak.ir.DeclarationNode;
import com.example.bodhak.ir.DecoratorNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.expression.TypeReferenceExpression;
import com.example.bodhak.model.entity.ModifierKind;
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
    List<DeclarationNode> members
) implements DeclarationNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
