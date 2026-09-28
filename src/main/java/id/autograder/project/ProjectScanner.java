package id.autograder.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ProjectScanner {
    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            "target", "build", "out", ".git", ".idea", ".gradle", "test");

    public ScannedProject scan(Path input) {
        Path root = input.toAbsolutePath().normalize();
        try {
            List<Path> files = findSourceFiles(root);
            String layout = detectLayout(root, files);
            String error = files.isEmpty() ? "No Java source files found" : "";
            return new ScannedProject(root, files, layout, error);
        } catch (IOException exception) {
            return new ScannedProject(root, List.of(), "plain", exception.getMessage());
        }
    }

    private List<Path> findSourceFiles(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> !isIgnored(root, path))
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !isDescriptor(path))
                    .sorted()
                    .toList();
        }
    }

    private boolean isIgnored(Path root, Path path) {
        for (Path part : root.relativize(path)) {
            if (IGNORED_DIRECTORIES.contains(part.toString())) return true;
        }
        return false;
    }

    private boolean isDescriptor(Path path) {
        String name = path.getFileName().toString();
        return name.equals("package-info.java") || name.equals("module-info.java");
    }

    private String detectLayout(Path root, List<Path> files) {
        for (Path file : files) {
            String relative = root.relativize(file).toString().replace('\\', '/');
            if (relative.startsWith("src/main/java/")) return "maven";
            if (relative.startsWith("src/main/")) return "gradle";
            if (relative.startsWith("src/")) return "intellij";
        }
        return "plain";
    }
}
