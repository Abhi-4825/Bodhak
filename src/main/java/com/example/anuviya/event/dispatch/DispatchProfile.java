package com.example.anuviya.event.dispatch;

/**
 * Named presets for the dispatcher's update cadence.
 *
 * <p>Choose based on project size and hardware capability:
 * <ul>
 *   <li>{@link #ULTRA}       — 16 ms  — small projects / fast SSD / responsiveness-first</li>
 *   <li>{@link #BALANCED}    — 50 ms  — default for most projects</li>
 *   <li>{@link #LOW_CPU}     — 150 ms — large repos / CI machines / battery mode</li>
 *   <li>{@link #MINIMAL}     — 250 ms — very large monorepos / background indexing</li>
 * </ul>
 *
 * <p>Profiles are consumed by {@link DispatchPolicy} implementations; they must
 * not be hard-coded anywhere else. Future adaptive policies can ignore the
 * {@code intervalMs} hint entirely and compute their own interval.
 */
public enum DispatchProfile {
    ULTRA(16),
    BALANCED(50),
    LOW_CPU(150),
    MINIMAL(250);

    private final long intervalMs;

    DispatchProfile(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public long intervalMs() {
        return intervalMs;
    }
}
