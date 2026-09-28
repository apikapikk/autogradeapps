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

## 8. Struktur project yang didukung

Scanner mendukung:

- Maven: `src/main/java`;
- Gradle: `src/main/...`;
- IntelliJ: `src/`;
- folder biasa yang berisi file `.java` di mana pun;
- project yang dibungkus satu folder tambahan, selama source masih berada di dalamnya.

File Java diurutkan sebelum dianalisis. Jika tidak ada source Java, autograder mengembalikan laporan error dengan skor 0 dan tidak crash.

## 9. Menambahkan assignment profile

Assignment baru dibuat dengan langkah berikut:

1. Buat class baru yang mengimplementasikan `AssignmentProfile`.
2. Tentukan `name()`, `targetClassName()`, dan daftar `FunctionalCase`.
3. Daftarkan profile tersebut di `AssignmentRegistry`.
4. Jalankan test dan gunakan nama profile melalui `--assignment`.

Contoh penggunaan setelah profile bernama `calculator` didaftarkan:

```bash
mvn -q compile exec:java \
  -Dexec.args="--batch submissions --assignment calculator"
```

## 10. Troubleshooting

### `Unknown assignment`

Pastikan nama assignment sudah terdaftar di `AssignmentRegistry`. Saat ini profile yang tersedia adalah `payroll`.

### `No Java source files found`

Pastikan path menunjuk ke folder yang benar dan source tidak berada di folder yang diabaikan scanner.

### Functional score 0

Periksa apakah project dapat dikompilasi dengan JDK 21, class target memiliki nama yang benar, method bersifat `static`, dan signature method sesuai profile.

### Tidak ada submission saat batch

`--batch` hanya membaca subfolder langsung di dalam folder yang diberikan. Pastikan setiap student project memiliki folder sendiri.
