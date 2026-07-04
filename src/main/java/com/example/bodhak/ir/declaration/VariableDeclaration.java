package com.example.bodhak.ir.declaration;

import com.example.bodhak.ir.DeclarationNode;
import com.example.bodhak.ir.DecoratorNode;
import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.expression.TypeReferenceExpression;
import com.example.bodhak.model.entity.ModifierKind;
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
