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

  // Table App Settings
  db.run(`
    CREATE TABLE IF NOT EXISTS app_settings (
      id INTEGER PRIMARY KEY DEFAULT 1,
      header_title TEXT NOT NULL,
      home_title TEXT NOT NULL,
      home_subtitle TEXT NOT NULL
    )
  `);

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

        // SUBTRACTION
        ["SUBTRACTION", "EASY", "5 - 2 = ?", "🍎🍎🍎🍎🍎 - 🍎🍎", 2, 3, 4, 1, 3, ""],
        ["SUBTRACTION", "EASY", "4 - 1 = ?", "🎈🎈🎈🎈 - 🎈", 2, 3, 4, 5, 3, ""],
        ["SUBTRACTION", "EASY", "6 - 3 = ?", "🌟🌟🌟🌟🌟🌟 - 🌟🌟🌟", 2, 3, 4, 5, 3, ""],
        ["SUBTRACTION", "MEDIUM", "15 - 7 = ?", "🔢 Pengurangan Belasan", 7, 8, 9, 6, 8, ""],
        ["SUBTRACTION", "HARD", "Ani punya 20 biskuit. Membagikan 8 biskuit ke teman. Sisa biskuit Ani?", "🍪 Cerita Pengurangan", 10, 11, 12, 14, 12, "20 - 8 = 12"],

        // MULTIPLICATION
        ["MULTIPLICATION", "EASY", "2 × 3 = ?", "(🍎🍎) (🍎🍎) (🍎🍎)", 5, 6, 7, 8, 6, ""],
        ["MULTIPLICATION", "EASY", "3 × 3 = ?", "(⭐⭐⭐) (⭐⭐⭐) (⭐⭐⭐)", 6, 8, 9, 12, 9, ""],
        ["MULTIPLICATION", "MEDIUM", "5 × 4 = ?", "🔢 Perkalian 5", 15, 20, 25, 18, 20, ""],
        ["MULTIPLICATION", "HARD", "Ada 4 kotak pensil. Setiap kotak isi 5 pensil. Total pensil?", "✏️ Cerita Perkalian", 15, 20, 25, 18, 20, "4 × 5 = 20"],

        // DIVISION
        ["DIVISION", "EASY", "6 ÷ 2 = ?", "🍎🍎🍎🍎🍎🍎 ÷ 2 kelompok", 2, 3, 4, 5, 3, ""],
        ["DIVISION", "EASY", "8 ÷ 2 = ?", "🎈 (8) ÷ 2 kelompok", 3, 4, 5, 6, 4, ""],
        ["DIVISION", "MEDIUM", "12 ÷ 3 = ?", "🔢 Pembagian 12", 3, 4, 5, 6, 4, ""],
        ["DIVISION", "HARD", "Pak Guru punya 18 buku dibagikan sama rata ke 3 murid. Setiap murid dapat?", "📚 Cerita Pembagian", 5, 6, 7, 8, 6, "18 ÷ 3 = 6"]
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
        ["Cerita Pendek & Pemahaman", "Ani suka membaca buku di perpustakaan bersama kawan. Pertanyaan: Di mana Ani membaca buku?", "🏫", "Perpustakaan", "Taman", "Pasar", "Pantai", "Perpustakaan", "Di dalam cerita: 'membaca buku di perpustakaan'"]
      ];

      initialReading.forEach(q => stmt.run(q));
      stmt.finalize();
    }
  });
});

module.exports = db;
