package com.example.anuviya.model.runtime;

/**
 * Basic access authentication configuration.
 */
public record BasicAuthProfile(
        String realm
) implements AuthenticationProfile {

    @Override
    public String getAuthType() {
        return "BASIC";
    }
}
