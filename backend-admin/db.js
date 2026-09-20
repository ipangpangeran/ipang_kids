const sqlite3 = require('sqlite3').verbose();
const path = require('path');

const dbPath = path.join(__dirname, 'quiz.db');
const db = new sqlite3.Database(dbPath);

db.serialize(() => {
  // Table Math Questions
  db.run(`
    CREATE TABLE IF NOT EXISTS math_questions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      category TEXT NOT NULL,
      difficulty TEXT NOT NULL,
      question_text TEXT NOT NULL,
      visual_rep TEXT DEFAULT '',
      option_a INTEGER NOT NULL,
      option_b INTEGER NOT NULL,
      option_c INTEGER NOT NULL,
      option_d INTEGER NOT NULL,
      correct_answer INTEGER NOT NULL,
      explanation_tip TEXT DEFAULT '',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Table Reading Questions
  db.run(`
    CREATE TABLE IF NOT EXISTS reading_questions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      category_title TEXT NOT NULL,
      question_text TEXT NOT NULL,
      emoji TEXT DEFAULT '',
      option_a TEXT NOT NULL,
      option_b TEXT NOT NULL,
      option_c TEXT NOT NULL,
      option_d TEXT NOT NULL,
      correct_answer TEXT NOT NULL,
      explanation_tip TEXT DEFAULT '',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Table Brain Questions
  db.run(`
    CREATE TABLE IF NOT EXISTS brain_questions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      category TEXT NOT NULL,
      difficulty TEXT NOT NULL,
      question_text TEXT NOT NULL,
      emoji_set TEXT DEFAULT '',
      option_a TEXT NOT NULL,
      option_b TEXT NOT NULL,
      option_c TEXT NOT NULL,
      option_d TEXT NOT NULL,
      correct_answer TEXT NOT NULL,
      explanation_tip TEXT DEFAULT '',
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Table App Settings
  db.run(`
    CREATE TABLE IF NOT EXISTS app_settings (
      id INTEGER PRIMARY KEY DEFAULT 1,
      header_title TEXT NOT NULL,
      home_title TEXT NOT NULL,
      home_subtitle TEXT NOT NULL
    )
  `);

  // Seed Brain Questions if empty
  db.get("SELECT COUNT(*) as count FROM brain_questions", (err, row) => {
    if (row && row.count === 0) {
      console.log("Seeding initial Brain Questions...");
      const stmt = db.prepare(`
        INSERT INTO brain_questions (category, difficulty, question_text, emoji_set, option_a, option_b, option_c, option_d, correct_answer, explanation_tip)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `);

      const initialBrain = [
        ["MEMORY_MATCH", "EASY", "Pencocokan Pasangan Emoji Hewan 🐱🐶", "🐱,🐶,🐼,🦁", "🐱", "🐶", "🐼", "🦁", "🐱", "Cocokkan pasangan gambar yang sama!"],
        ["PATTERN_SEQUENCE", "EASY", "Lengkapi Pola: 🍎, 🍌, 🍎, ❓", "🍎,🍌,🍎,❓", "🍎", "🍌", "🍇", "🍊", "🍌", "Pola berulang: Apel, Pisang, Apel, Pisang!"],
        ["PATTERN_SEQUENCE", "MEDIUM", "Lengkapi Pola: 🚗, 🚀, 🚗, 🚀, ❓", "🚗,🚀,🚗,🚀,❓", "🚗", "🚀", "✈️", "🚲", "🚗", "Pola berulang: Mobil, Roket, Mobil, Roket, Mobil!"],
        ["SHADOW_MATCH", "EASY", "Hewan mana yang berada di hutan? 🦁", "🦁", "🦁 Singa", "🐟 Ikan", "🐙 Gurita", "🐬 Lumba-lumba", "🦁 Singa", "Singa tinggal di daratan/hutan!"]
      ];

      initialBrain.forEach(q => stmt.run(q));
      stmt.finalize();
    }
  });

  // Seed App Settings if empty
  db.get("SELECT COUNT(*) as count FROM app_settings", (err, row) => {
    if (row && row.count === 0) {
      db.run(`
        INSERT INTO app_settings (id, header_title, home_title, home_subtitle)
        VALUES (1, 'Anka Games 🌟', 'Anka Games', 'Kuis & Mini Games Edukasi Anak')
      `);
    }
  });

  // Check and seed Math Questions if empty
  db.get("SELECT COUNT(*) as count FROM math_questions", (err, row) => {
    if (row && row.count === 0) {
      console.log("Seeding initial Math Questions...");
      const stmt = db.prepare(`
        INSERT INTO math_questions (category, difficulty, question_text, visual_rep, option_a, option_b, option_c, option_d, correct_answer, explanation_tip)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `);

      const initialMath = [
        // ADDITION
        ["ADDITION", "EASY", "2 + 3 = ?", "🍎🍎 + 🍎🍎🍎", 4, 5, 6, 7, 5, "2 ditambahkan 3 menjadi 5"],
        ["ADDITION", "EASY", "4 + 1 = ?", "🐱🐱🐱🐱 + 🐱", 3, 5, 6, 4, 5, "4 ditambahkan 1 menjadi 5"],
        ["ADDITION", "EASY", "3 + 3 = ?", "🌟🌟🌟 + 🌟🌟🌟", 5, 6, 7, 8, 6, "3 ditambahkan 3 menjadi 6"],
        ["ADDITION", "EASY", "5 + 2 = ?", "🎈🎈🎈🎈🎈 + 🎈🎈", 6, 7, 8, 9, 7, ""],
        ["ADDITION", "EASY", "6 + 4 = ?", "⚽ (6) + ⚽ (4)", 9, 10, 11, 8, 10, ""],
        ["ADDITION", "MEDIUM", "12 + 5 = ?", "🔢 Penjumlahan Puluhan", 15, 17, 18, 16, 17, ""],
        ["ADDITION", "MEDIUM", "15 + 10 = ?", "🔢 Penjumlahan Puluhan", 20, 25, 30, 22, 25, ""],
        ["ADDITION", "MEDIUM", "8 + ? = 14", "❓ Cari Angka Rahasia", 5, 6, 7, 8, 6, "14 dikurangi 8 adalah 6"],
        ["ADDITION", "HARD", "Budi punya 8 kelereng. Dito memberi 6 kelereng. Total kelereng Budi?", "🔮 Cerita Matematika", 12, 14, 15, 16, 14, "8 + 6 = 14"],
        ["ADDITION", "HARD", "28 + 35 = ?", "🔢 Penjumlahan 2 Digit", 53, 63, 65, 73, 63, "28 + 35 = (20 + 30) + (8 + 5) = 50 + 13 = 63"],
        ["ADDITION", "HARD", "47 + 38 = ?", "🔢 Penjumlahan 2 Digit", 75, 83, 85, 95, 85, "47 + 38 = 85"],
        ["ADDITION", "HARD", "75 + 45 = ?", "🔢 Penjumlahan Ratusan", 110, 115, 120, 130, 120, "75 + 45 = 120"],
        ["ADDITION", "HARD", "Ayah membeli 24 buah apel dan Ibu membeli 18 buah jeruk. Berapakah jumlah seluruh buah yang dibeli?", "🍎 Cerita Matematika", 38, 40, 42, 44, 42, "24 + 18 = 42 buah"],
        ["ADDITION", "HARD", "Di perpustakaan ada 45 buku cerita dan 37 buku pelajaran. Berapa total buku di perpustakaan?", "📚 Cerita Matematika", 72, 80, 82, 92, 82, "45 + 37 = 82 buku"],

        // SUBTRACTION
        ["SUBTRACTION", "EASY", "5 - 2 = ?", "🍎🍎🍎🍎🍎 - 🍎🍎", 2, 3, 4, 1, 3, ""],
        ["SUBTRACTION", "EASY", "4 - 1 = ?", "🎈🎈🎈🎈 - 🎈", 2, 3, 4, 5, 3, ""],
        ["SUBTRACTION", "EASY", "6 - 3 = ?", "🌟🌟🌟🌟🌟🌟 - 🌟🌟🌟", 2, 3, 4, 5, 3, ""],
        ["SUBTRACTION", "MEDIUM", "15 - 7 = ?", "🔢 Pengurangan Belasan", 7, 8, 9, 6, 8, ""],
        ["SUBTRACTION", "HARD", "Ani punya 20 biskuit. Membagikan 8 biskuit ke teman. Sisa biskuit Ani?", "🍪 Cerita Pengurangan", 10, 11, 12, 14, 12, "20 - 8 = 12"],
        ["SUBTRACTION", "HARD", "50 - 24 = ?", "🔢 Pengurangan Puluhan", 24, 26, 28, 36, 26, "50 - 24 = 26"],
        ["SUBTRACTION", "HARD", "85 - 39 = ?", "🔢 Pengurangan Puluhan", 44, 46, 48, 54, 46, "85 - 39 = 46"],
        ["SUBTRACTION", "HARD", "100 - 45 = ?", "🔢 Pengurangan Ratusan", 45, 50, 55, 65, 55, "100 - 45 = 55"],
        ["SUBTRACTION", "HARD", "Anka mempunyai 60 lembar kertas warna. Setelah dipakai melipat origami 27 lembar, berapa sisa kertas Anka?", "📄 Cerita Pengurangan", 33, 37, 43, 47, 33, "60 - 27 = 33 lembar"],
        ["SUBTRACTION", "HARD", "Pak Tani memanen 75 buah semangka. Sebanyak 28 semangka dijual ke pasar. Berapa sisa semangka Pak Tani?", "🍉 Cerita Pengurangan", 45, 47, 53, 57, 47, "75 - 28 = 47 semangka"],

        // MULTIPLICATION
        ["MULTIPLICATION", "EASY", "2 × 3 = ?", "(🍎🍎) (🍎🍎) (🍎🍎)", 5, 6, 7, 8, 6, ""],
        ["MULTIPLICATION", "EASY", "3 × 3 = ?", "(⭐⭐⭐) (⭐⭐⭐) (⭐⭐⭐)", 6, 8, 9, 12, 9, ""],
        ["MULTIPLICATION", "MEDIUM", "5 × 4 = ?", "🔢 Perkalian 5", 15, 20, 25, 18, 20, ""],
        ["MULTIPLICATION", "HARD", "Ada 4 kotak pensil. Setiap kotak isi 5 pensil. Total pensil?", "✏️ Cerita Perkalian", 15, 20, 25, 18, 20, "4 × 5 = 20"],
        ["MULTIPLICATION", "MEDIUM", "6 × 7 = ?", "🔢 Perkalian 6", 36, 42, 48, 54, 42, "6 × 7 = 42"],
        ["MULTIPLICATION", "MEDIUM", "8 × 4 = ?", "🔢 Perkalian 8", 28, 30, 32, 36, 32, "8 × 4 = 32"],
        ["MULTIPLICATION", "HARD", "7 × 8 = ?", "🔢 Perkalian 7", 48, 54, 56, 64, 56, "7 × 8 = 56"],
        ["MULTIPLICATION", "HARD", "9 × 6 = ?", "🔢 Perkalian 9", 45, 52, 54, 63, 54, "9 × 6 = 54"],
        ["MULTIPLICATION", "HARD", "Ada 5 rak buku. Setiap rak berisi 12 buku. Berapakah jumlah seluruh buku di kelima rak tersebut?", "📚 Cerita Perkalian", 50, 55, 60, 65, 60, "5 × 12 = 60 buku"],

        // DIVISION
        ["DIVISION", "EASY", "6 ÷ 2 = ?", "🍎🍎🍎🍎🍎🍎 ÷ 2 kelompok", 2, 3, 4, 5, 3, ""],
        ["DIVISION", "EASY", "8 ÷ 2 = ?", "🎈 (8) ÷ 2 kelompok", 3, 4, 5, 6, 4, ""],
        ["DIVISION", "MEDIUM", "12 ÷ 3 = ?", "🔢 Pembagian 12", 3, 4, 5, 6, 4, ""],
        ["DIVISION", "HARD", "Pak Guru punya 18 buku dibagikan sama rata ke 3 murid. Setiap murid dapat?", "📚 Cerita Pembagian", 5, 6, 7, 8, 6, "18 ÷ 3 = 6"],
        ["DIVISION", "MEDIUM", "36 ÷ 6 = ?", "🔢 Pembagian 36", 5, 6, 7, 8, 6, "36 ÷ 6 = 6"],
        ["DIVISION", "HARD", "56 ÷ 8 = ?", "🔢 Pembagian 56", 6, 7, 8, 9, 7, "56 ÷ 8 = 7"],
        ["DIVISION", "HARD", "72 ÷ 9 = ?", "🔢 Pembagian 72", 7, 8, 9, 10, 8, "72 ÷ 9 = 8"],
        ["DIVISION", "HARD", "Paman membawa 48 buah jeruk untuk dibagikan sama rata kepada 6 keponakannya. Berapa jeruk yang diterima setiap anak?", "🍊 Cerita Pembagian", 6, 7, 8, 9, 8, "48 ÷ 6 = 8 jeruk"],
        ["ADDITION", "HARD", "Ibu membeli 3 kotak donat. Setiap kotak berisi 6 donat. Jika 4 donat sudah dimakan, berapa sisa donat Ibu?", "🍩 Cerita Dua Langkah", 12, 14, 16, 18, 14, "(3 × 6) - 4 = 18 - 4 = 14 donat"]
      ];

      initialMath.forEach(q => stmt.run(q));
      stmt.finalize();
    }
  });

  // Check and seed Reading Questions if empty
  db.get("SELECT COUNT(*) as count FROM reading_questions", (err, row) => {
    if (row && row.count === 0) {
      console.log("Seeding initial Reading Questions...");
      const stmt = db.prepare(`
        INSERT INTO reading_questions (category_title, question_text, emoji, option_a, option_b, option_c, option_d, correct_answer, explanation_tip)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      `);

      const initialReading = [
        ["Cerita & Pemahaman Kritis", "Dimas menemukan dompet di gerbang sekolah. Dimas tidak mengambil uangnya, melainkan memberikan dompet itu ke Satpam sekolah agar dikembalikan kepada pemiliknya.\n\nPertanyaan: Sikap terpuji apakah yang ditunjukkan Dimas?", "👛", "Jujur dan Amanah", "Penakut", "Pelit", "Pemalas", "Jujur dan Amanah", "Dimas mengembalikan barang yang bukan miliknya"],
        ["Cerita & Pemahaman Kritis", "Tumbuhan hijau memerlukan cahaya matahari, air, dan udara untuk mengolah makanannya. Proses ini menghasilkan oksigen yang dihirup manusia dan hewan.\n\nPertanyaan: Apakah nama proses pembuatan makanan pada tumbuhan hijau?", "🌱", "Fotosintesis", "Respirasi", "Penguapan", "Pencernaan", "Fotosintesis", "Pembuatan makanan pada tumbuhan disebut fotosintesis"],
        ["Cerita & Pemahaman Kritis", "Tumbuhan hijau memerlukan cahaya matahari, air, dan udara untuk mengolah makanannya. Proses ini menghasilkan oksigen yang dihirup manusia dan hewan.\n\nPertanyaan: Gas apakah yang dihasilkan oleh tumbuhan dari proses tersebut?", "🍃", "Oksigen", "Karbondioksida", "Nitrogen", "Asap", "Oksigen", "Tumbuhan menghasilkan oksigen bagi makhluk hidup"],
        ["Cerita & Pemahaman Kritis", "Rani berlatih renang setiap sore tanpa kenal lelah. Walaupun pernah kalah dalam lomba, Rani terus berlatih hingga akhirnya menjadi juara pertama tingkat nasional.\n\nPertanyaan: Pesan moral apakah yang bisa kita petik dari kisah Rani?", "🏆", "Pantang menyerah membawa keberhasilan", "Kekalahan adalah akhir dari segalanya", "Tidak perlu berlatih jika sudah pintar", "Juara diperoleh tanpa kerja keras", "Pantang menyerah membawa keberhasilan", "Kegigihan Rani membuahkan hasil juara"],
        ["Ungkapan & Idiom", "Siswa yang sering membantu temannya tanpa mengharap balasan dikenal sebagai anak yang 'RINGAN TANGAN'.\n\nApa arti ungkapan 'ringan tangan'?", "🤝", "Suka menolong", "Suka memukul", "Tangannya enteng", "Suka mencuri", "Suka menolong", "Ringan tangan artinya suka menolong"],
        ["Ungkapan & Idiom", "Robi menjadi 'BINTANG LAPANGAN' pada pertandingan sepak bola kemarin karena mencetak 3 gol.\n\nApa arti ungkapan 'bintang lapangan'?", "⚽", "Pemain terbaik / paling menonjol", "Bintang yang jatuh di lapangan", "Penonton paling heboh", "Wasit pertandingan", "Pemain terbaik / paling menonjol", "Bintang lapangan adalah pemain yang paling menonjol/hebat"],
        ["Ungkapan & Idiom", "Meskipun dari keluarga kaya dan berprestasi, Sinta tidak pernah 'BESAR KEPALA'.\n\nApa arti ungkapan 'besar kepala'?", "🧠", "Sombong / Angkuh", "Kepalanya berukuran besar", "Pintar", "Penyayang", "Sombong / Angkuh", "Besar kepala artinya sombong"],
        ["Ungkapan & Idiom", "Karena persaingan bisnis yang ketat, toko baju itu akhirnya 'GULUNG TIKAR'.\n\nApa arti ungkapan 'gulung tikar'?", "🏪", "Bangkrut / Tutup usaha", "Merapikan tikar", "Pindah rumah", "Menjual karpet", "Bangkrut / Tutup usaha", "Gulung tikar artinya bangkrut"],
        ["Susun Kalimat SPOK", "Susun kata acak ini menjadi kalimat yang benar:\n\n*membaca - di - Ayah - ruang - koran - tamu*", "📰", "Ayah membaca koran di ruang tamu", "Koran membaca Ayah di ruang tamu", "Di ruang tamu koran membaca Ayah", "Ayah di ruang tamu membaca koran", "Ayah membaca koran di ruang tamu", "Struktur SPOK: Subjek (Ayah) + Predikat (membaca) + Objek (koran) + Keterangan Tempat (di ruang tamu)"],
        ["Susun Kalimat SPOK", "Susun kata acak ini menjadi kalimat yang benar:\n\n*lezat - Ibu - kue - memasak - di - dapur*", "🎂", "Ibu memasak kue lezat di dapur", "Kue lezat memasak Ibu di dapur", "Di dapur kue lezat memasak Ibu", "Ibu di dapur kue lezat memasak", "Ibu memasak kue lezat di dapur", "Subjek (Ibu) + Predikat (memasak) + Objek (kue lezat) + Keterangan (di dapur)"],
        ["Susun Kalimat SPOK", "Susun kata acak ini menjadi kalimat yang benar:\n\n*sepeda - menaiki - adik - ke - baru - sekolah*", "🚲", "Adik menaiki sepeda baru ke sekolah", "Sepeda baru menaiki adik ke sekolah", "Ke sekolah sepeda baru menaiki adik", "Adik ke sekolah sepeda baru menaiki", "Adik menaiki sepeda baru ke sekolah", "Adik (Subjek) + menaiki (Predikat) + sepeda baru (Objek) + ke sekolah (Keterangan)"],
        ["Tata Bahasa & Imbuhan", "Pilihlah kata berimbuhan yang tepat untuk melengkapi kalimat:\n\n'Petugas pemadam kebakaran sedang ___ kobaran api di perumahan.'", "🚒", "memadamkan", "dipadamkan", "pemadam", "terpadam", "memadamkan", "Kata kerja aktif transitif menggunakan imbuhan me-kan"],
        ["Tata Bahasa & Imbuhan", "Pilihlah kata berimbuhan yang tepat:\n\n'Kakak sedang ___ pakaian adik yang robek dengan jarum dan benang.'", "🪡", "menjahit", "penjahit", "dijahitkan", "terjahit", "menjahit", "Tindakan aktif memerlukan kata kerja 'menjahit'"],
        ["Tata Bahasa & Imbuhan", "Manakah bentuk kata baku dalam Bahasa Indonesia yang benar?", "✏️", "Apotek", "Apotik", "Apoteq", "Apotick", "Apotek", "Bentuk baku menurut KBBI adalah Apotek (dengan huruf e)"],
        ["Tata Bahasa & Imbuhan", "Manakah bentuk kata baku dalam Bahasa Indonesia yang benar?", "📜", "Ijazah", "Ijasah", "Izajah", "Idjasah", "Ijazah", "Bentuk baku menurut KBBI adalah Ijazah (menggunakan z)"],
        ["Sinonim Level Tinggi", "Apakah persamaan kata (sinonim) dari kata 'DERMAWAN'?", "🎁", "Suka memberi / Pemurah", "Hemat", "Pelit", "Pemberani", "Suka memberi / Pemurah", "Dermawan berarti pemurah hati atau suka memberi"],
        ["Sinonim Level Tinggi", "Apakah persamaan kata (sinonim) dari kata 'PARAS'?", "✨", "Wajah / Muka", "Pakaian", "Harta", "Suara", "Wajah / Muka", "Paras artinya wajah atau rupa"],
        ["Sinonim Level Tinggi", "Apakah persamaan kata (sinonim) dari kata 'LESTARI'?", "🌿", "Abadi / Kekal", "Cepat rusak", "Sementara", "Musnah", "Abadi / Kekal", "Lestari artinya bertahan/kekal"],
        ["Antonim Level Tinggi", "Apakah lawan kata (antonim) dari kata 'GIGIH'?", "🛡️", "Mudah menyerah / Putus asa", "Tekun", "Semangat", "Kuat", "Mudah menyerah / Putus asa", "Gigih berlawanan dengan mudah menyerah"],
        ["Antonim Level Tinggi", "Apakah lawan kata (antonim) dari kata 'PELIT'?", "💰", "Dermawan", "Kecil", "Ragu", "Takut", "Dermawan", "Pelit berlawanan dengan dermawan"],
        ["Antonim Level Tinggi", "Apakah lawan kata (antonim) dari kata 'TENTRAM'?", "🕊️", "Gelisah / Rusuh", "Damai", "Tenang", "Aman", "Gelisah / Rusuh", "Tentram berlawanan dengan gelisah/rusuh"],
        ["Analisis Paragraf Rumpang", "Bacalah kalimat rumpang berikut:\n\n'Hutan Bakau sangat bermanfaat untuk mencegah ___ pantai dari hantaman gelombang laut.'\n\nKata yang paling tepat untuk mengisi titik-titik adalah...", "🌊", "Erosi / Abrasi", "Banjir", "Tsunami", "Longsor", "Erosi / Abrasi", "Pengikisan pantai oleh gelombang laut disebut abrasi/erosi pantai"],
        ["Analisis Paragraf Rumpang", "Bacalah kalimat berikut:\n\n'Indonesia adalah negara kepulauan yang kaya akan keanekaragaman suku dan budaya, namun tetap bersatu sesuai semboyan ___.'", "🇮🇩", "Bhinneka Tunggal Ika", "Tut Wuri Handayani", "Garuda Pancasila", "Indonesiaraya", "Bhinneka Tunggal Ika", "Semboyan persatuan Indonesia adalah Bhinneka Tunggal Ika"],
        ["Logika & Makna Kata", "Benda ini digunakan untuk menunjuk arah Utara dan Selatan. Benda apakah ini?", "🧭", "Kompas", "Penggaris", "Jam Dinding", "Termometer", "Kompas", "Kompas adalah penunjuk arah mata angin"],
        ["Logika & Makna Kata", "Alat medis yang digunakan oleh dokter untuk mendengarkan detak jantung dan suara napas adalah...", "🩺", "Stetoskop", "Mikroskop", "Teleskop", "Termometer", "Stetoskop", "Dokter menggunakan stetoskop untuk mendengar detak jantung"]
      ];

      initialReading.forEach(q => stmt.run(q));
      stmt.finalize();
    }
  });
});

module.exports = db;
