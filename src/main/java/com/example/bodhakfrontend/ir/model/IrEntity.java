package com.example.bodhakfrontend.ir.model;

import java.util.List;

/**
 * IR representation of a single code entity (class, module, struct, etc.).
 *
 * <p>{@code entityName} is the fully-qualified name as produced by the
 * originating language plugin, e.g. {@code com.example.OrderService}
 * for Java or {@code orders.service.OrderService} for Python.</p>
 */
public record IrEntity(String entityName, List<IrMethod> methods) {}
