package id.autograder.engine;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import id.autograder.assignment.AssignmentProfile;
import id.autograder.functional.FunctionalRunner;
import id.autograder.functional.ProjectCompiler;
import id.autograder.model.AssessmentResult;
import id.autograder.model.FunctionalCaseResult;
import id.autograder.model.Severity;
import id.autograder.model.Violation;
import id.autograder.project.ScannedProject;
import id.autograder.rule.DeepNestRule;
import id.autograder.rule.LongMethodRule;
import id.autograder.rule.NamingConventionRule;
import id.autograder.rule.Rule;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AutograderEngine {
    private final List<Rule> rules;
    private final GradingRule grading = new GradingRule();

    public AutograderEngine() {
        this(List.of(new LongMethodRule(), new DeepNestRule(), new NamingConventionRule()));
    }

    public AutograderEngine(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    public AssessmentResult grade(ScannedProject project,
            Optional<AssignmentProfile> profile) {
        List<Violation> violations = new ArrayList<>();
        for (Path file : project.sourceFiles()) analyze(file, violations);
        if (project.sourceFiles().isEmpty()) {
            return new AssessmentResult(project.root().getFileName().toString(), 0,
                    project.layout(), 0, 0, 0, "E", List.of(), violations,
                    project.errorMessage());
        }
        FunctionalRunner.Result functional = new FunctionalRunner.Result(List.of(), 0, "");
        if (profile.isPresent()) {
            ProjectCompiler.Result compiled = new ProjectCompiler().compile(project);
            functional = new FunctionalRunner().run(compiled,
                    profile.get().targetClassName(), profile.get().cases());
        }
        int quality = grading.quality(violations);
        double total = grading.total(functional.score(), quality, profile.isPresent());
        List<FunctionalCaseResult> cases = functional.cases().stream()
                .map(result -> new FunctionalCaseResult(result.name(), result.weight(),
                        result.passed(), result.message()))
                .toList();
        return new AssessmentResult(project.root().getFileName().toString(),
                project.sourceFiles().size(), project.layout(), functional.score(), quality,
                total, grading.grade(total), cases, violations, functional.error());
    }

    private void analyze(Path file, List<Violation> violations) {
        try {
            var parser = new JavaParser(new ParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_21));
            var unit = parser.parse(file).getResult().orElseThrow();
            for (Rule rule : rules) {
                for (Violation violation : rule.check(unit)) {
                    violations.add(new Violation(violation.rule(), violation.severity(),
                            file.getFileName().toString(), violation.line(), violation.message()));
                }
            }
        } catch (IOException | RuntimeException exception) {
            violations.add(new Violation("Parser", Severity.MAJOR,
                    file.getFileName().toString(), 0,
                    "parse error: " + exception.getMessage()));
        }
    }
}
