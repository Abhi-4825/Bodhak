package com.example.anuviya.model.entity;

import com.example.anuviya.ir.declaration.CallableDeclaration;
import com.example.anuviya.ir.declaration.VariableDeclaration;
import com.example.anuviya.ir.declaration.TypeDeclaration;
import java.util.List;
import java.util.Set;

/**
 * Structural members (fields, callables, inner types) associated with an entity.
 */
public record Structure(
    Set<String> fields,
    List<String> memberNames,
    List<VariableDeclaration> fieldDeclarations,
    List<CallableDeclaration> callableDeclarations,
    List<TypeDeclaration> nestedTypes
) {}
