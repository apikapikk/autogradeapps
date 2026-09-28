package id.autograder.model;

public record Violation(
        String rule,
        Severity severity,
        String file,
        int line,
        String message) {
}
