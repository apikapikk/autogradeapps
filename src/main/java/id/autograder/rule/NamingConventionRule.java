package id.autograder.rule;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import id.autograder.model.Severity;
import id.autograder.model.Violation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class NamingConventionRule implements Rule {
    private static final Pattern CAMEL = Pattern.compile("[a-z][a-zA-Z0-9]*");
    private static final Pattern PASCAL = Pattern.compile("[A-Z][a-zA-Z0-9]*");
    private static final Pattern CONSTANT = Pattern.compile("[A-Z][A-Z0-9_]*");

    @Override
    public String name() {
        return "NamingConventionRule";
    }

    @Override
    public List<Violation> check(CompilationUnit unit) {
        List<Violation> violations = new ArrayList<>();
        unit.findAll(TypeDeclaration.class).forEach(type -> checkName(
                type.getNameAsString(), PASCAL, violations, line(type), "type", false));
        unit.findAll(MethodDeclaration.class).forEach(method -> checkName(
                method.getNameAsString(), CAMEL, violations, line(method), "method", false));
        unit.findAll(Parameter.class).forEach(parameter -> checkName(
                parameter.getNameAsString(), CAMEL, violations, line(parameter), "parameter", false));
        unit.findAll(VariableDeclarator.class).forEach(variable -> {
            boolean constant = variable.getParentNode()
                    .map(parent -> parent instanceof FieldDeclaration field
                            && field.isStatic() && field.isFinal())
                    .orElse(false);
            boolean counter = variable.findAncestor(com.github.javaparser.ast.stmt.ForStmt.class)
                    .isPresent();
            checkName(variable.getNameAsString(), constant ? CONSTANT : CAMEL,
                    violations, line(variable), "variable", counter);
        });
        return violations;
    }

    private int line(com.github.javaparser.ast.Node node) {
        return node.getBegin().map(position -> position.line).orElse(0);
    }

    private void checkName(String value, Pattern pattern, List<Violation> violations,
            int line, String kind, boolean loopCounter) {
        boolean shortName = value.length() == 1
                && !(loopCounter && Set.of("i", "j", "k").contains(value));
        if (shortName || !pattern.matcher(value).matches()) {
            violations.add(new Violation(name(), Severity.MINOR, "", line,
                    "'" + value + "' is not a valid " + kind + " name"));
        }
    }
}
