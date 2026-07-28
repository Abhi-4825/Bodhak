package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import java.util.List;

/**
 * Represents collections literals (lists, sets, tuples, arrays).
 */
public record CollectionLiteralExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    List<ExpressionNode> elements,
    CollectionKind kind
) implements ExpressionNode {

    public enum CollectionKind {
        LIST, SET, TUPLE, ARRAY
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
