import java.util.Scanner;

public class InventoryCli {
    public static void main(String[] args) {
        InventoryService x = new InventoryService();
        Scanner s = new Scanner(System.in);
        boolean b = true;
        while (b) {
            System.out.println("1 Add | 2 List | 3 Search | 4 Restock | 5 Sell | 0 Exit");
            String c = s.nextLine();
            try {
                if (c.equals("1")) {
                    System.out.print("SKU: "); String a = s.nextLine();
                    System.out.print("Name: "); String n = s.nextLine();
                    System.out.print("Price: "); double p = Double.parseDouble(s.nextLine());
                    System.out.print("Stock: "); int q = Integer.parseInt(s.nextLine());
                    x.addItem(a, n, p, q);
                } else if (c.equals("2")) {
                    System.out.println(x.report());
                } else if (c.equals("3")) {
                    System.out.println(x.search(s.nextLine()));
                } else if (c.equals("4")) {
                    x.restock(s.nextLine(), Integer.parseInt(s.nextLine()));
                } else if (c.equals("5")) {
                    x.sell(s.nextLine(), Integer.parseInt(s.nextLine()));
                } else if (c.equals("0")) {
                    b = false;
                } else {
                    System.out.println("wrong menu");
                }
            } catch (Exception e) {
                System.out.println("error " + e.getMessage());
            }
        }
    }
}
