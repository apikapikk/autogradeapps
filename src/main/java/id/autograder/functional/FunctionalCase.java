package id.autograder.functional;

import java.util.function.Function;

public interface FunctionalCase {
    String name();

    int weight();

    FunctionalCaseResult evaluate(Class<?> target) throws Exception;

    record FunctionalCaseResult(
            String name,
            int weight,
            boolean passed,
            String message) {
    }

    static FunctionalCase of(
            String name,
            int weight,
            Function<Class<?>, FunctionalCaseResult> function) {
        return new FunctionalCase() {
            public String name() {
                return name;
            }

            public int weight() {
                return weight;
            }

            public FunctionalCaseResult evaluate(Class<?> target) {
                return function.apply(target);
            }
        };
    }
}
