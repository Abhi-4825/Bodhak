package com.example.anuviya.platform.onboarding;

public interface OnboardingStep {
    enum StepResult {
        SUCCESS,
        FAILED,
        SKIPPED
    }

    String id();
    String displayName();
    StepResult execute(OnboardingContext ctx);
    String failureMessage(OnboardingContext ctx);
}
