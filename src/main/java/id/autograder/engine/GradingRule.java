package id.autograder.engine;
import id.autograder.model.Severity;
import id.autograder.model.Violation;
import java.util.List;

public class GradingRule {
    public static final int QUALITY_MAX = 40;
    public static final int FUNCTIONAL_MAX = 60;

    public int quality(List<Violation> violations) {
        int penalty = violations.stream()
                .mapToInt(this::penalty)
                .sum();
        return Math.max(0, QUALITY_MAX - penalty);
    }

    public double total(double functionalScore, int qualityScore, boolean full) {
        return full ? functionalScore + qualityScore
                : qualityScore / (double) QUALITY_MAX * 100;
    }

    public String grade(double score) {
        if (score >= 85) return "A";
        if (score >= 70) return "B";
        if (score >= 55) return "C";
        if (score >= 40) return "D";
        return "E";
    }

    private int penalty(Violation violation) {
        return violation.severity() == Severity.MAJOR ? 5 : 2;
    }
}
