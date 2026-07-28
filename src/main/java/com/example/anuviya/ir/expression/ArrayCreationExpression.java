package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import java.util.List;

/**
 * Represents an array initialization/creation expression (e.g. new int[10]).
 */
public record ArrayCreationExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    TypeReferenceExpression type,
    List<ExpressionNode> dimensions,
    ExpressionNode initializer
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
