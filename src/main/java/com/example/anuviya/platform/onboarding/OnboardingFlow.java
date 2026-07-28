package com.example.anuviya.platform.onboarding;

import java.util.List;

public abstract class OnboardingFlow {
    protected abstract List<OnboardingStep> steps();
    
    public void run(OnboardingContext ctx, java.util.function.Consumer<OnboardingStep> onStepSuccess, java.util.function.Consumer<OnboardingStep> onStepFailure) {
        for (OnboardingStep step : steps()) {
            OnboardingStep.StepResult res = step.execute(ctx);
            if (res == OnboardingStep.StepResult.FAILED) {
                if (onStepFailure != null) {
                    onStepFailure.accept(step);
                }
                return;
            }
            if (onStepSuccess != null) {
                onStepSuccess.accept(step);
            }
        }
    }
}
