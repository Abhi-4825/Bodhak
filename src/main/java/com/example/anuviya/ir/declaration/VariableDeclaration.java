package com.example.anuviya.ir.declaration;

import com.example.anuviya.ir.DeclarationNode;
import com.example.anuviya.ir.DecoratorNode;
import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.ir.expression.TypeReferenceExpression;
import com.example.anuviya.model.entity.ModifierKind;
import java.util.List;
import java.util.Set;

/**
 * Represents variables, fields, parameters, and constants.
 */
public record VariableDeclaration(
    NodeId nodeId,
    SourceRange sourceRange,
    String name,
    Set<ModifierKind> modifiers,
    List<DecoratorNode> decorators,
    TypeReferenceExpression type,
    ExpressionNode initializer,
    VariableKind kind
) implements DeclarationNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
