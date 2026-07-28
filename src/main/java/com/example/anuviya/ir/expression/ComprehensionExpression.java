package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import java.util.List;

/**
 * Represents comprehensions (list, set, dict comprehensions or generator expressions).
 */
public record ComprehensionExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    ExpressionNode element,
    ExpressionNode key, // Non-null for dict comprehensions
    List<Generator> generators,
    ComprehensionKind kind
) implements ExpressionNode {

    public record Generator(
        ExpressionNode variable,
        ExpressionNode iterable,
        List<ExpressionNode> conditions
    ) {}

    public enum ComprehensionKind {
        LIST, SET, DICT, GENERATOR
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
