package id.autograder.model;
import java.util.List;

public record AssessmentResult(
        String projectName,
        int fileCount,
        String layout,
        double functionalScore,
        int qualityScore,
        double totalScore,
        String grade,
        List<FunctionalCaseResult> functionalCases,
        List<Violation> violations,
        String errorMessage) {

    public AssessmentResult {
        functionalCases = List.copyOf(functionalCases);
        violations = List.copyOf(violations);
    }
}
