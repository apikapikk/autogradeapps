public class InventoryItem {
    private final String sku;
    private final String name;
    private final double price;
    private int stock;

    public InventoryItem(String sku, String name, double price, int stock) {
        if (sku == null || sku.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("SKU dan nama barang wajib diisi.");
        }
        if (price < 0 || stock < 0) {
            throw new IllegalArgumentException("Harga dan stok tidak boleh negatif.");
        }
        this.sku = sku.trim().toUpperCase();
        this.name = name.trim();
        this.price = price;
        this.stock = stock;
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void addStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Jumlah stok harus lebih dari 0.");
        stock += quantity;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0 || quantity > stock) {
            throw new IllegalArgumentException("Jumlah stok tidak mencukupi.");
        }
        stock -= quantity;
    }

    @Override
    public String toString() {
        return "%s | %s | Rp%.2f | stok: %d".formatted(sku, name, price, stock);
    }
}
