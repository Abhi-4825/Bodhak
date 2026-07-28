package com.example.anuviya.ir;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates sequential NodeIds per compilation unit to support incremental compilation.
 */
public class NodeIdGenerator {
    private final String compilationUnitId;
    private final AtomicLong counter = new AtomicLong(0);

    public NodeIdGenerator(String compilationUnitId) {
        this.compilationUnitId = compilationUnitId;
    }

    public NodeId nextId() {
        return new NodeId(compilationUnitId, counter.incrementAndGet());
    }
}
