package id.autograder.assignment;

import id.autograder.functional.FunctionalCase;
import id.autograder.functional.FunctionalCase.FunctionalCaseResult;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PayrollProfile implements AssignmentProfile {
    @Override
    public String name() {
        return "payroll";
    }

    @Override
    public String targetClassName() {
        return "PayrollCalculator";
    }

    @Override
    public List<FunctionalCase> cases() {
        return List.of(
                check("work hours normal day", 10,
                        target -> equalsResult(target, "calculateWorkHours", 8.0,
                                LocalTime.of(8, 0), LocalTime.of(16, 0))),
                check("work hours invalid (throws)", 5,
                        target -> throwsException(target, "calculateWorkHours",
                                LocalTime.of(9, 0), LocalTime.of(8, 0))),
                check("overtime hours", 10,
                        target -> equalsResult(target, "calculateOvertimeHours", 3.0, 11.0)),
                check("late minutes (on time, late)", 10,
                        target -> equalsResult(target, "calculateLateMinutes", 30,
                                LocalTime.of(8, 30))),
                check("hourly rate per level + invalid level", 10, this::rates),
                check("overtime pay tiered", 15, this::overtimePay),
                check("late deduction blocks", 10, this::deduction),
                check("net pay examples", 20, this::netPay),
                check("payslip contains name and net pay digits", 10, this::payslip));
    }

    private FunctionalCase check(String name, int weight, Function<Class<?>, Boolean> test) {
        return FunctionalCase.of(name, weight,
                target -> result(name, weight, test.apply(target), "expected payroll behavior"));
    }

    private FunctionalCaseResult result(String name, int weight, boolean passed, String message) {
        return new FunctionalCaseResult(name, weight, passed, passed ? "ok" : message);
    }

    private boolean equalsResult(Class<?> target, String method, Object expected, Object... arguments) {
        return Objects.equals(invoke(target, method, arguments), expected);
    }

    private boolean throwsException(Class<?> target, String method, Object... arguments) {
        try {
            invoke(target, method, arguments);
            return false;
        } catch (Exception exception) {
            return true;
        }
    }

    private boolean rates(Class<?> target) {
        return equalsResult(target, "getHourlyRate", 20000.0, "junior")
                && equalsResult(target, "getHourlyRate", 30000.0, "MID")
                && equalsResult(target, "getHourlyRate", 45000.0, "SENIOR")
                && throwsException(target, "getHourlyRate", "x");
    }

    private boolean overtimePay(Class<?> target) {
        return equalsResult(target, "calculateOvertimePay", 30000.0, 1.0, 20000.0)
                && equalsResult(target, "calculateOvertimePay", 60000.0, 2.0, 20000.0)
                && equalsResult(target, "calculateOvertimePay", 100000.0, 3.0, 20000.0)
                && equalsResult(target, "calculateOvertimePay", 140000.0, 4.0, 20000.0);
    }

    private boolean deduction(Class<?> target) {
        return equalsResult(target, "calculateLateDeduction", 0.0, 0, 20000.0)
                && equalsResult(target, "calculateLateDeduction", 5000.0, 1, 20000.0)
                && equalsResult(target, "calculateLateDeduction", 5000.0, 15, 20000.0)
                && equalsResult(target, "calculateLateDeduction", 10000.0, 16, 20000.0)
                && equalsResult(target, "calculateLateDeduction", 10000.0, 30, 20000.0);
    }

    private boolean netPay(Class<?> target) {
        return equalsResult(target, "calculateNetPay", 585000L, "SENIOR",
                        LocalTime.of(8, 0), LocalTime.of(19, 0))
                && equalsResult(target, "calculateNetPay", 150000L, "JUNIOR",
                        LocalTime.of(8, 30), LocalTime.of(16, 30))
                && equalsResult(target, "calculateNetPay", 240000L, "MID",
                        LocalTime.of(8, 0), LocalTime.of(16, 0))
                && equalsResult(target, "calculateNetPay", 60000L, "MID",
                        LocalTime.of(9, 0), LocalTime.of(12, 0));
    }

    private boolean payslip(Class<?> target) {
        String payslip = (String) invoke(target, "formatPayslip", "Ani", "MID",
                LocalTime.of(8, 0), LocalTime.of(16, 0));
        return payslip.contains("Ani") && payslip.matches("(?s).*240000.*");
    }

    private Object invoke(Class<?> target, String name, Object... arguments) {
        try {
            for (Method method : target.getDeclaredMethods()) {
                if (method.getName().equals(name)
                        && method.getParameterCount() == arguments.length) {
                    return method.invoke(null, arguments);
                }
            }
            throw new NoSuchMethodException(name);
        } catch (InvocationTargetException exception) {
            throw new RuntimeException(exception.getCause());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
