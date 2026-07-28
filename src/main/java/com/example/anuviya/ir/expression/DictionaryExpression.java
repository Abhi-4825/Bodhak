package com.example.anuviya.ir.expression;

import com.example.anuviya.ir.ExpressionNode;
import com.example.anuviya.ir.IRVisitor;
import com.example.anuviya.ir.NodeId;
import com.example.anuviya.ir.SourceRange;
import java.util.List;

/**
 * Represents map or dictionary literal structures (key-value pairs).
 */
public record DictionaryExpression(
    NodeId nodeId,
    SourceRange sourceRange,
    List<Entry> entries
) implements ExpressionNode {

    public record Entry(ExpressionNode key, ExpressionNode value) {}

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
