package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.runtime.*;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.DiagnosticEntry;
import com.example.anuviya.model.runtime.RuntimeDiagnostics.Severity;

/**
 * Plans the authentication strategies based on security capabilities and technologies.
 */
public class AuthenticationPlanningPass implements RuntimePlanningPass {
    @Override
    public void execute(RuntimePlanningContext context, RuntimeFacts facts) {
        boolean hasJwt = facts.detectedTechnologies().contains("JWT") || facts.capabilities().contains("AUTHENTICATION_JWT");
        boolean hasBasic = facts.detectedTechnologies().contains("BASIC_AUTH") || facts.capabilities().contains("AUTHENTICATION_BASIC");

        if (hasJwt) {
            String loginUrl = "/api/auth/login";
            AuthenticationProfile profile = new JwtProfile(loginUrl, "token", "Bearer %s");
            context.setAuthenticationDescriptor(new AuthenticationDescriptor(true, profile));
            context.addDiagnostic(new DiagnosticEntry(
                    "JWT_AUTHENTICATION_RESOLVED",
                    "Application requires JWT Bearer validation. Discovered login handler: " + loginUrl,
                    Severity.LOW
            ));
        } else if (hasBasic) {
            AuthenticationProfile profile = new BasicAuthProfile("BodhakRealm");
            context.setAuthenticationDescriptor(new AuthenticationDescriptor(true, profile));
            context.addDiagnostic(new DiagnosticEntry(
                    "BASIC_AUTHENTICATION_RESOLVED",
                    "Application requires HTTP Basic Access Authentication.",
                    Severity.LOW
            ));
        } else {
            context.setAuthenticationDescriptor(new AuthenticationDescriptor(false, null));
        }
    }
}
