package com.example.anuviya.ir;

import java.util.List;

/**
 * Represents annotations or decorators attached to declarations (e.g. Java's @Override, Python's @dataclass).
 */
public record DecoratorNode(
    NodeId nodeId,
    SourceRange sourceRange,
    String name,
    List<ExpressionNode> arguments
) implements IRNode {
    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
