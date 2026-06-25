package com.example.bodhakfrontend.ir.spi;

import com.example.bodhakfrontend.ir.spi.IrBuilder;

/**
 * Optional mixin interface for {@link com.example.bodhakfrontend.core.plugin.LanguagePlugin}
 * implementations that support BIR generation.
 *
 * <p>Keeping this separate from {@code LanguagePlugin} means existing plugins
 * (Python, future languages) are not forced to implement IR generation before
 * they are ready. Callers use {@code instanceof LanguageIrProvider} to check
 * support at runtime.</p>
 *
 * <p>Usage:
 * <pre>{@code
 * LanguagePlugin plugin = registry.forId("java").orElseThrow();
 * if (plugin instanceof LanguageIrProvider irProvider) {
 *     IrProject ir = irProvider.getIrBuilder().build(context);
 * }
 * }</pre>
 * </p>
 */
public interface LanguageIrProvider {
    /**
     * Returns the {@link IrBuilder} for this language.
     * Never returns {@code null}.
     */
    IrBuilder getIrBuilder();
}
