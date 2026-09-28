package id.autograder.rule;
import com.github.javaparser.ast.CompilationUnit;
import id.autograder.model.Violation;
import java.util.List;

public interface Rule {
    String name();

    List<Violation> check(CompilationUnit unit);
}
