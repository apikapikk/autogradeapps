# Inventory CLI - lala_01234

Aplikasi command-line sederhana untuk mengelola inventaris barang.

## Fitur

- Menambah barang baru.
- Melihat semua barang berdasarkan SKU.
- Mencari barang berdasarkan SKU atau nama.
- Menambah stok.
- Mencatat penjualan dan mengurangi stok.
- Validasi SKU duplikat, harga, stok, dan jumlah penjualan.

## Menjalankan aplikasi

Gunakan Java 21 dan Maven:

```bash
mvn -q compile exec:java
```

Contoh alur input:

```text
1
BRG-001
Keyboard
250000
10
2
4
BRG-001
5
5
BRG-001
2
0
```
