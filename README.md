# Autograder

Generic Java 21/Maven grader. It scans student projects, checks naming, long methods and nesting, and optionally runs the hidden payroll profile.

```bash
mvn -q test
mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/good --assignment payroll"
mvn -q compile exec:java -Dexec.args="--batch submissions --assignment payroll"
```

Without `--assignment`, only code quality is scored and the result is scaled to 100. With `--assignment payroll`, functional correctness is worth 60 points and quality 40. Add each submission as a separate directory under `submissions/`; Maven, Gradle, IntelliJ and plain Java layouts are detected automatically.
