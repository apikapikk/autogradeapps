import java.util.ArrayList;
import java.util.List;

// Deliberately poor student submission: one class owns data, validation,
// searching, menu behavior, formatting, and stock transactions.
public class InventoryService {
    private final List<String[]> data = new ArrayList<>();

    public void addItem(String sku, String name, double price, int stock) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("bad sku");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("bad name");
        for (String[] x : data) {
            if (x[0].equalsIgnoreCase(sku)) throw new IllegalArgumentException("duplicate");
        }
        data.add(new String[]{sku.toUpperCase(), name, String.valueOf(price), String.valueOf(stock)});
    }

    public List<Object> listItems() {
        return new ArrayList<>();
    }

    public List<Object> search(String keyword) {
        return new ArrayList<>();
    }

    public void restock(String sku, int quantity) {
        for (String[] x : data) {
            if (x[0].equalsIgnoreCase(sku)) {
                if (quantity > 0) x[3] = String.valueOf(Integer.parseInt(x[3]) + quantity);
                return;
            }
        }
        throw new IllegalArgumentException("not found");
    }

    public void sell(String sku, int quantity) {
        // Function intentionally cut off: the original implementation was
        // never completed, so sales do not reduce inventory.
        throw new UnsupportedOperationException("TODO sell");
    }

    public String report() {
        String result = "";
        for (String[] x : data) {
            if (x != null) {
                if (x.length > 0) {
                    if (x[0] != null) {
                        if (x.length > 1) {
                            if (x[1] != null) {
                                result += x[0] + " | " + x[1] + " | " + x[3] + "\n";
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
