package id.autograder.reporter;

import id.autograder.model.AssessmentResult;
import java.util.List;

public class ConsoleReporter implements AssessmentReporter {
    @Override
    public String render(AssessmentResult result) {
        StringBuilder output = new StringBuilder("=== AUTOGRADER RESULT ===\n")
                .append("Project    : ").append(result.projectName()).append('\n')
                .append("Detected   : ").append(result.fileCount()).append(" file(s) in ")
                .append(result.layout()).append('\n');
        renderFunctional(output, result);
        output.append("\nCODE QUALITY (40 pts)\n");
        result.violations().forEach(violation -> output.append("  [")
                .append(violation.severity()).append("] ").append(violation.rule())
                .append(' ').append(violation.file()).append(':').append(violation.line())
                .append("  ").append(violation.message()).append('\n'));
        output.append("  Quality score: ").append(result.qualityScore()).append(" / 40\n\n")
                .append("TOTAL: ").append(result.totalScore()).append(" / 100   GRADE: ")
                .append(result.grade()).append('\n');
        if (result.errorMessage() != null && !result.errorMessage().isBlank()) {
            output.append("ERROR: ").append(result.errorMessage()).append('\n');
        }
        return output.toString();
    }

    public String batchSummary(List<AssessmentResult> results) {
        StringBuilder output = new StringBuilder("\nSUMMARY\n")
                .append("student\tfunctional\tquality\ttotal\tgrade\n");
        results.forEach(result -> output.append(result.projectName()).append('\t')
                .append(result.functionalScore()).append('\t').append(result.qualityScore())
                .append('\t').append(result.totalScore()).append('\t').append(result.grade())
                .append('\n'));
        return output.toString();
    }

    private void renderFunctional(StringBuilder output, AssessmentResult result) {
        if (result.functionalCases().isEmpty()) {
            output.append("\nFUNCTIONAL: not assessed\n");
            return;
        }
        output.append("\nFUNCTIONAL (60 pts)\n");
        result.functionalCases().forEach(test -> output.append("  [")
                .append(test.passed() ? "PASS" : "FAIL").append("] ")
                .append(test.name()).append(" (").append(test.weight()).append(") ")
                .append(test.message()).append('\n'));
        output.append("  Functional score: ").append(result.functionalScore()).append(" / 60\n");
    }
}
