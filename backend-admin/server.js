const express = require('express');
const cors = require('cors');
const path = require('path');
const db = require('./db');

const app = express();
const PORT = process.env.PORT || 3000;
const ADMIN_KEY = process.env.ADMIN_KEY || "ipang123"; // Simple API protection

app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Simple Auth Middleware for mutating endpoints (POST, PUT, DELETE)
const authCheck = (req, res, next) => {
  const token = req.headers['x-admin-key'] || req.query.admin_key;
  if (token === ADMIN_KEY) {
    next();
  } else {
    res.status(401).json({ success: false, error: "Unauthorized: Invalid Admin Key" });
  }
};

// ==================== REST API ENDPOINTS ====================

// Health Check
app.get('/api/health', (req, res) => {
  res.json({ status: "ok", service: "Ipang Kids Quiz API", timestamp: new Date().toISOString() });
});

// Admin Auth Check
app.post('/api/auth/verify', (req, res) => {
  const { key } = req.body;
  if (key === ADMIN_KEY) {
    res.json({ success: true, message: "Admin Key Valid" });
  } else {
    res.status(401).json({ success: false, error: "Admin Key Salah!" });
  }
});

// --- MATH QUIZ ENDPOINTS ---

// GET All Math Questions (For App & Portal)
app.get('/api/math', (req, res) => {
  const { category, difficulty } = req.query;
  let sql = "SELECT * FROM math_questions WHERE 1=1";
  const params = [];

  if (category && category !== 'ALL') {
    sql += " AND category = ?";
    params.push(category);
  }
  if (difficulty && difficulty !== 'ALL') {
    sql += " AND difficulty = ?";
    params.push(difficulty);
  }

  sql += " ORDER BY id DESC";

  db.all(sql, params, (err, rows) => {
    if (err) return res.status(500).json({ success: false, error: err.message });
    
    // Map to App format
    const mapped = rows.map(r => ({
      id: r.id,
      category: r.category,
      difficulty: r.difficulty,
      questionText: r.question_text,
      visualRepresentation: r.visual_rep || "",
      options: [r.option_a, r.option_b, r.option_c, r.option_d],
      correctAnswer: r.correct_answer,
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
    correctAnswer,
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
    correctAnswer,
    explanationTip || "",
    id
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Matematika berhasil diperbarui!" });
  });
});

// DELETE Math Question
app.delete('/api/math/:id', authCheck, (req, res) => {
  const { id } = req.params;
  db.run("DELETE FROM math_questions WHERE id = ?", [id], function(err) {
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
      correctAnswer: r.correct_answer,
      explanationTip: r.explanation_tip || ""
    }));

    res.json({ success: true, count: mapped.length, data: mapped });
  });
});

// POST Add New Reading Question
app.post('/api/reading', authCheck, (req, res) => {
  const { categoryTitle, questionText, emoji, options, correctAnswer, explanationTip } = req.body;

  if (!categoryTitle || !questionText || !options || options.length < 4 || !correctAnswer) {
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
    correctAnswer,
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
    correctAnswer,
    explanationTip || "",
    id
  ], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Membaca berhasil diperbarui!" });
  });
});

// DELETE Reading Question
app.delete('/api/reading/:id', authCheck, (req, res) => {
  const { id } = req.params;
  db.run("DELETE FROM reading_questions WHERE id = ?", [id], function(err) {
    if (err) return res.status(500).json({ success: false, error: err.message });
    if (this.changes === 0) return res.status(404).json({ success: false, error: "Soal tidak ditemukan" });
    res.json({ success: true, message: "Soal Membaca berhasil dihapus!" });
  });
});

// Fallback route for SPA Admin Portal
app.get('*', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`🚀 Ipang Kids Quiz Admin Server running at http://localhost:${PORT}`);
});
