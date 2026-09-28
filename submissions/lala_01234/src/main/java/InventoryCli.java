import java.util.List;
import java.util.Scanner;

public class InventoryCli {
    private final InventoryService inventory = new InventoryService();
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) { new InventoryCli().run(); }

    private void run() {
        boolean running = true;
        System.out.println("=== INVENTORY CLI ===");
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try { running = handleChoice(choice); }
            catch (IllegalArgumentException exception) {
                System.out.println("Gagal: " + exception.getMessage());
            }
        }
        System.out.println("Program selesai.");
    }

    private boolean handleChoice(String choice) {
        return switch (choice) {
            case "1" -> addItem();
            case "2" -> showItems();
            case "3" -> searchItems();
            case "4" -> changeStock(true);
            case "5" -> changeStock(false);
            case "0" -> false;
            default -> { System.out.println("Menu tidak tersedia."); yield true; }
        };
    }

    private boolean addItem() {
        inventory.addItem(ask("SKU"), ask("Nama barang"), askDouble("Harga"), askInt("Stok awal"));
        System.out.println("Barang berhasil ditambahkan.");
        return true;
    }

    private boolean showItems() { printResults(inventory.listItems()); return true; }
    private boolean searchItems() { printResults(inventory.search(ask("Kata kunci"))); return true; }

    private boolean changeStock(boolean restock) {
        String sku = ask("SKU");
        int quantity = askInt("Jumlah");
        if (restock) inventory.restock(sku, quantity); else inventory.sell(sku, quantity);
        System.out.println(restock ? "Stok berhasil ditambah." : "Penjualan berhasil dicatat.");
        return true;
    }

    private void printMenu() {
        System.out.println("\n1. Tambah barang\n2. Lihat semua barang\n3. Cari barang");
        System.out.println("4. Restock barang\n5. Jual barang\n0. Keluar");
        System.out.print("Pilih menu: ");
    }

    private void printResults(List<InventoryItem> results) {
        if (results.isEmpty()) { System.out.println("Tidak ada barang."); return; }
        results.forEach(System.out::println);
    }

    private String ask(String label) { System.out.print(label + ": "); return scanner.nextLine().trim(); }
    private int askInt(String label) { return Integer.parseInt(ask(label)); }
    private double askDouble(String label) { return Double.parseDouble(ask(label)); }
}
