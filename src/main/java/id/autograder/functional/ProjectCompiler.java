package id.autograder.functional;

import id.autograder.project.ScannedProject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

public class ProjectCompiler {
    public record Result(ClassLoader loader, String diagnostics, boolean success) {
    }

    public Result compile(ScannedProject project) {
        if (project.sourceFiles().isEmpty()) {
            return new Result(null, "No Java source files found", false);
        }
        try {
            Path output = Files.createTempDirectory("autograder-");
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) return new Result(null, "Java compiler unavailable", false);
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            List<String> names = project.sourceFiles().stream().map(Path::toString).toList();
            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, null, diagnostics, List.of("-d", output.toString()), null,
                    compiler.getStandardFileManager(diagnostics, null, null)
                            .getJavaFileObjectsFromStrings(names));
            boolean success = task.call();
            String message = diagnostics.getDiagnostics().stream()
                    .map(Object::toString)
                    .reduce("", (left, right) -> left + right + System.lineSeparator());
            if (!success) return new Result(null, message, false);
            return new Result(new java.net.URLClassLoader(
                    new java.net.URL[]{output.toUri().toURL()}), message, true);
        } catch (Exception exception) {
            return new Result(null, exception.getMessage(), false);
        }
    }
}
