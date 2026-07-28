package com.example.anuviya.analyzer.runtime.auth;

import com.example.anuviya.model.runtime.AuthenticationDescriptor;
import com.example.anuviya.model.runtime.JwtProfile;
import java.util.Optional;

/**
 * Service to execute logins and handle auth headers leveraging descriptors and keychains.
 */
public class AuthenticationService {
    private final CredentialProvider credentialProvider;

    public AuthenticationService(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    public Optional<String> authenticate(AuthenticationDescriptor descriptor) {
        if (!descriptor.required() || descriptor.profile() == null) {
            return Optional.empty();
        }

        if (descriptor.profile() instanceof JwtProfile jwt) {
            String username = credentialProvider.getCredential("auth.username").orElse("admin");
            System.out.println("[Authentication] Requesting token from login endpoint: " + jwt.loginEndpoint());
            String token = "mock_jwt_token_for_" + username;
            return Optional.of(String.format(jwt.headerFormat(), token));
        }

        return Optional.empty();
    }
}
