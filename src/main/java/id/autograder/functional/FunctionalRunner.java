package id.autograder.functional;

import id.autograder.model.FunctionalCaseResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FunctionalRunner {
    public record Result(List<FunctionalCaseResult> cases, double score, String error) {
    }

    public Result run(ProjectCompiler.Result compiled, String className,
            List<FunctionalCase> cases) {
        if (!compiled.success()) return new Result(List.of(), 0, compiled.diagnostics());
        try {
            Class<?> target = Class.forName(className, true, compiled.loader());
            List<FunctionalCaseResult> results = new ArrayList<>();
            for (FunctionalCase functionalCase : cases) {
                results.add(runOne(functionalCase, target));
            }
            int totalWeight = cases.stream().mapToInt(FunctionalCase::weight).sum();
            int passedWeight = results.stream().filter(FunctionalCaseResult::passed)
                    .mapToInt(FunctionalCaseResult::weight).sum();
            double score = totalWeight == 0 ? 0 : passedWeight / (double) totalWeight * 60;
            return new Result(results, score, "");
        } catch (Exception exception) {
            return new Result(List.of(), 0, exception.getMessage());
        }
    }

    private FunctionalCaseResult runOne(FunctionalCase functionalCase, Class<?> target) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            FunctionalCase.FunctionalCaseResult result = executor.submit(
                    () -> functionalCase.evaluate(target)).get(2, TimeUnit.SECONDS);
            return new FunctionalCaseResult(result.name(), result.weight(),
                    result.passed(), result.message());
        } catch (TimeoutException exception) {
            return new FunctionalCaseResult(functionalCase.name(), functionalCase.weight(),
                    false, "timed out after 2 seconds");
        } catch (Exception exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            return new FunctionalCaseResult(functionalCase.name(), functionalCase.weight(),
                    false, cause.getClass().getSimpleName() + ": " + cause.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }
}
