package com.example.bodhak.classification;

public enum Capability {

    HTTP_ENDPOINT(Category.NETWORK),
    WEB_UI(Category.USER_INTERFACE),
    DESKTOP_UI(Category.USER_INTERFACE),
    MOBILE_UI(Category.USER_INTERFACE),
    CLI(Category.USER_INTERFACE),
    DATABASE_ACCESS(Category.DATA),
    SECURITY(Category.INFRASTRUCTURE),
    MESSAGE_QUEUE(Category.NETWORK),
    WEBSOCKET(Category.NETWORK),
    GRAPHQL(Category.NETWORK),
    SERVERLESS_FUNCTION(Category.INFRASTRUCTURE),
    BACKGROUND_JOB(Category.INFRASTRUCTURE),
    TEMPLATE_RENDERING(Category.USER_INTERFACE),
    STATIC_SITE_GENERATION(Category.USER_INTERFACE),
    FILE_IO(Category.DATA),
    GRPC(Category.NETWORK),
    SCHEDULED_TASK(Category.INFRASTRUCTURE),
    DEPENDENCY_INJECTION(Category.CORE),
    ORM(Category.DATA),
    TESTING(Category.CORE),
    CONFIGURATION_MANAGEMENT(Category.CORE);

    private final Category category;

    Capability(Category category) {
        this.category = category;
    }

    public Category getCategory() {
        return category;
    }

    public enum Category {
        USER_INTERFACE,
        NETWORK,
        DATA,
        INFRASTRUCTURE,
        CORE
    }
}
