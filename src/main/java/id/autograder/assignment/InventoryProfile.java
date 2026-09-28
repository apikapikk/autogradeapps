package id.autograder.assignment;

import id.autograder.functional.FunctionalCase;
import id.autograder.functional.FunctionalCase.FunctionalCaseResult;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class InventoryProfile implements AssignmentProfile {
    @Override
    public String name() {
        return "inventory";
    }

    @Override
    public String targetClassName() {
        return "InventoryService";
    }

    @Override
    public List<FunctionalCase> cases() {
        return List.of(
                check("add and list item", 20, this::addAndList),
                check("duplicate SKU rejected", 15, this::duplicateSkuRejected),
                check("search by SKU or name", 20, this::search),
                check("restock increases stock", 15, this::restock),
                check("selling reduces stock", 15, this::sell),
                check("invalid stock operation rejected", 15, this::invalidStock));
    }

    private FunctionalCase check(String name, int weight, InventoryCheck test) {
        return FunctionalCase.of(name, weight, target -> {
            try {
                return result(name, weight, test.run(target));
            } catch (Exception exception) {
                return new FunctionalCaseResult(name, weight, false,
                        exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }
        });
    }

    private FunctionalCaseResult result(String name, int weight, boolean passed) {
        String message = passed ? "ok" : "expected inventory behavior";
        return new FunctionalCaseResult(name, weight, passed, message);
    }

    private boolean addAndList(Class<?> target) throws Exception {
        Object service = newService(target);
        invoke(service, "addItem", "BRG-001", "Keyboard", 250000.0, 10);
        List<?> items = (List<?>) invoke(service, "listItems");
        Object item = items.getFirst();
        return items.size() == 1
                && "BRG-001".equals(invoke(item, "getSku"))
                && "Keyboard".equals(invoke(item, "getName"))
                && Integer.valueOf(10).equals(invoke(item, "getStock"));
    }

    private boolean duplicateSkuRejected(Class<?> target) throws Exception {
        Object service = newService(target);
        invoke(service, "addItem", "BRG-001", "Keyboard", 250000.0, 10);
        return throwsException(() -> invoke(service, "addItem", "brg-001", "Mouse", 100000.0, 5));
    }

    private boolean search(Class<?> target) throws Exception {
        Object service = newService(target);
        invoke(service, "addItem", "BRG-001", "Mechanical Keyboard", 250000.0, 10);
        invoke(service, "addItem", "BRG-002", "Wireless Mouse", 150000.0, 8);
        List<?> bySku = (List<?>) invoke(service, "search", "001");
        List<?> byName = (List<?>) invoke(service, "search", "mouse");
        return bySku.size() == 1 && byName.size() == 1;
    }

    private boolean restock(Class<?> target) throws Exception {
        Object service = newService(target);
        invoke(service, "addItem", "BRG-001", "Keyboard", 250000.0, 10);
        invoke(service, "restock", "BRG-001", 5);
        return Integer.valueOf(15).equals(stock(service));
    }

    private boolean sell(Class<?> target) throws Exception {
        Object service = newService(target);
        invoke(service, "addItem", "BRG-001", "Keyboard", 250000.0, 10);
        invoke(service, "sell", "BRG-001", 3);
        return Integer.valueOf(7).equals(stock(service));
    }

    private boolean invalidStock(Class<?> target) throws Exception {
        Object service = newService(target);
        invoke(service, "addItem", "BRG-001", "Keyboard", 250000.0, 10);
        return throwsException(() -> invoke(service, "sell", "BRG-001", 11))
                && throwsException(() -> invoke(service, "restock", "BRG-001", 0));
    }

    private Object newService(Class<?> target) throws Exception {
        return target.getDeclaredConstructor().newInstance();
    }

    private Object stock(Object service) throws Exception {
        Object item = ((List<?>) invoke(service, "listItems")).getFirst();
        return invoke(item, "getStock");
    }

    private Object invoke(Object target, String name, Object... arguments) throws Exception {
        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == arguments.length) {
                try {
                    return method.invoke(target, arguments);
                } catch (InvocationTargetException exception) {
                    throw new RuntimeException(exception.getCause());
                }
            }
        }
        throw new NoSuchMethodException(name);
    }

    private boolean throwsException(ThrowingOperation operation) {
        try {
            operation.run();
            return false;
        } catch (Exception exception) {
            return true;
        }
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface InventoryCheck {
        boolean run(Class<?> target) throws Exception;
    }
}
