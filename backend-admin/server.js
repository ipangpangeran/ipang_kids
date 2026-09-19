const express = require('express');
const cors = require('cors');
const path = require('path');
const db = require('./db');

const app = express();
const PORT = process.env.PORT || 8765;
const ADMIN_USER = process.env.ADMIN_USER || "admin";
const ADMIN_PASS = process.env.ADMIN_PASS || "adminipang123";
const ADMIN_TOKEN = process.env.ADMIN_TOKEN || "ipang_auth_token_8765_secret";

app.use(cors());
app.use(express.json());

// Disable caching for all API responses
app.use('/api', (req, res, next) => {
  res.setHeader('Cache-Control', 'no-store, no-cache, must-revalidate, proxy-revalidate, max-age=0');
  res.setHeader('CDN-Cache-Control', 'no-store');
  res.setHeader('Cloudflare-CDN-Cache-Control', 'no-store');
  res.setHeader('Pragma', 'no-cache');
  res.setHeader('Expires', '0');
  next();
});

app.use(express.static(path.join(__dirname, 'public')));

// Auth Middleware for mutating endpoints (POST, PUT, DELETE)
const authCheck = (req, res, next) => {
  const token = req.headers['x-admin-token'] || req.headers['x-admin-key'] || req.query.admin_key;
  if (token === ADMIN_TOKEN || token === "ipang123") {
    next();
  } else {
    res.status(401).json({ success: false, error: "Unauthorized: Token atau Password Salah!" });
  }
};

// ==================== REST API ENDPOINTS ====================

// Health Check
app.get('/api/health', (req, res) => {
  res.json({ status: "ok", service: "Ipang Kids Quiz API", timestamp: new Date().toISOString() });
});

// Admin Login (Username + Password)
app.post('/api/auth/login', (req, res) => {
  const { username, password } = req.body;
  if (username === ADMIN_USER && password === ADMIN_PASS) {
    res.json({ success: true, message: "Login Berhasil!", token: ADMIN_TOKEN, username: ADMIN_USER });
  } else {
    res.status(401).json({ success: false, error: "Username atau Password Salah!" });
  }
});

// Admin Auth Verify
app.post('/api/auth/verify', (req, res) => {
  const { token, key } = req.body;
  if (token === ADMIN_TOKEN || key === "ipang123") {
    res.json({ success: true, message: "Session Valid" });
  } else {
    res.status(401).json({ success: false, error: "Session Expired" });
  }
});

// --- MATH QUIZ ENDPOINTS ---

// GET All Math Questions
app.get('/api/math', (req, res) => {
  db.all("SELECT * FROM math_questions ORDER BY id DESC", [], (err, rows) => {
    if (err) return res.status(500).json({ success: false, error: err.message });

    const mapped = rows.map(r => ({
      id: r.id,
      category: r.category,
      difficulty: r.difficulty,
      questionText: r.question_text,
      visualRepresentation: r.visual_rep || "",
      options: [r.option_a, r.option_b, r.option_c, r.option_d],
      correctAnswer: isNaN(r.correct_answer) ? r.correct_answer : Number(r.correct_answer),
      explanationTip: r.explanation_tip || ""
    }));

    res.json({ success: true, count: mapped.length, data: mapped });
  });
});

// POST Add New Math Question
app.post('/api/math', authCheck, (req, res) => {
  const { category, difficulty, questionText, visualRepresentation, options, correctAnswer, explanationTip } = req.body;

  if (!category || !difficulty || !questionText || !options || options.length < 4 || correctAnswer === undefined) {
    return res.status(400).json({ success: false, error: "Field tidak lengkap. Mohon isi semua data!" });
  }

  const sql = `
    INSERT INTO math_questions (category, difficulty, question_text, visual_rep, option_a, option_b, option_c, option_d, correct_answer, explanation_tip)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `;

  db.run(sql, [
    category,
    difficulty,
    questionText,
    visualRepresentation || "",
    options[0],
    options[1],
    options[2],
    options[3],
    String(correctAnswer),
    explanationTip || ""
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    res.json({ success: true, id: this.lastID, message: "Soal Matematika berhasil ditambahkan!" });
  });
});

// PUT Update Math Question
app.put('/api/math/:id', authCheck, (req, res) => {
  const { id } = req.params;
  const { category, difficulty, questionText, visualRepresentation, options, correctAnswer, explanationTip } = req.body;

  if (!category || !difficulty || !questionText || !options || options.length < 4 || correctAnswer === undefined) {
    return res.status(400).json({ success: false, error: "Field tidak lengkap. Mohon isi semua data!" });
  }

  const sql = `
    UPDATE math_questions 
    SET category = ?, difficulty = ?, question_text = ?, visual_rep = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_answer = ?, explanation_tip = ?
    WHERE id = ?
  `;

  db.run(sql, [
    category,
    difficulty,
    questionText,
    visualRepresentation || "",
    options[0],
    options[1],
    options[2],
    options[3],
    String(correctAnswer),
    explanationTip || "",
    Number(id)
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Matematika berhasil diperbarui!" });
  });
});

// DELETE Math Question
app.delete('/api/math/:id', authCheck, (req, res) => {
  const { id } = req.params;
  db.run("DELETE FROM math_questions WHERE id = ?", [Number(id)], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Matematika berhasil dihapus!" });
  });
});

// --- READING QUIZ ENDPOINTS ---

// GET All Reading Questions
app.get('/api/reading', (req, res) => {
  db.all("SELECT * FROM reading_questions ORDER BY id DESC", [], (err, rows) => {
    if (err) return res.status(500).json({ success: false, error: err.message });

    const mapped = rows.map(r => ({
      id: r.id,
      categoryTitle: r.category_title,
      questionText: r.question_text,
      emoji: r.emoji || "",
      options: [r.option_a, r.option_b, r.option_c, r.option_d],
      correctAnswer: isNaN(r.correct_answer) ? r.correct_answer : Number(r.correct_answer),
      explanationTip: r.explanation_tip || ""
    }));

    res.json({ success: true, count: mapped.length, data: mapped });
  });
});

// POST Add New Reading Question
app.post('/api/reading', authCheck, (req, res) => {
  const { categoryTitle, questionText, emoji, options, correctAnswer, explanationTip } = req.body;

  if (!categoryTitle || !questionText || !options || options.length < 4 || correctAnswer === undefined) {
    return res.status(400).json({ success: false, error: "Field tidak lengkap. Mohon isi semua data!" });
  }

  const sql = `
    INSERT INTO reading_questions (category_title, question_text, emoji, option_a, option_b, option_c, option_d, correct_answer, explanation_tip)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
  `;

  db.run(sql, [
    categoryTitle,
    questionText,
    emoji || "",
    options[0],
    options[1],
    options[2],
    options[3],
    String(correctAnswer),
    explanationTip || ""
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    res.json({ success: true, id: this.lastID, message: "Soal Membaca berhasil ditambahkan!" });
  });
});

// PUT Update Reading Question
app.put('/api/reading/:id', authCheck, (req, res) => {
  const { id } = req.params;
  const { categoryTitle, questionText, emoji, options, correctAnswer, explanationTip } = req.body;

  if (!categoryTitle || !questionText || !options || options.length < 4 || correctAnswer === undefined) {
    return res.status(400).json({ success: false, error: "Field tidak lengkap. Mohon isi semua data!" });
  }

  const sql = `
    UPDATE reading_questions
    SET category_title = ?, question_text = ?, emoji = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_answer = ?, explanation_tip = ?
    WHERE id = ?
  `;

  db.run(sql, [
    categoryTitle,
    questionText,
    emoji || "",
    options[0],
    options[1],
    options[2],
    options[3],
    String(correctAnswer),
    explanationTip || "",
    Number(id)
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Membaca berhasil diperbarui!" });
  });
});

// DELETE Reading Question
app.delete('/api/reading/:id', authCheck, (req, res) => {
  const { id } = req.params;
  db.run("DELETE FROM reading_questions WHERE id = ?", [Number(id)], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Membaca berhasil dihapus!" });
  });
});

// GET App Settings
app.get('/api/settings', (req, res) => {
  db.get("SELECT header_title, home_title, home_subtitle FROM app_settings WHERE id = 1", (err, row) => {
    if (err) return res.status(500).json({ success: false, error: err.message });
    const data = row || {
      header_title: 'Anka Games 🌟',
      home_title: 'Anka Games',
      home_subtitle: 'Kuis & Mini Games Edukasi Anak'
    };
    res.json({
      success: true,
      data: {
        headerTitle: data.header_title,
        homeTitle: data.home_title,
        homeSubtitle: data.home_subtitle
      }
    });
  });
});

// PUT Update App Settings
app.put('/api/settings', authCheck, (req, res) => {
  const { headerTitle, homeTitle, homeSubtitle } = req.body;
  if (!headerTitle || !homeTitle || !homeSubtitle) {
    return res.status(400).json({ success: false, error: "Field tidak lengkap. Mohon isi semua judul!" });
  }

  const sql = `
    INSERT INTO app_settings (id, header_title, home_title, home_subtitle)
    VALUES (1, ?, ?, ?)
    ON CONFLICT(id) DO UPDATE SET
      header_title = excluded.header_title,
      home_title = excluded.home_title,
      home_subtitle = excluded.home_subtitle
  `;
  db.run(sql, [headerTitle.trim(), homeTitle.trim(), homeSubtitle.trim()], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    res.json({ success: true, message: "Pengaturan Judul Games berhasil diperbarui!" });
  });
});

// GET Brain Games Questions
app.get('/api/brain', (req, res) => {
  db.all("SELECT * FROM brain_questions ORDER BY id DESC", [], (err, rows) => {
    if (err) return res.status(500).json({ success: false, error: err.message });

    const mapped = rows.map(r => ({
      id: r.id,
      category: r.category,
      difficulty: r.difficulty,
      questionText: r.question_text,
      emojiSet: r.emoji_set ? r.emoji_set.split(',').map(s => s.trim()) : [],
      options: [r.option_a, r.option_b, r.option_c, r.option_d],
      correctAnswer: r.correct_answer,
      explanationTip: r.explanation_tip || ""
    }));

    res.json({ success: true, count: mapped.length, data: mapped });
  });
});

// POST Create Brain Games Question
app.post('/api/brain', authCheck, (req, res) => {
  const { category, difficulty, questionText, emojiSet, options, correctAnswer, explanationTip } = req.body;

  if (!category || !difficulty || !questionText || !options || options.length < 4 || !correctAnswer) {
    return res.status(400).json({ success: false, error: "Field tidak lengkap. Mohon isi semua data!" });
  }

  const emojiStr = Array.isArray(emojiSet) ? emojiSet.join(',') : (emojiSet || '');

  const sql = `
    INSERT INTO brain_questions (category, difficulty, question_text, emoji_set, option_a, option_b, option_c, option_d, correct_answer, explanation_tip)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  `;

  db.run(sql, [
    category,
    difficulty,
    questionText,
    emojiStr,
    options[0],
    options[1],
    options[2],
    options[3],
    String(correctAnswer),
    explanationTip || ""
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    res.json({ success: true, id: this.lastID, message: "Soal Brain Games berhasil ditambahkan!" });
  });
});

// PUT Update Brain Games Question
app.put('/api/brain/:id', authCheck, (req, res) => {
  const { id } = req.params;
  const { category, difficulty, questionText, emojiSet, options, correctAnswer, explanationTip } = req.body;

  if (!category || !difficulty || !questionText || !options || options.length < 4 || !correctAnswer) {
    return res.status(400).json({ success: false, error: "Field tidak lengkap. Mohon isi semua data!" });
  }

  const emojiStr = Array.isArray(emojiSet) ? emojiSet.join(',') : (emojiSet || '');

  const sql = `
    UPDATE brain_questions
    SET category = ?, difficulty = ?, question_text = ?, emoji_set = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_answer = ?, explanation_tip = ?
    WHERE id = ?
  `;

  db.run(sql, [
    category,
    difficulty,
    questionText,
    emojiStr,
    options[0],
    options[1],
    options[2],
    options[3],
    String(correctAnswer),
    explanationTip || "",
    Number(id)
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Brain Games berhasil diperbarui!" });
  });
});

// DELETE Brain Games Question
app.delete('/api/brain/:id', authCheck, (req, res) => {
  const { id } = req.params;
  db.run("DELETE FROM brain_questions WHERE id = ?", [Number(id)], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Brain Games berhasil dihapus!" });
  });
});

app.listen(PORT, () => {
  console.log(`🚀 Ipang Kids Quiz Admin Server running at http://localhost:${PORT}`);
});
