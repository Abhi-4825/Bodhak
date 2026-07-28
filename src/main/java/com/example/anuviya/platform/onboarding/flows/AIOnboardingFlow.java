package com.example.anuviya.platform.onboarding.flows;

import com.example.anuviya.platform.onboarding.OnboardingFlow;
import com.example.anuviya.platform.onboarding.OnboardingStep;

import java.util.List;

public class AIOnboardingFlow extends OnboardingFlow {
    private final List<OnboardingStep> steps;

    public AIOnboardingFlow(List<OnboardingStep> steps) {
        this.steps = steps;
    }

    @Override
    protected List<OnboardingStep> steps() {
        return steps;
    }
}
