# Tucil2_13524012 - 3D Mesh Voxelizer

## Deskripsi Program

Program ini adalah **3D Mesh Voxelizer** yang mengkonversi model 3D dalam format OBJ menjadi representasi voxel (kotak-kotak 3D). Program menggunakan algoritma **Divide and Conquer** dengan struktur data **Octree** untuk melakukan voxelisasi secara efisien.

### Cara Kerja Algoritma

1. **Divide**: Ruang 3D yang mengelilingi mesh dibagi menjadi 8 sub-kubus (oktant) secara rekursif
2. **Conquer**: Setiap sub-kubus diklasifikasikan sebagai:
   - `OUTSIDE` - Berada di luar mesh (dipangkas)
   - `INTERSECTING` - Berpotongan dengan mesh
3. **Base Case**: Proses berhenti ketika mencapai kedalaman maksimum yang ditentukan

Output program adalah file OBJ baru yang berisi representasi voxel dari model 3D input.

## Struktur Program

```
Tucil2_13524012/
├── src/
│   ├── Main.java          # Entry point program
│   ├── Parser.java        # Parser file OBJ
│   ├── Voxelizer.java     # Algoritma voxelisasi (Divide & Conquer)
│   ├── OctreeNode.java    # Struktur data Octree
│   ├── Exporter.java      # Export hasil ke OBJ
│   ├── Mesh.java          # Model mesh 3D
│   ├── Vertex.java        # Representasi vertex
│   ├── Face.java          # Representasi face/triangle
│   ├── Cube.java          # Representasi kubus
│   ├── Voxel.java         # Representasi voxel
│   └── Statistics.java    # Statistik eksekusi
├── test/                  # Sample file OBJ untuk testing
│   ├── cow.obj
│   ├── line.obj
│   ├── pumpkin.obj
│   └── teapot.obj
├── doc/                   # Dokumentasi
└── README.md
```

## Requirements

- **Java Development Kit (JDK)** versi 8 atau lebih baru
- Sistem operasi: Windows, Linux, atau macOS

### Instalasi Java

**Windows:**
1. Download JDK dari [Oracle](https://www.oracle.com/java/technologies/downloads/) atau [OpenJDK](https://adoptium.net/)
2. Install dan tambahkan ke PATH

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-11-jdk
```

**macOS:**
```bash
brew install openjdk@11
```

Verifikasi instalasi:
```bash
java -version
javac -version
```

## Cara Kompilasi

1. Buka terminal/command prompt
2. Navigasi ke folder `src/`:
   ```bash
   cd src
   ```
3. Kompilasi semua file Java:
   ```bash
   javac *.java
   ```

Atau dari root folder:
```bash
javac src/*.java
```

## Cara Menjalankan Program

### Format Perintah
```bash
java Main <input.obj> <kedalaman_maksimum>
```

### Parameter
| Parameter | Deskripsi |
|-----------|-----------|
| `input.obj` | Path ke file OBJ yang akan di-voxelisasi |
| `kedalaman_maksimum` | Kedalaman maksimum octree (integer >= 0). Semakin tinggi, semakin detail hasilnya |

### Contoh Penggunaan

Dari folder `src/`:
```bash
# Voxelisasi model cow dengan kedalaman 5
java Main ../test/cow.obj 5

# Voxelisasi model teapot dengan kedalaman 7
java Main ../test/teapot.obj 7

# Voxelisasi model pumpkin dengan kedalaman 6
java Main ../test/pumpkin.obj 6
```

### Output Program

Program akan menampilkan:
- Path file input dan output
- Kedalaman maksimum yang digunakan
- Jumlah voxel yang dihasilkan
- Jumlah vertex dan face terbentuk
- Waktu eksekusi
- Statistik node per kedalaman
- Statistik pruning per kedalaman

File output akan disimpan dengan nama `<nama_input>_voxelized.obj` di lokasi yang sama dengan file input.

### Contoh Output
```
[HASIL VOXELISASI]
Input file              : ../test/cow.obj
Output file             : ../test/cow_voxelized.obj
Kedalaman Maksimum      : 5
Jumlah Voxel            : 1234
Jumlah vertex terbentuk : 9872
Jumlah face terbentuk   : 14808
Kedalaman Octree        : 5
Lama waktu eksekusi     : 150 ms
```

## Tips Penggunaan

- **Kedalaman rendah (1-4)**: Hasil kasar, eksekusi cepat
- **Kedalaman menengah (5-7)**: Keseimbangan detail dan performa
- **Kedalaman tinggi (8+)**: Hasil sangat detail, membutuhkan waktu dan memori lebih besar

## Author

| Nama | NIM |
|------|-----|
| Tengku Naufal Saqib | 13524012 |

**Institut Teknologi Bandung**  
Tugas Kecil 2 - IF2211 Strategi Algoritma  
Semester 4 - 2025/2026
