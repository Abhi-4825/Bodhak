package com.example.anuviya.model.entity;

import com.example.anuviya.ir.DecoratorNode;
import java.util.List;
import java.util.Set;

/**
 * Comments, docstrings, and decorator meta annotations.
 */
public record Documentation(
    String docstring,
    List<String> comments,
    Set<String> decorators,
    List<DecoratorNode> decoratorNodes
) {}
