package com.example.anuviya.classification;

public enum Capability {

    HTTP_ENDPOINT(CapabilityCategory.NETWORK),
    WEB_UI(CapabilityCategory.UI),
    DESKTOP_UI(CapabilityCategory.UI),
    MOBILE_UI(CapabilityCategory.UI),
    CLI(CapabilityCategory.UI),
    DATABASE_ACCESS(CapabilityCategory.DATA),
    SECURITY(CapabilityCategory.INFRA),
    MESSAGE_QUEUE(CapabilityCategory.NETWORK),
    WEBSOCKET(CapabilityCategory.NETWORK),
    GRAPHQL(CapabilityCategory.NETWORK),
    SERVERLESS_FUNCTION(CapabilityCategory.INFRA),
    BACKGROUND_JOB(CapabilityCategory.INFRA),
    TEMPLATE_RENDERING(CapabilityCategory.UI),
    STATIC_SITE_GENERATION(CapabilityCategory.UI),
    FILE_IO(CapabilityCategory.DATA),
    GRPC(CapabilityCategory.NETWORK),
    SCHEDULED_TASK(CapabilityCategory.INFRA),
    DEPENDENCY_INJECTION(CapabilityCategory.GENERAL),
    ORM(CapabilityCategory.DATA),
    TESTING(CapabilityCategory.GENERAL),
    CONFIGURATION_MANAGEMENT(CapabilityCategory.GENERAL);

    private final CapabilityCategory category;

    Capability(CapabilityCategory category) {
        this.category = category;
    }

    public CapabilityCategory getCategory() {
        return category;
    }
}
