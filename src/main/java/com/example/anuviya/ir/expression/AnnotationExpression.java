package com.example.anuviya.ir.expression;

import com.example.anuviya.compiler.symbol.QualifiedName;
import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import java.util.List;

/**
 * Represents an annotation/decorator expression with arguments.
 */
public record AnnotationExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    QualifiedName name,
    List<ExpressionNode> arguments
) implements ExpressionNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
