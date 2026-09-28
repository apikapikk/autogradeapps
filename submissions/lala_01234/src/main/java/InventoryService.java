import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InventoryService {
    private final List<InventoryItem> items = new ArrayList<>();

    public void addItem(String sku, String name, double price, int stock) {
        if (findBySku(sku).isPresent()) throw new IllegalArgumentException("SKU sudah digunakan.");
        items.add(new InventoryItem(sku, name, price, stock));
    }

    public List<InventoryItem> listItems() {
        return items.stream().sorted(Comparator.comparing(InventoryItem::getSku)).toList();
    }

    public List<InventoryItem> search(String keyword) {
        String query = keyword.toLowerCase();
        return items.stream().filter(item -> item.getSku().toLowerCase().contains(query)
                        || item.getName().toLowerCase().contains(query))
                .sorted(Comparator.comparing(InventoryItem::getName)).toList();
    }

    public Optional<InventoryItem> findBySku(String sku) {
        return items.stream().filter(item -> item.getSku().equalsIgnoreCase(sku.trim())).findFirst();
    }

    public void restock(String sku, int quantity) { findRequired(sku).addStock(quantity); }
    public void sell(String sku, int quantity) { findRequired(sku).reduceStock(quantity); }

    private InventoryItem findRequired(String sku) {
        return findBySku(sku).orElseThrow(() -> new IllegalArgumentException("SKU tidak ditemukan."));
    }
}
