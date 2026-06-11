package com.example.bodhakfrontend.core.gatling.parser;

/**
 * Signals that Gatling's output JSON could not be parsed into PerformanceMetrics.
 */
public class GatlingParseException extends Exception {

    public GatlingParseException(String message) {
        super(message);
    }

    public GatlingParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
