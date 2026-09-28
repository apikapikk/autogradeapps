package id.autograder.rule;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import id.autograder.model.Severity;
import id.autograder.model.Violation;
import java.util.ArrayList;
import java.util.List;

public class LongMethodRule implements Rule {
    public static final int MAX_LINES = 20;

    @Override
    public String name() {
        return "LongMethodRule";
    }

    @Override
    public List<Violation> check(CompilationUnit unit) {
        List<Violation> violations = new ArrayList<>();
        unit.findAll(CallableDeclaration.class).forEach(callable -> {
            if (callable.getRange().isEmpty()) return;
            var range = callable.getRange().get();
            int lines = range.end.line - range.begin.line + 1;
            if (lines <= MAX_LINES) return;
            String methodName = callable instanceof MethodDeclaration method
                    ? method.getNameAsString() : "constructor";
            violations.add(new Violation(name(), Severity.MAJOR, "",
                    range.begin.line, methodName + " is " + lines
                            + " lines (max " + MAX_LINES + ")"));
        });
        return violations;
    }
}
