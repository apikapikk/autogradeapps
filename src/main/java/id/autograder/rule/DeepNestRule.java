package id.autograder.rule;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import id.autograder.model.Severity;
import id.autograder.model.Violation;
import java.util.ArrayList;
import java.util.List;

public class DeepNestRule implements Rule {
    public static final int MAX_DEPTH = 3;

    @Override
    public String name() {
        return "DeepNestRule";
    }

    @Override
    public List<Violation> check(CompilationUnit unit) {
        List<Violation> violations = new ArrayList<>();
        unit.findAll(CallableDeclaration.class).forEach(callable -> {
            int[] state = {0, 0, 0};
            walk(callable, state);
            if (state[1] > MAX_DEPTH) {
                violations.add(new Violation(name(), Severity.MAJOR, "",
                        state[2], "nesting depth " + state[1]
                                + " (max " + MAX_DEPTH + ")"));
            }
        });
        return violations;
    }

    private void walk(Node node, int[] state) {
        boolean control = isControl(node);
        int previousDepth = state[0];
        if (control && !isElseIf(node)) {
            state[0]++;
            if (state[0] > state[1]) {
                state[1] = state[0];
                state[2] = node.getRange().map(range -> range.begin.line).orElse(0);
            }
        }
        for (Node child : node.getChildNodes()) {
            if (!(child instanceof CallableDeclaration)) walk(child, state);
        }
        state[0] = previousDepth;
    }

    private boolean isControl(Node node) {
        return node instanceof IfStmt || node instanceof ForStmt
                || node instanceof ForEachStmt || node instanceof WhileStmt
                || node instanceof DoStmt || node instanceof SwitchStmt
                || node instanceof TryStmt;
    }

    private boolean isElseIf(Node node) {
        return node instanceof IfStmt
                && node.getParentNode().filter(parent -> parent instanceof IfStmt).isPresent();
    }
}
