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
    boolean isDefault,
    boolean isAsync,
    CallableScope scope,
    String documentation
) implements DeclarationNode, StatementNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
