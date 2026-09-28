package id.autograder.assignment;
import id.autograder.functional.FunctionalCase;
import java.util.List;

public interface AssignmentProfile {
    String name();

    String targetClassName();

    List<FunctionalCase> cases();
}
