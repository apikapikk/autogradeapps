# Inventory CLI - lily_3211

Salinan tugas Inventory CLI yang sengaja belum selesai dan memiliki kualitas kode rendah.

Masalah yang diketahui:

- `InventoryService` menjadi god class yang menangani data, validasi, transaksi, report, dan pencarian.
- `listItems` dan `search` belum diimplementasikan.
- `sell` masih melempar `UnsupportedOperationException`.
- `report` memiliki nesting yang terlalu dalam.
- Banyak nama variabel terlalu pendek dan tidak deskriptif.

Jalankan dengan:

```bash
mvn -q compile exec:java
```
