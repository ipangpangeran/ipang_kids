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
        ["Tebak Gambar & Kata", "Gambar apakah ini?", "🍎", "Apel", "Jeruk", "Pisang", "Nanas", "Apel", "A-P-E-L mengeja Apel"],
        ["Lengkapi Suku Kata", "Lengkapi suku kata: KA - ___", "🦶", "KI", "KU", "KE", "KO", "KI", "KA + KI menjadi KAKI"],
        ["Tebak Gambar & Kata", "Pilih kata yang sesuai dengan gambar ini:", "⚽", "Bola", "Buku", "Baju", "Batu", "Bola", "BO + LA menjadi BOLA"],
        ["Lengkapi Suku Kata", "Lengkapi kata: MA - ___", "👩", "MA", "PA", "TA", "SA", "MA", "MA + MA menjadi MAMA"],
        ["Lengkapi Suku Kata", "Lengkapi suku kata: SU - ___", "🥛", "SU", "SA", "SI", "SO", "SU", "SU + SU menjadi SUSU"],
        ["Mengenal Huruf Depan", "Manakah kata yang diawali dengan huruf 'K'?", "🐱", "Kucing", "Gajah", "Sapi", "Babi", "Kucing", "K-U-C-I-N-G berawalan huruf K"],
        ["Mengenal Huruf Depan", "Manakah kata yang diawali dengan huruf 'B'?", "📚", "Buku", "Topi", "Meja", "Pintu", "Buku", "B-U-K-U berawalan huruf B"],
        ["Tebak Gambar & Kata", "Gambar hewan apakah ini?", "🐘", "Gajah", "Jerapah", "Kuda", "Sapi", "Gajah", "G-A-J-A-H mengeja Gajah"],
        ["Lengkapi Suku Kata", "Lengkapi suku kata: PI - ___", "🍌", "SANG", "KIR", "PA", "TA", "SANG", "PI + SANG menjadi PISANG"],
        ["Lengkapi Suku Kata", "Lengkapi suku kata: MO - ___", "🚗", "BIL", "TOR", "TAR", "PA", "BIL", "MO + BIL menjadi MOBIL"],
        ["Lengkapi Suku Kata", "Lengkapi suku kata: TO - ___", "🧢", "PI", "PA", "PU", "PE", "PI", "TO + PI menjadi TOPI"],
        ["Lengkapi Suku Kata", "Lengkapi suku kata: KU - ___", "🐴", "DA", "DU", "DI", "DO", "DA", "KU + DA menjadi KUDA"],
        ["Cerita Pendek & Pemahaman", "Kucing Budi berwarna putih. Kucing Budi sangat suka minum susu. Pertanyaan: Apa warna kucing Budi?", "🐱", "Putih", "Hitam", "Cokelat", "Kuning", "Putih", "Di dalam cerita tertulis: 'Kucing Budi berwarna putih'"],
        ["Cerita Pendek & Pemahaman", "Budi pergi ke sekolah naik sepeda warna merah. Pertanyaan: Apa warna sepeda Budi?", "🚲", "Merah", "Biru", "Hijau", "Kuning", "Merah", "Di dalam cerita tertulis: 'sepeda warna merah'"],
        ["Cerita Pendek & Pemahaman", "Ani suka membaca buku di perpustakaan bersama kawan. Pertanyaan: Di mana Ani membaca buku?", "🏫", "Perpustakaan", "Taman", "Pasar", "Pantai", "Perpustakaan", "Di dalam cerita: 'membaca buku di perpustakaan'"],
        ["Lengkapi Suku Kata", "Lengkapi suku kata: PER - PUS - TA - ___ - AN", "📚", "KA", "KI", "KU", "KO", "KA", "Perpustakaan"],
        ["Lengkapi Suku Kata", "Lengkapi kata: MEN - JA - ___", "🪡", "HIT", "HAT", "HUT", "HOT", "HIT", "MENJAHIT"],
        ["Lengkapi Suku Kata", "Lengkapi kata: PEM - BA - NGU - ___", "🏗️", "NAN", "NA", "NUN", "NI", "NAN", "PEMBANGUNAN"],
        ["Lengkapi Suku Kata", "Lengkapi kata: BER - SE - PE - ___", "🚲", "DA", "DU", "DI", "DO", "DA", "BERSEPEDA"],
        ["Lawan Kata (Antonim)", "Apakah lawan kata (antonim) dari kata 'RAJIN'?", "💡", "Pintar", "Malas", "Tekun", "Semangat", "Malas", "Lawan kata rajin adalah malas"],
        ["Lawan Kata (Antonim)", "Apakah lawan kata dari 'GELAP'?", "☀️", "Malam", "Terang", "Hitam", "Kelabu", "Terang", "Lawan kata gelap adalah terang"],
        ["Lawan Kata (Antonim)", "Apakah lawan kata dari 'TINGGI'?", "📏", "Panjang", "Pendek", "Kecil", "Besar", "Pendek", "Lawan kata tinggi adalah pendek"],
        ["Lawan Kata (Antonim)", "Apakah lawan kata dari 'BERSIH'?", "🧹", "Wangi", "Kotor", "Rapi", "Indah", "Kotor", "Lawan kata bersih adalah kotor"],
        ["Lengkapi Kalimat", "Sebelum makan, kita sebaiknya mencuci ___ agar terhindar dari kuman.", "🧼", "Tangan", "Piring", "Baju", "Kaki", "Tangan", "Mencuci tangan sebelum makan"],
        ["Lengkapi Kalimat", "Matahari terbit di sebelah ___ pada pagi hari.", "🌅", "Barat", "Timur", "Utara", "Selatan", "Timur", "Matahari terbit di sebelah timur"],
        ["Lengkapi Kalimat", "Nelayan pergi ke laut untuk menangkap ___.", "🐟", "Burung", "Ikan", "Ayam", "Kambing", "Ikan", "Nelayan menangkap ikan"],
        ["Lengkapi Kalimat", "Reno menyiram tanaman di halaman rumah agar tidak ___.", "🌻", "Layu", "Tumbuh", "Subur", "Mekar", "Layu", "Menyiram tanaman agar tidak layu"],
        ["Menyusun Huruf", "Susunlah huruf acak ini menjadi kata yang benar: S - E - K - O - L - A - H", "🏫", "Sekolah", "Kelasa", "Solehah", "Kosalah", "Sekolah", "S-E-K-O-L-A-H = Sekolah"],
        ["Menyusun Huruf", "Susunlah huruf acak ini menjadi kata yang benar: K - E - L - I - N - C - I", "🐇", "Kelinci", "Kancil", "Kucing", "Kancin", "Kelinci", "K-E-L-I-N-C-I = Kelinci"],
        ["Menyusun Huruf", "Susunlah huruf acak ini menjadi kata yang benar: P - E - L - A - N - G - I", "🌈", "Pelangi", "Pengali", "Palingan", "Pelangian", "Pelangi", "P-E-L-A-N-G-I = Pelangi"],
        ["Cerita Pendek & Pemahaman", "Setiap hari Minggu, Dito membantu Ayah membersihkan akuarium. Mereka mengeluarkan air keruh dan menggantinya dengan air bersih. Koki, ikan komet kesayangan Dito, berenang dengan gembira.\n\nPertanyaan: Kapan Dito membantu Ayah membersihkan akuarium?", "🐠", "Hari Senin", "Hari Sabtu", "Hari Minggu", "Hari Jumat", "Hari Minggu", "Tercantum pada awal cerita: 'Setiap hari Minggu...'"],
        ["Cerita Pendek & Pemahaman", "Setiap hari Minggu, Dito membantu Ayah membersihkan akuarium. Mereka mengeluarkan air keruh dan menggantinya dengan air bersih. Koki, ikan komet kesayangan Dito, berenang dengan gembira.\n\nPertanyaan: Siapa nama ikan komet kesayangan Dito?", "🐟", "Nemo", "Koki", "Dory", "Bubu", "Koki", "Tercantum pada cerita: 'Koki, ikan komet kesayangan Dito...'"],
        ["Cerita Pendek & Pemahaman", "Nina suka menanam bunga mawar di kebun belakang rumah. Nina rajin menyiram bunganya setiap pagi dan sore hari. Bunga mawar Nina tumbuh mekar dan harum semerbak.\n\nPertanyaan: Mengapa bunga mawar Nina tumbuh mekar dan harum?", "🌹", "Karena diberi es krim", "Karena dipetik setiap hari", "Karena rajin disiram pagi dan sore", "Karena disimpan di dalam kamar", "Karena rajin disiram pagi dan sore", "Nina rajin menyiram bunganya setiap pagi dan sore hari"],
        ["Cerita Pendek & Pemahaman", "Lani dan kawan-kawannya pergi ke museum sains. Di museum, mereka melihat robot cerdas dan planetarium. Lani sangat kagum melihat bintang-bintang di planetarium.\n\nPertanyaan: Di mana Lani melihat bintang-bintang?", "🌌", "Di lapangan", "Di planetarium", "Di taman bunga", "Di tepi pantai", "Di planetarium", "Lani sangat kagum melihat bintang-bintang di planetarium"]
      ];

      initialReading.forEach(q => stmt.run(q));
      stmt.finalize();
    }
  });
});

module.exports = db;
