package id.autograder.project;
import java.nio.file.Path;
import java.util.List;

public record ScannedProject(
        Path root,
        List<Path> sourceFiles,
        String layout,
        String errorMessage) {

    public ScannedProject {
        sourceFiles = List.copyOf(sourceFiles);
    }
}
