package com.example.bodhak.model.project;

/**
 * Capabilities that externally exposed project surfaces can have.
 */
public enum RootCapability {
    EXECUTABLE,             // Standalone runtime execution (mains, CLI entrypoints)
    API_PROVIDER,           // Exposes web/RPC endpoints (controllers, router definitions)
    LIBRARY_EXPORT,         // Public classes, functions, or package exports
    BUILD_EXTENSION,        // Custom compiler plugins, annotation processors, build tags
    CONFIGURATION_PROVIDER, // Startup properties, service wiring classes
    TEST_ENTRY,             // Test suites and execution modules
    FRAMEWORK_BOOTSTRAP,    // Framework runtime configuration hooks
    DEPLOYMENT_UNIT         // Serverless handlers, deployment entry points
}
