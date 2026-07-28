package com.example.anuviya.analyzer.gatling.runner;

/**
 * Signals that Gatling failed to execute or was not properly configured.
 */
public class GatlingExecutionException extends Exception {

    public GatlingExecutionException(String message) {
        super(message);
    }

    public GatlingExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
