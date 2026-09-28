package id.autograder.reporter;

import id.autograder.model.AssessmentResult;

public interface AssessmentReporter {
    String render(AssessmentResult result);
}
