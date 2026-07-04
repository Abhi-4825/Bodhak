package com.example.bodhak.ir;

/**
 * Base interface for all Intermediate Representation (IR) nodes.
 */
public interface IRNode {
    NodeId nodeId();
    SourceRange sourceRange();
    void accept(IRVisitor visitor);
}
