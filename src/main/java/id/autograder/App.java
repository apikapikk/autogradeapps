package id.autograder;

import id.autograder.assignment.AssignmentProfile;
import id.autograder.assignment.AssignmentRegistry;
import id.autograder.engine.AutograderEngine;
import id.autograder.model.AssessmentResult;
import id.autograder.project.ProjectScanner;
import id.autograder.reporter.ConsoleReporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class App {
    public static void main(String[] args) {
        Map<String, String> arguments = parse(args);
        if (!hasExactlyOneMode(arguments)) {
            usage();
            System.exit(1);
        }
        Optional<AssignmentProfile> profile = findProfile(arguments);
        if (profile.isEmpty() && arguments.containsKey("assignment")) System.exit(1);
        ProjectScanner scanner = new ProjectScanner();
        ConsoleReporter reporter = new ConsoleReporter();
        AutograderEngine engine = new AutograderEngine();
        if (arguments.containsKey("project")) {
            AssessmentResult result = engine.grade(scanner.scan(Path.of(arguments.get("project"))), profile);
            System.out.print(reporter.render(result));
            return;
        }
        gradeBatch(arguments.get("batch"), profile, scanner, reporter, engine);
    }

    private static void gradeBatch(String directory, Optional<AssignmentProfile> profile,
            ProjectScanner scanner, ConsoleReporter reporter, AutograderEngine engine) {
        try (var paths = Files.list(Path.of(directory))) {
            List<AssessmentResult> results = paths.filter(Files::isDirectory).sorted()
                    .map(path -> engine.grade(scanner.scan(path), profile)).toList();
            if (results.isEmpty()) {
                System.out.println("no submissions found");
                return;
            }
            results.forEach(result -> System.out.print(reporter.render(result)));
            System.out.print(reporter.batchSummary(results));
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }
    }

    private static Optional<AssignmentProfile> findProfile(Map<String, String> arguments) {
        if (!arguments.containsKey("assignment")) return Optional.empty();
        Optional<AssignmentProfile> profile = new AssignmentRegistry().find(arguments.get("assignment"));
        if (profile.isEmpty()) {
            System.err.println("Unknown assignment: " + arguments.get("assignment")
                    + " (registered: payroll, inventory)");
        }
        return profile;
    }

    private static boolean hasExactlyOneMode(Map<String, String> arguments) {
        return arguments.containsKey("project") ^ arguments.containsKey("batch");
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> arguments = new HashMap<>();
        for (int index = 0; index + 1 < args.length; index += 2) {
            if (args[index].startsWith("--")) {
                arguments.put(args[index].substring(2), args[index + 1]);
            }
        }
        return arguments;
    }

    private static void usage() {
        System.err.println("Usage: autograder --project <dir> | --batch <dir> "
                + "[--assignment <name>]");
    }
}
