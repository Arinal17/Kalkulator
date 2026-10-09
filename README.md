<div align="center">

# 🧮 Kalkulator & Converter

**Aplikasi desktop berbasis Java Swing dengan kalkulator ilmiah sederhana dan konverter satuan dalam satu jendela.**

![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=openjdk&logoColor=white)
![GUI](https://img.shields.io/badge/GUI-Swing-blue)
![Theme](https://img.shields.io/badge/Theme-Lavender%20%26%20Pink-FFC6ED)
![Status](https://img.shields.io/badge/Status-Selesai-brightgreen)

</div>

---

## 📖 Deskripsi

**Kalkulator & Converter** adalah aplikasi desktop yang dibangun dengan Java Swing. Aplikasi ini punya dua mode yang bisa dipilih lewat tab di bagian atas jendela:

1. **Calculator**: kalkulator dengan operasi dasar, akar, pangkat, persen, dan riwayat perhitungan.
2. **Converter**: konverter satuan untuk 8 kategori, lengkap dengan tombol tukar satuan.

Tampilannya bertema terang dengan latar lavender, tombol berbentuk persegi membulat dengan bayangan tipis, serta aksen pink dan indigo. Setiap tombol punya efek *hover* dan *pressed*. Seluruh antarmuka digambar dengan komponen Swing, jadi tidak butuh library tambahan.

Tujuan aplikasi ini adalah untuk menyediakan kalkulator ilmiah sederhana dan konverter satuan dalam sebuah aplikasi desktop berbasis Java Swing, sekaligus menerapkan konsep pemrograman GUI dan perhitungan presisi dengan BigDecimal.

---

## 🎨 Tampilan

<div align="center">
<table>
  <tr>
    <td align="center"><b>Calculator</b></td>
    <td align="center"><b>Converter</b></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/8f29eab5-798f-4198-89cc-8d01f4fe5b9c" alt="Tampilan mode Calculator" width="300"></td>
    <td><img src="https://github.com/user-attachments/assets/35679d21-7ff6-467b-a6b8-1135d667c785" alt="Tampilan mode Converter" width="300"></td>
  </tr>
</table>
</div>
Tombol dikelompokkan berdasarkan fungsinya, masing-masing dengan warna sendiri:

| Jenis tombol | Warna | Tombol |
|---|---|---|
| Latar jendela | Lavender muda | - |
| Layar | Putih lavender, sudut membulat | - |
| Angka | Putih | `0`-`9`, `.`, `±` |
| Operator | Biru muda, teks indigo | `÷` `×` `−` `+` |
| Fungsi | Ungu muda, teks ungu | `√x` `x²` `x^y` `y√x` |
| Aksi utama | Pink `#FFC6ED` | `=`, `AC`, `⌫`, tab yang sedang aktif |
| Pendukung | Abu muda | `%` |

Di mode **Converter**, tombol `AC` dan tukar satuan `⇅` berwarna pink, sedangkan `⌫` berwarna abu muda.

Simbol `√x`, `x²`, `x^y`, `y√x`, dan ikon hapus `⌫` digambar manual dengan `Graphics2D`, jadi bentuknya mirip kalkulator asli.

---

## ✨ Fitur

### Calculator

| Fitur | Keterangan |
|---|---|
| Operasi dasar | Penjumlahan `+`, pengurangan `-`, perkalian `×`, pembagian `÷` |
| Akar kuadrat `√x` | Menghitung akar kuadrat dari angka di layar |
| Kuadrat `x²` | Mengkuadratkan angka di layar |
| Pangkat `x^y` | Memangkatkan `x` dengan `y` |
| Akar ke-n `y√x` | Akar berderajat `y` dari `x` (akar ganjil dari bilangan negatif tetap valid) |
| Persen `%` | Membagi angka di layar dengan 100 |
| Ubah tanda `±` | Mengganti angka menjadi positif atau negatif |
| Hapus `⌫` / Reset `AC` | Menghapus satu digit atau mengosongkan seluruh kalkulasi |
| Riwayat | Klik badge **🕒 Riwayat** untuk melihat seluruh operasi yang pernah dihitung |
| Rangkaian operasi | Operasi berantai (mis. `2 + 3 × 4`) mengikuti urutan prioritas: pangkat/akar, lalu `×` `÷`, lalu `+` `−`. Ekspresinya tampil di atas layar |
| Penanganan error | Pesan jelas untuk pembagian dengan 0, akar bilangan negatif, hasil terlalu besar, dan pangkat/akar tidak valid |
| Format angka | Dibulatkan ke 12 digit signifikan (menghindari hasil seperti `0.30000000000000004`) dan memakai notasi eksponen untuk angka ekstrem |
| Batas input | Maksimal 15 digit per angka; ukuran font layar menyesuaikan panjang angka |

### Converter

Konverter mendukung 8 kategori dengan banyak satuan:

| Kategori | Satuan |
|---|---|
| **Panjang** | mm, cm, m, km, inci, kaki, yard, mil |
| **Massa** | mg, g, kg, ton, ons (oz), pon (lb) |
| **Suhu** | °C, °F, K |
| **Luas** | cm², m², km², hektar, ft², acre |
| **Volume** | mL, L, m³, galon (US), cangkir (US) |
| **Waktu** | ms, detik, menit, jam, hari, minggu |
| **Kecepatan** | m/s, km/jam, mph, knot |
| **Data** | bit, B, KB, MB, GB, TB |

Hasil konversi diperbarui otomatis setiap kali angka atau satuan berubah. Tombol **⇅** menukar satuan asal dan tujuan. Keypad converter punya tombol `AC`, `±`, `⇅`, angka `0`-`9`, titik desimal, dan `⌫`.

---

## 🛠️ Teknologi

- **Bahasa:** Java (JDK 8 atau lebih baru)
- **GUI:** Java Swing & AWT
- **Layout:** `BorderLayout`, `GridLayout`, `BoxLayout`, `CardLayout`
- **Presisi angka:** `BigDecimal` dengan `MathContext`
- **Komponen kustom:**
  - `RoundButton`: `JButton` yang digambar ulang dengan `paintComponent` untuk sudut membulat, bayangan tipis, dan efek hover/pressed
  - `RoundedComboBox`: `JComboBox` bersudut membulat dengan panah dan popup yang disesuaikan temanya
  - `Glyph`: enum simbol yang digambar manual (`√x`, `x²`, `xʸ`, `ʸ√x`, ikon hapus)

---

## 🚀 Cara Menjalankan

### Prasyarat

Pastikan **JDK 8 atau lebih baru** sudah terpasang. Cek dengan:

```bash
java -version
javac -version
```

### Langkah-langkah

1. **Clone atau unduh** repositori ini, lalu buka foldernya di terminal.

2. **Compile** program:

   ```bash
   javac AppKalkulator.java
   ```

3. **Jalankan** program:

   ```bash
   java AppKalkulator
   ```

> 💡 Bisa juga dibuka lewat IDE seperti IntelliJ IDEA, NetBeans, Eclipse, atau VS Code. Buka file `AppKalkulator.java`, lalu jalankan method `main`.

---

## 📘 Panduan Penggunaan

### Calculator

- **Operasi biasa:** ketik angka, pilih operator, ketik angka berikutnya, lalu tekan `=`.
- **Pangkat (`x^y`):** ketik `x`, tekan `x^y`, ketik `y`, lalu tekan `=`.
  Contoh: `2` → `x^y` → `10` → `=` menghasilkan `1024`.
- **Akar ke-n (`y√x`):** ketik **derajat akar (`y`) terlebih dahulu**, tekan `y√x`, ketik `x`, lalu tekan `=`.
  Contoh: `3` → `y√x` → `27` → `=` menghasilkan `3`.
- **Fungsi `√x` dan `x²`:** langsung diterapkan pada angka di layar tanpa menekan `=`.
- **Riwayat:** klik **🕒 Riwayat** di pojok kanan atas layar untuk membuka daftar seluruh operasi. Badge ini juga menampilkan hasil perhitungan terakhir.

### Converter

1. Pilih tab **Converter**.
2. Pilih **kategori** (mis. Panjang) pada dropdown paling atas.
3. Pilih **satuan asal** dan **satuan tujuan**.
4. Ketik angka lewat keypad, dan hasil langsung muncul di baris bawah.
5. Tekan **⇅** untuk menukar satuan asal dan tujuan.

### Contoh Penggunaan

| Skenario | Langkah | Hasil |
|---|---|---|
| Operasi berantai | `2` `+` `3` `×` `4` `=` | `14` |
| Konversi panjang | Tab Converter → Panjang → km ke m → ketik `2.5` | `2500` |
| Konversi suhu | Suhu → °C ke °F → ketik `100` | `212` |

---

## 🗂️ Struktur Kode

```
AppKalkulator.java
├── AppKalkulator (JFrame)          # Jendela utama, header tab, dan CardLayout
├── Palet warna (konstanta COLOR_*) # Warna tema lavender, pink, dan indigo
├── Glyph (enum)                    # Jenis simbol yang digambar manual
├── RoundButton (inner class)       # Tombol persegi membulat dengan bayangan, hover & pressed
├── RoundedComboBox (inner class)   # Dropdown bersudut membulat
├── Kategori (inner class)          # Data satuan dan logika konversi
├── createCalculatorPanel()         # Tampilan dan keypad kalkulator
├── createConverterPanel()          # Tampilan dan keypad converter
├── tekanTombol() / hitung()        # Logika input dan perhitungan
├── evaluasi()                      # Evaluasi ekspresi dengan urutan prioritas operator
├── formatNumber()                  # Pemformatan angka (12 digit signifikan)
└── tampilkanDaftarRiwayat()        # Dialog riwayat operasi
```

---

## 👥 Anggota Kelompok

**Kelompok 10**

| No | Nama | NPM |
|:--:|---|---|
| 1 | Muhammad Albharaka Putrosandy | 250810701100022 |
| 2 | Siti Salwa Shafina | 250810701100055 |
| 3 | Arinal Haq | 250810701100073 |
| 4 | Nawal Azqia | 250810701100092 |

**Dosen Pengampu:** Maulyanda, S.Tr.Kom., M.Kom (NIP: 199708242024061001)

---

<div align="center">

**Jurusan Informatika**
Fakultas Matematika dan Ilmu Pengetahuan Alam
**Universitas Syiah Kuala**
2026

</div>
