package id.autograder.assignment;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssignmentRegistry {
    private final Map<String, AssignmentProfile> profiles =
            Map.of("payroll", new PayrollProfile());

    public Optional<AssignmentProfile> find(String name) {
        return Optional.ofNullable(profiles.get(name));
    }

    public Set<String> names() {
        return profiles.keySet();
    }
}
