# Autograder

Autograder adalah aplikasi command-line untuk menilai project Java mahasiswa. Aplikasi ini dapat:

- mendeteksi source Java dari beberapa struktur project;
- menjalankan pemeriksaan kualitas kode;
- menguji fungsionalitas dengan assignment profile tertentu; dan
- menilai satu project atau banyak submission sekaligus.

## 1. Prasyarat

- Java Development Kit (JDK) 21 atau lebih baru;
- Maven 3.9 atau lebih baru;
- project student yang berisi file `.java`.

Periksa instalasi dengan:

```bash
java -version
mvn -version
```

## 2. Build dan test

Jalankan dari root repository autograder:

```bash
mvn -q test
```

Untuk hanya melakukan compile:

```bash
mvn -q compile
```

## 3. Menjalankan autograder

Format umum perintahnya:

```text
autograder --project <folder-project> [--assignment <nama>]
autograder --batch <folder-submissions> [--assignment <nama>]
```

Karena aplikasi dijalankan melalui Maven, bentuk lengkapnya adalah:

```bash
mvn -q compile exec:java -Dexec.args="--project <folder-project> [--assignment <nama>]"
```

### Menilai satu project

Contoh quality-only mode:

```bash
mvn -q compile exec:java \
  -Dexec.args="--project src/test/resources/fixtures/good"
```

Contoh full mode dengan payroll assignment:

```bash
mvn -q compile exec:java \
  -Dexec.args="--project src/test/resources/fixtures/good --assignment payroll"
```

`--project` harus menunjuk ke folder project mahasiswa, bukan langsung ke file Java.

### Menilai banyak submission

Letakkan setiap project mahasiswa sebagai subfolder terpisah:

```text
submissions/
├── andi/
│   └── src/main/java/...
├── budi/
│   └── src/main/java/...
└── citra/
    └── src/...
```

Kemudian jalankan:

```bash
mvn -q compile exec:java \
  -Dexec.args="--batch submissions --assignment payroll"
```

Autograder mencetak satu laporan untuk setiap folder dan tabel ringkasan di bagian akhir. Jika folder batch kosong, outputnya adalah `no submissions found`.

## 4. Mode penilaian

### Quality-only mode

Mode ini digunakan jika `--assignment` tidak diberikan. Yang dinilai hanya kualitas kode, sehingga dapat digunakan untuk project Java apa pun, bukan hanya payroll.

Nilai kualitas memiliki maksimum 40 poin dan dikonversi ke skala 100:

```text
total = quality / 40 * 100
```

Laporan menampilkan:

```text
FUNCTIONAL: not assessed
```

### Full mode

Mode ini digunakan dengan assignment profile, misalnya `--assignment payroll`:

```text
functional score : maksimal 60 poin
quality score    : maksimal 40 poin
total            : maksimal 100 poin
```

Grade yang digunakan:

| Nilai | Grade |
|---:|:---:|
| 85–100 | A |
| 70–84 | B |
| 55–69 | C |
| 40–54 | D |
| 0–39 | E |

## 5. Pemeriksaan kualitas kode

Autograder memiliki tiga rule bawaan:

| Rule | Severity | Ketentuan |
|---|---|---|
| `LongMethodRule` | MAJOR | Method atau constructor lebih dari 20 baris |
| `DeepNestRule` | MAJOR | Kedalaman nesting lebih dari 3 |
| `NamingConventionRule` | MINOR | Nama type, method, parameter, variable, dan constant tidak sesuai konvensi |

Penalty yang digunakan:

- `MAJOR`: dikurangi 5 poin;
- `MINOR`: dikurangi 2 poin;
- quality score tidak pernah kurang dari 0.

Source yang berada di `target/`, `build/`, `out/`, `.git/`, `.idea/`, `.gradle/`, atau folder test tidak dianalisis.

## 6. Payroll assignment

Profile payroll mencari class berikut di default package:

```java
public class PayrollCalculator {
    static double calculateWorkHours(LocalTime checkIn, LocalTime checkOut);
    static double calculateOvertimeHours(double workHours);
    static int calculateLateMinutes(LocalTime checkIn);
    static double getHourlyRate(String level);
    static double calculateOvertimePay(double overtimeHours, double hourlyRate);
    static double calculateLateDeduction(int lateMinutes, double hourlyRate);
    static long calculateNetPay(String level, LocalTime checkIn, LocalTime checkOut);
    static String formatPayslip(String name, String level,
                                LocalTime checkIn, LocalTime checkOut);
}
```

Functional cases payroll mencakup jam kerja, overtime, keterlambatan, hourly rate, deduction, net pay, dan payslip. Setiap case dijalankan dengan batas waktu 2 detik. Project yang gagal compile atau melempar exception dianggap gagal pada case terkait.

Untuk mencoba fixture yang tersedia:

```bash
mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/good --assignment payroll"
mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/bad --assignment payroll"
mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/broken --assignment payroll"
mvn -q compile exec:java -Dexec.args="--project src/test/resources/fixtures/skeleton --assignment payroll"
```

## 7. Demo cepat

Script demo menyalin fixture `good`, `bad`, dan `broken` ke `submissions/`, menjalankan full mode dan quality-only mode, lalu menghapus salinan tersebut:

```bash
./demo.sh
```

Setelah selesai, folder `submissions/` kembali kosong kecuali `.gitkeep`.

## 8. Memasang dan menjalankan submission mahasiswa

Bagian ini adalah contoh lengkap menggunakan tugas `lala_01234`, yaitu aplikasi Inventory CLI.

### Langkah 1: Siapkan folder submission

Setiap tugas mahasiswa harus berada sebagai subfolder langsung di dalam `submissions/`:

```text
autograder/
└── submissions/
    └── lala_01234/
        ├── pom.xml
        └── src/main/java/
            ├── InventoryCli.java
            ├── InventoryItem.java
            └── InventoryService.java
```

Jika project masih berada di luar repository, salin foldernya ke `submissions/`:

```bash
cp -R /path/ke/lala_01234 submissions/lala_01234
```

Jangan menyalin folder `target/`, `.git/`, atau output build lainnya. Scanner autograder memang mengabaikan folder-folder tersebut, tetapi submission sebaiknya tetap hanya berisi source dan konfigurasi project.

### Langkah 2: Pastikan project mahasiswa dapat dijalankan

Sebelum dinilai, masuk ke folder submission dan jalankan aplikasinya secara mandiri:

```bash
cd submissions/lala_01234
mvn -q compile
mvn -q exec:java
```

Setelah selesai, kembali ke root autograder:

```bash
cd ../..
```

Langkah ini hanya memeriksa bahwa aplikasi mahasiswa dapat dibuild. Proses ini tidak memasang atau mendaftarkan aplikasi tersebut ke source code autograder.

### Langkah 3: Scan satu submission

Jalankan autograder dari root repository:

```bash
mvn -q compile exec:java \
  -Dexec.args="--project submissions/lala_01234"
```

Output untuk `lala_01234` akan menunjukkan bahwa tiga file Java ditemukan dalam layout Maven:

```text
Project    : lala_01234
Detected   : 3 file(s) in maven
FUNCTIONAL: not assessed
Quality score: 40 / 40
TOTAL: 100.0 / 100   GRADE: A
```

### Langkah 4: Scan semua submission

Jika terdapat beberapa folder mahasiswa, gunakan batch mode:

```bash
mvn -q compile exec:java \
  -Dexec.args="--batch submissions"
```

Autograder akan mencetak laporan setiap mahasiswa, kemudian tabel ringkasan:

```text
SUMMARY
student     functional  quality  total  grade
lala_01234  0.0         40       100.0  A
```

### Langkah 5: Pahami mode penilaian untuk Inventory CLI

Mode quality-only tetap tersedia jika `--assignment` tidak diberikan. Setelah `inventory` didaftarkan, Inventory CLI dapat dinilai secara fungsional dengan perintah berikut:

```bash
mvn -q compile exec:java \
  -Dexec.args="--project submissions/lala_01234"
```

Untuk menjalankan functional profile Inventory CLI, gunakan:

```bash
mvn -q compile exec:java \
  -Dexec.args="--project submissions/lala_01234 --assignment inventory"
```

Profile `inventory` menguji behavior service berikut:

- menambah dan membaca barang;
- menolak SKU duplikat;
- mencari berdasarkan SKU atau nama;
- menambah stok;
- mengurangi stok saat penjualan; dan
- menolak operasi stok yang tidak valid.

Contoh hasil yang diharapkan:

```text
Functional score: 60.0 / 60
Quality score: 40 / 40
TOTAL: 100.0 / 100   GRADE: A
```

## 9. Struktur project yang didukung

Scanner mendukung:

- Maven: `src/main/java`;
- Gradle: `src/main/...`;
- IntelliJ: `src/`;
- folder biasa yang berisi file `.java` di mana pun;
- project yang dibungkus satu folder tambahan, selama source masih berada di dalamnya.

File Java diurutkan sebelum dianalisis. Jika tidak ada source Java, autograder mengembalikan laporan error dengan skor 0 dan tidak crash.

## 10. Menambahkan assignment profile

Assignment baru dibuat dengan langkah berikut. Contoh pada bagian ini adalah profile `inventory` untuk submission `lala_01234`.

1. Tentukan class target yang akan diuji. Untuk Inventory CLI, class targetnya adalah `InventoryService`, karena service dapat diuji langsung tanpa bergantung pada input interaktif terminal.
2. Buat `src/main/java/id/autograder/assignment/InventoryProfile.java` yang mengimplementasikan `AssignmentProfile`.
3. Isi `name()` dengan `inventory` dan `targetClassName()` dengan `InventoryService`.
4. Buat daftar `FunctionalCase` dengan nama, bobot, dan assertion behavior yang diharapkan.
5. Daftarkan profile di `AssignmentRegistry`:

   ```java
   Map.of(
       "payroll", new PayrollProfile(),
       "inventory", new InventoryProfile());
   ```

6. Pastikan nama class target dan method yang dipanggil sesuai dengan source submission.
7. Jalankan test project autograder:

   ```bash
   mvn -q test
   ```

8. Jalankan profile pada submission:

   ```bash
   mvn -q compile exec:java \
     -Dexec.args="--project submissions/lala_01234 --assignment inventory"
   ```

Contoh penggunaan setelah profile bernama `calculator` didaftarkan:

```bash
mvn -q compile exec:java \
  -Dexec.args="--batch submissions --assignment calculator"
```

## 11. Contoh submission dengan hasil negatif

Folder `submissions/lily_3211/` adalah salinan Inventory CLI yang sengaja dibuat buruk untuk menguji kemampuan autograder mendeteksi masalah kode dan function yang belum selesai.

Masalah pada submission tersebut antara lain:

- `InventoryService` menjadi god class;
- `listItems()` dan `search()` menghasilkan list kosong;
- `sell()` masih melempar `UnsupportedOperationException`;
- method `report()` memiliki nesting yang dalam;
- method `main()` terlalu panjang;
- banyak nama variable hanya satu huruf.

Jalankan penilaiannya dengan:

```bash
mvn -q compile exec:java \
  -Dexec.args="--project submissions/lily_3211 --assignment inventory"
```

Hasil yang diharapkan:

```text
Functional score: 9.0 / 60
Quality score: 6 / 40
TOTAL: 15.0 / 100   GRADE: E
```

Perbandingan kedua submission dapat dilihat sekaligus melalui batch mode:

```bash
mvn -q compile exec:java \
  -Dexec.args="--batch submissions --assignment inventory"
```

## 12. Troubleshooting

### `Unknown assignment`

Pastikan nama assignment sudah terdaftar di `AssignmentRegistry`. Profile yang tersedia saat ini adalah `payroll` dan `inventory`.

### `No Java source files found`

Pastikan path menunjuk ke folder yang benar dan source tidak berada di folder yang diabaikan scanner.

### Functional score 0

Periksa apakah project dapat dikompilasi dengan JDK 21, class target memiliki nama yang benar, dan method yang dipanggil profile memiliki signature sesuai. Method target tidak harus `static`; profile `inventory` membuat instance `InventoryService`, sedangkan profile `payroll` memanggil method static.

### Tidak ada submission saat batch

`--batch` hanya membaca subfolder langsung di dalam folder yang diberikan. Pastikan setiap student project memiliki folder sendiri.
