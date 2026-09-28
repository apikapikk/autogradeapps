# Autograder: Agent Guide (Project 1 of 2)

This repo is the **grader**. It is **generic**: it works on any Java application, not only payroll. It is pointed at a finished student project, detects the Java sources inside, runs the code-quality rules, and produces a score plus the list of violated rules. Functional correctness is graded only when an assignment profile (hidden tests for one specific task) is selected.

Two independent projects exist:
1. `autograder/` (this repo): the grading tool.
2. `payroll-assignment/`: the payroll CLI given to students with core methods emptied. Built separately.

The autograder repo ships with an **empty `submissions/` folder** (only `.gitkeep`). Finished student projects are dropped there, one subfolder per student (any Java project, any structure), and graded in batch.

**Deadline context:** demo tomorrow. Build one thin end-to-end path first (Phases 0 to 7). Stretch is optional.

---

## 1. Ground rules for agents

- Java 21, Maven only. Base package `id.autograder`.
- Dependencies: `com.github.javaparser:javaparser-core` (3.26.2+), `org.junit.jupiter:junit-jupiter` (5.10+, test scope), `exec-maven-plugin`.
- Work phase by phase. After each phase run `mvn -q test`, then `git commit -m "phase N: ..."`.
- A TODO is done only when its **Done when** condition is verified by running a command.
- Never weaken a hidden test or threshold to make a fixture pass. If something looks wrong, stop and report.
- The autograder's own code must pass its own quality rules (small methods, flat logic, clean names).
- No frameworks, no database, no network.

---

## 2. Structure

```
autograder/
├── AGENTS.md
├── pom.xml
├── README.md
├── demo.sh
├── submissions/                     # EMPTY (.gitkeep). Student projects go here
├── src/main/java/id/autograder/
│   ├── App.java                     # CLI
│   ├── model/                       # Severity, Violation, FunctionalCaseResult, AssessmentResult
│   ├── project/
│   │   ├── ProjectScanner.java      # detects sources in a student project
│   │   └── ScannedProject.java      # root path, list of .java files
│   ├── rule/
│   │   ├── Rule.java
│   │   ├── LongMethodRule.java
│   │   ├── NamingConventionRule.java
│   │   └── DeepNestRule.java
│   ├── functional/
│   │   ├── ProjectCompiler.java     # compiles all sources with javax.tools, loads classes
│   │   ├── FunctionalCase.java
│   │   └── FunctionalRunner.java
│   ├── assignment/
│   │   ├── AssignmentProfile.java   # interface: name, targetClassName, cases()
│   │   ├── AssignmentRegistry.java  # name → profile lookup
│   │   └── PayrollProfile.java      # hidden cases for the payroll assignment
│   ├── engine/
│   │   ├── GradingRule.java         # weights, penalties, grade thresholds
│   │   └── AutograderEngine.java
│   └── reporter/
│       ├── AssessmentReporter.java
│       └── ConsoleReporter.java
└── src/test/
    ├── java/id/autograder/...       # unit tests
    └── resources/fixtures/          # mini student projects for tests: skeleton, good, bad, broken
```

Fixtures are test data, not submissions. They come from `payroll-assignment/_private/samples/` (copy them in once that repo has them). Until then, create minimal fixtures yourself that match the descriptions in Section 5, and replace them later.

---

## 3. Flow

```
CLI:  autograder --project <dir>              (grade one student project)
      autograder --batch <dir>                (grade every subfolder, e.g. submissions/)
        │
        ▼
  ProjectScanner: find src/main/java (fallback: all .java under the root)
        │            → ScannedProject (files list). No .java found → error result.
        ▼
  AutograderEngine.grade(project, profile)
        │
   ┌────┴─────────────────────────────┐
   ▼                                  ▼
 Static analysis                Functional testing
 for each file:                 1. ProjectCompiler compiles all files
   parse (JavaParser)              (compile error → functional 0 + diagnostics)
   run every Rule               2. Load profile.targetClassName()
   → List<Violation>            3. Run each FunctionalCase via reflection
   (parse error → recorded,        with a 2s timeout
    file skipped)
   └────┬─────────────────────────────┘
        ▼
  GradingRule: functional (0..60) + quality (0..40) = total (0..100), letter grade
        ▼
  AssessmentResult → ConsoleReporter
  (batch mode: also a summary table, one row per student folder)
```

Static and functional parts are independent: a failure in one never stops the other.

### Grading modes
| Mode | When | What is graded |
|---|---|---|
| **Quality-only** (default) | no `--assignment` given | Static rules only, on any Java project. Total = quality score scaled to 100. The report states "functional: not assessed". |
| **Full** | `--assignment payroll` (or another registered profile) | Functional (0..60) + quality (0..40) = total (0..100). |

Quality-only mode is what makes the tool generic. Full mode needs one `AssignmentProfile` per task, written by the lecturer (see Section 5). Adding a new task means adding a new profile class and registering it in `AssignmentRegistry`; no other code changes.

---

## 4. Components

### Model
Records where possible. `AssessmentResult`: project name, file count, functional score, quality score, total, grade, functional case results, violations (with file name), optional error message.

### Rule interface
```java
public interface Rule {
    String name();
    List<Violation> check(CompilationUnit unit);
}
```
Stateless. Testable by parsing a source string.

### LongMethodRule
Flag methods/constructors longer than **20 lines** (`end - begin + 1`, signature and braces included). `MAJOR`. Message has method name and actual length. Threshold is a named constant.

### DeepNestRule
Depth counts `if`, `for`, `foreach`, `while`, `do`, `switch`, `try`. An `else if` chain does not increase depth per branch. Flag methods whose max depth is **> 3** (one violation per method, line of the deepest structure). `MAJOR`.

### NamingConventionRule
Types `PascalCase`. Methods, parameters, locals, non-constant fields `camelCase` (`^[a-z][a-zA-Z0-9]*$`). `static final` constants `UPPER_SNAKE_CASE`. One-letter names are violations except `i`, `j`, `k` as `for` counters. `MINOR`.

### ProjectScanner
Must handle whatever layout students hand in:
- Maven or Gradle (`src/main/java`), IntelliJ default (`src/`), or plain folders with `.java` files anywhere.
- A single wrapper folder (student zipped the project inside another folder): descend into it automatically.
- Ignore build and tooling output: `target/`, `build/`, `out/`, `.git/`, `.idea/`, `.gradle/`, and test source directories (`src/test`, any folder named `test`).
- Ignore `package-info.java` and `module-info.java`.
- Return the `.java` files sorted, plus a short "detected layout" label (`maven`, `gradle`, `intellij`, `plain`) for the report.
- No `.java` files found → result with an error message, score 0, and no crash.

### ProjectCompiler
Copy sources to a temp dir, compile with `ToolProvider.getSystemJavaCompiler()`, collect diagnostics, load with `URLClassLoader`. Return loaded classes or the compile errors.

### FunctionalCase / FunctionalRunner
A case has `name`, `weight`, and logic that receives the loaded target `Class<?>` and returns pass/fail with a message. Use reflection. Treat `UnsupportedOperationException` (unfilled skeleton) and any exception as a failed case with a clear message. Enforce a 2-second timeout per case (run in an executor).
Functional score = `passed weight / total weight * 60`.

### AssignmentProfile / PayrollProfile
```java
public interface AssignmentProfile {
    String name();
    String targetClassName();          // "PayrollCalculator"
    List<FunctionalCase> cases();
}
```
Profiles are registered by name in `AssignmentRegistry` (`payroll` is the only one for now). Hidden cases live here, never in the student project.

### GradingRule
Quality max 40. Quality starts at 40; each `MAJOR` costs 5, each `MINOR` costs 2, floor 0.
Full mode: functional max 60, total = functional + quality. Quality-only mode: total = quality / 40 * 100.
Grade: A ≥ 85, B ≥ 70, C ≥ 55, D ≥ 40, else E. All numbers in one place.

### AutograderEngine
`AssessmentResult grade(ScannedProject, Optional<AssignmentProfile>)`. Empty profile = quality-only mode. Never throws on bad student code. Rules are injected via the constructor (default: the three rules).

### ConsoleReporter
```
=== AUTOGRADER RESULT ===
Project    : submissions/andi
Detected   : 1 file(s) in src/main/java

FUNCTIONAL (60 pts)
  [PASS] work hours normal day        (10)
  [FAIL] late deduction blocks        (10)  expected 10000 but got 5000
  ...
  Functional score: 48.0 / 60

CODE QUALITY (40 pts)
  [MAJOR] LongMethodRule    PayrollCalculator.java:34  calculateNetPay is 47 lines (max 20)
  [MAJOR] DeepNestRule      PayrollCalculator.java:52  nesting depth 5 (max 3)
  [MINOR] NamingConvention  PayrollCalculator.java:12  'x' is too short
  Quality score: 26 / 40

TOTAL: 74 / 100   GRADE: B
```
Batch mode prints one block per student, then a summary table (folder, functional, quality, total, grade).

### App
`--project <dir>` or `--batch <dir>` (exactly one required), optional `--assignment <name>` (omitted = quality-only mode; unknown name = usage error listing the registered names). Exit 0 when grading ran, 1 on usage errors. `--batch` on an empty folder prints "no submissions found".

---

## 5. Payroll profile (hidden cases)

The student class is `PayrollCalculator` (default package) with these static methods:

```java
double calculateWorkHours(LocalTime checkIn, LocalTime checkOut)
double calculateOvertimeHours(double workHours)
int    calculateLateMinutes(LocalTime checkIn)
double getHourlyRate(String level)
double calculateOvertimePay(double overtimeHours, double hourlyRate)
double calculateLateDeduction(int lateMinutes, double hourlyRate)
long   calculateNetPay(String level, LocalTime checkIn, LocalTime checkOut)
String formatPayslip(String name, String level, LocalTime checkIn, LocalTime checkOut)
```

Rules: standard day 8h, start 08:00. Work hours = minutes / 60.0 (check-out not after check-in throws `IllegalArgumentException`). Overtime = `max(0, work - 8)`. Late minutes = `max(0, minutes after 08:00)`. Rates: JUNIOR 20000, MID 30000, SENIOR 45000, case-insensitive, unknown level throws `IllegalArgumentException`. Base pay = `min(work, 8) * rate`. Overtime pay: first 2 overtime hours at 1.5x, rest at 2x, times rate. Late deduction = `ceil(late / 15.0) * 0.25 * rate`. Net = `round(base + overtime - deduction)`, minimum 0.

Worked examples: SENIOR 08:00-19:00 → **585000**. JUNIOR 08:30-16:30 → **150000**. MID 08:00-16:00 → **240000**. MID 09:00-12:00 → **60000**.

| Case | Weight |
|---|---|
| work hours normal day | 10 |
| work hours invalid (throws) | 5 |
| overtime hours | 10 |
| late minutes (on time, late) | 10 |
| hourly rate per level + invalid level | 10 |
| overtime pay tiered (1h, 2h, 3h, 4h) | 15 |
| late deduction blocks (0, 1, 15, 16, 30 min) | 10 |
| net pay: the four examples above | 20 |
| payslip contains name and net pay digits | 10 |

Fixtures expected (`src/test/resources/fixtures/<name>/src/main/java/PayrollCalculator.java`):
- `good`: correct and clean.
- `bad`: correct but smelly (one 30+ line method, nesting depth ≥ 4, names like `x`, `tmp`).
- `broken`: clean but wrong (bad overtime multiplier, off-by-one in late blocks).
- `skeleton`: all bodies `throw new UnsupportedOperationException("TODO")`.

---

## 6. TODO (in order)

### Phase 0: Setup
- [ ] `pom.xml` (Java 21, dependencies, exec plugin with main class `id.autograder.App`), folder layout, `.gitignore` (`target/`, `.idea/`), `submissions/.gitkeep`.
- **Done when:** `mvn -q compile` succeeds.

### Phase 1: Model
- [ ] `Severity`, `Violation`, `FunctionalCaseResult`, `AssessmentResult`.
- **Done when:** compiles; a trivial test builds each object.

### Phase 2: Rules
- [ ] `Rule`, `LongMethodRule`, `DeepNestRule`, `NamingConventionRule`, each with tests (20 vs 21 lines; depth 3 vs 4 and `else if`; bad class/method/variable/constant/one-letter, `i` allowed in `for`).
- **Done when:** `mvn -q test` passes with at least one passing and one failing case per rule.

### Phase 3: Project detection
- [ ] `ProjectScanner`, `ScannedProject`.
- [ ] Tests: Maven layout, Gradle layout, IntelliJ `src/` layout, plain folder with `.java` at root, one wrapper folder around the project, ignored directories (`target`, `build`, `out`, tests), folder with no Java files.
- **Done when:** those tests pass.

### Phase 4: Functional testing
- [ ] `ProjectCompiler`, `FunctionalCase`, `FunctionalRunner` (timeout), `AssignmentProfile`, `PayrollProfile`.
- [ ] Tests on fixtures: `good` scores 60, `skeleton` scores 0, `broken` between 0 and 60, uncompilable project gives 0 with diagnostics.
- **Done when:** those tests pass.

### Phase 5: Grading and engine
- [ ] `GradingRule`, `AutograderEngine`.
- [ ] Tests (full mode, profile `payroll`): `good` total ≥ 95; `bad` functional ≥ 55 and quality ≤ 30 (adjust the fixture, not the thresholds); `broken` functional < 60 with quality high; `skeleton` functional 0.
- [ ] Tests (quality-only mode, no profile): `good` scores high, `bad` scores clearly lower, and the result says functional was not assessed.
- [ ] Test on a non-payroll fixture (e.g. a small `todo-app` or `calculator` Java project written for the test): quality-only mode runs without errors and reports violations.
- **Done when:** engine tests pass.

### Phase 6: Reporter and CLI
- [ ] `AssessmentReporter`, `ConsoleReporter` (single and batch with summary table), `App`.
- **Done when:**
  ```
  mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/good --assignment payroll"
  mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/bad --assignment payroll"
  mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/bad"
  mvn -q compile exec:java -Dexec.args="--batch src/test/resources/fixtures --assignment payroll"
  ```
  each print a readable report (the third one in quality-only mode), and `--batch submissions` prints "no submissions found".

### Phase 7: Demo readiness
- [ ] `README.md` (purpose, build, run, scoring table, how to add submissions).
- [ ] `demo.sh`: copies the fixture projects `good`, `bad`, `broken` into `submissions/`, runs `--batch submissions --assignment payroll`, then runs it again without `--assignment` to show quality-only mode, then removes the copies so the folder is empty again.
- **Done when:** running `demo.sh` from a clean clone works and leaves `submissions/` empty.

### Stretch
- [ ] JSON reporter, config file for weights, extra rules (magic numbers, too many parameters), additional assignment profiles.

---

## 7. Ask the owner before changing
- Thresholds (20 lines, depth 3) and penalty weights: they must be justified in the thesis.
- The payroll business rules above: hidden cases and the assignment project depend on them.