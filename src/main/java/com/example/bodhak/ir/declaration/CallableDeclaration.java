package com.example.bodhak.ir.declaration;

import com.example.bodhak.ir.DeclarationNode;
import com.example.bodhak.ir.DecoratorNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.ir.StatementNode;
import com.example.bodhak.ir.expression.TypeReferenceExpression;
import com.example.bodhak.model.entity.ModifierKind;
import java.util.List;
import java.util.Set;

/**
 * Represents executable definitions (e.g. methods, constructors, functions).
 */
public record CallableDeclaration(
    NodeId nodeId,
    SourceRange sourceRange,
    String name,
    Set<ModifierKind> modifiers,
    List<DecoratorNode> decorators,
    List<VariableDeclaration> parameters,
    TypeReferenceExpression returnType,
    List<TypeReferenceExpression> throwsTypes,
    List<String> genericParameters,
    StatementNode body,
    boolean isConstructor,
    boolean isAbstract,
    boolean isDefault
) implements DeclarationNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
