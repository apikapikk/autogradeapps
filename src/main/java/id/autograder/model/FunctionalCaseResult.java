package id.autograder.model;

public record FunctionalCaseResult(
        String name,
        int weight,
        boolean passed,
        String message) {
}
