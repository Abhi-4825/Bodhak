package com.example.bodhak.ir.expression;

import com.example.bodhak.compiler.symbol.QualifiedName;
import com.example.bodhak.ir.ExpressionNode;
import com.example.bodhak.ir.IRVisitor;
import com.example.bodhak.ir.NodeId;
import com.example.bodhak.ir.SourceRange;
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
