package com.example.bodhak.ir.expression;

import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
import java.util.List;

/**
 * Represents instantiation of objects/types (e.g. 'new Foo()').
 */
public record ObjectCreationExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    TypeReferenceExpression type,
    List<TypeReferenceExpression> genericArguments,
    List<ExpressionNode> arguments,
    boolean isAnonymous
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
