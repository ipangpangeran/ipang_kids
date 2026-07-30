let adminToken = localStorage.getItem('admin_token') || '';
let currentTab = 'math';
let questionsList = [];

document.addEventListener('DOMContentLoaded', () => {
  initAuth();
  setupEventListeners();
});

// Auth Setup
async function initAuth() {
  const loginModal = document.getElementById('login-modal');
  if (adminToken) {
    try {
      const res = await fetch('/api/auth/verify', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: adminToken })
      });
      const data = await res.json();
      if (data.success) {
        loginModal.classList.remove('active');
        loadQuestions();
        return;
      }
    } catch (_e) {}
  }
  adminToken = '';
  localStorage.removeItem('admin_token');
  loginModal.classList.add('active');
}

function setupEventListeners() {
  // Mobile Sidebar Drawer Toggle
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('sidebar-overlay');
  const mobileToggleBtn = document.getElementById('btn-mobile-toggle');

  function openMobileSidebar() {
    sidebar.classList.add('open');
    overlay.classList.add('active');
  }

  function closeMobileSidebar() {
    sidebar.classList.remove('open');
    overlay.classList.remove('active');
  }

  if (mobileToggleBtn) {
    mobileToggleBtn.addEventListener('click', openMobileSidebar);
  }
  if (overlay) {
    overlay.addEventListener('click', closeMobileSidebar);
  }

  // Login Form Submission (Username + Password)
  document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const usernameInput = document.getElementById('admin-username-input').value.trim();
    const passwordInput = document.getElementById('admin-password-input').value.trim();

    try {
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: usernameInput, password: passwordInput })
      });
      const data = await res.json();

      if (data.success && data.token) {
        adminToken = data.token;
        localStorage.setItem('admin_token', adminToken);
        document.getElementById('login-modal').classList.remove('active');
        showToast('Login Admin Berhasil! 🚀', 'success');
        loadQuestions();
      } else {
        showToast(data.error || 'Username atau Password Salah!', 'error');
      }
    } catch (err) {
      showToast('Gagal terhubung ke server!', 'error');
    }
  });

  // Logout Button
  document.getElementById('btn-logout').addEventListener('click', () => {
    adminToken = '';
    localStorage.removeItem('admin_token');
    document.getElementById('login-modal').classList.add('active');
    closeMobileSidebar();
  });

  // Tab Switch (Math / Reading)
  document.querySelectorAll('.nav-item').forEach(btn => {
    btn.addEventListener('click', (e) => {
      document.querySelectorAll('.nav-item').forEach(b => b.classList.remove('active'));
      e.currentTarget.classList.add('active');

      currentTab = e.currentTarget.dataset.tab;
      document.getElementById('math-filters').style.display = (currentTab === 'math') ? 'flex' : 'none';
      document.getElementById('form-math-fields').style.display = (currentTab === 'math') ? 'block' : 'none';
      document.getElementById('math-visual-group').style.display = (currentTab === 'math') ? 'block' : 'none';
      document.getElementById('form-reading-fields').style.display = (currentTab === 'reading') ? 'block' : 'none';

      document.getElementById('page-title').innerText = (currentTab === 'math') ? 'Management Kuis Matematika' : 'Management Kuis Baca & Tulis';
      closeMobileSidebar();
      loadQuestions();
    });
  });

  // Filter & Search Inputs
  document.getElementById('filter-category').addEventListener('change', filterAndRender);
  document.getElementById('filter-difficulty').addEventListener('change', filterAndRender);
  document.getElementById('search-input').addEventListener('input', filterAndRender);

  // Add Question Button
  document.getElementById('btn-add-question').addEventListener('click', () => {
    openQuestionModal();
  });

  // Close Modal Buttons
  document.getElementById('btn-close-modal').addEventListener('click', closeQuestionModal);
  document.getElementById('btn-cancel-modal').addEventListener('click', closeQuestionModal);

  // Question Form Submit
  document.getElementById('question-form').addEventListener('submit', handleFormSubmit);
}

// Fetch Questions from API
async function loadQuestions() {
  try {
    const url = currentTab === 'math' ? '/api/math' : '/api/reading';
    const res = await fetch(url);
    const result = await res.json();

    if (result.success) {
      questionsList = result.data;
      document.getElementById('stat-total-questions').innerText = questionsList.length;
      filterAndRender();
    }
  } catch (err) {
    showToast('Gagal memuat daftar soal!', 'error');
  }
}

// Filter & Render Cards
function filterAndRender() {
  const search = document.getElementById('search-input').value.toLowerCase();
  const catFilter = document.getElementById('filter-category').value;
  const diffFilter = document.getElementById('filter-difficulty').value;

  const filtered = questionsList.filter(q => {
    const textMatch = q.questionText.toLowerCase().includes(search) || (q.visualRepresentation && q.visualRepresentation.toLowerCase().includes(search));

    if (currentTab === 'math') {
      const catMatch = (catFilter === 'ALL' || q.category === catFilter);
      const diffMatch = (diffFilter === 'ALL' || q.difficulty === diffFilter);
      return textMatch && catMatch && diffMatch;
    }
    return textMatch;
  });

  const grid = document.getElementById('questions-grid');
  grid.innerHTML = '';

  if (filtered.length === 0) {
    grid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: #64748b; padding: 40px;">Tidak ada soal yang ditemukan.</p>`;
    return;
  }

  filtered.forEach(q => {
    const card = document.createElement('div');
    card.className = 'question-card';

    const tagsHtml = currentTab === 'math' 
      ? `<span class="tag tag-category">${q.category}</span><span class="tag tag-difficulty">${q.difficulty}</span>`
      : `<span class="tag tag-category">${q.categoryTitle}</span>`;

    const optionsHtml = q.options.map(opt => {
      const isCorrect = (String(opt) === String(q.correctAnswer));
      return `<div class="opt-chip ${isCorrect ? 'correct' : ''}">${opt} ${isCorrect ? '✓' : ''}</div>`;
    }).join('');

    card.innerHTML = `
      <div>
        <div class="card-tags">${tagsHtml}</div>
        <h3 class="question-title">${q.questionText}</h3>
        ${q.visualRepresentation || q.emoji ? `<div class="question-visual">${q.visualRepresentation || q.emoji}</div>` : ''}
        <div class="options-preview">${optionsHtml}</div>
      </div>
      <div class="card-actions">
        <button class="btn btn-secondary btn-small" onclick="editQuestion(${q.id})">✏️ Edit</button>
        <button class="btn btn-danger btn-small" onclick="deleteQuestion(${q.id})">🗑️ Hapus</button>
      </div>
    `;

    grid.appendChild(card);
  });
}

// Open Question Modal
function openQuestionModal(editData = null) {
  const modal = document.getElementById('question-modal');
  const form = document.getElementById('question-form');
  form.reset();

  document.getElementById('form-type').value = currentTab;
  document.getElementById('modal-title').innerText = editData ? `Edit Soal (${currentTab.toUpperCase()})` : `Tambah Soal Baru (${currentTab.toUpperCase()})`;

  if (editData) {
    document.getElementById('form-question-id').value = editData.id;
    document.getElementById('question-text').value = editData.questionText;
    document.getElementById('opt-a').value = editData.options[0];
    document.getElementById('opt-b').value = editData.options[1];
    document.getElementById('opt-c').value = editData.options[2];
    document.getElementById('opt-d').value = editData.options[3];
    document.getElementById('correct-answer').value = editData.correctAnswer;
    document.getElementById('explanation-tip').value = editData.explanationTip || '';

    if (currentTab === 'math') {
      document.getElementById('math-category').value = editData.category;
      document.getElementById('math-difficulty').value = editData.difficulty;
      document.getElementById('math-visual').value = editData.visualRepresentation || '';
    } else {
      document.getElementById('reading-category-title').value = editData.categoryTitle || '';
      document.getElementById('reading-emoji').value = editData.emoji || '';
    }
  } else {
    document.getElementById('form-question-id').value = '';
  }

  modal.classList.add('active');
}

function closeQuestionModal() {
  document.getElementById('question-modal').classList.remove('active');
}

// Edit Question Trigger
window.editQuestion = function(id) {
  const q = questionsList.find(item => item.id === id);
  if (q) openQuestionModal(q);
};

// Delete Question Trigger
window.deleteQuestion = async function(id) {
  if (!confirm('Apakah Anda yakin ingin menghapus soal ini?')) return;

  try {
    const url = (currentTab === 'math') ? `/api/math/${id}` : `/api/reading/${id}`;
    const res = await fetch(url, {
      method: 'DELETE',
      headers: { 'x-admin-token': adminToken }
    });
    const data = await res.json();

    if (data.success) {
      showToast('Soal berhasil dihapus! 🗑️', 'success');
      loadQuestions();
    } else {
      showToast(data.error || 'Gagal menghapus soal', 'error');
    }
  } catch (err) {
    showToast('Gagal menghapus soal', 'error');
  }
};

// Form Submit (Create / Update)
async function handleFormSubmit(e) {
  e.preventDefault();

  const id = document.getElementById('form-question-id').value;
  const questionText = document.getElementById('question-text').value.trim();
  const optA = document.getElementById('opt-a').value.trim();
  const optB = document.getElementById('opt-b').value.trim();
  const optC = document.getElementById('opt-c').value.trim();
  const optD = document.getElementById('opt-d').value.trim();
  const correctAnswerInput = document.getElementById('correct-answer').value.trim();
  const explanationTip = document.getElementById('explanation-tip').value.trim();

  let body = {
    questionText,
    options: [optA, optB, optC, optD],
    correctAnswer: isNaN(correctAnswerInput) ? correctAnswerInput : Number(correctAnswerInput),
    explanationTip
  };

  if (currentTab === 'math') {
    body.category = document.getElementById('math-category').value;
    body.difficulty = document.getElementById('math-difficulty').value;
    body.visualRepresentation = document.getElementById('math-visual').value.trim();
  } else {
    body.categoryTitle = document.getElementById('reading-category-title').value.trim() || 'Tebak Gambar & Kata';
    body.emoji = document.getElementById('reading-emoji').value.trim();
  }

  try {
    const baseUrl = (currentTab === 'math') ? '/api/math' : '/api/reading';
    const url = id ? `${baseUrl}/${id}` : baseUrl;
    const method = id ? 'PUT' : 'POST';

    const res = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'x-admin-token': adminToken
      },
      body: JSON.stringify(body)
    });

    const data = await res.json();

    if (data.success) {
      showToast(data.message || 'Berhasil menyimpan soal! 🎉', 'success');
      closeQuestionModal();
      loadQuestions();
    } else {
      showToast(data.error || 'Gagal menyimpan soal!', 'error');
    }
  } catch (err) {
    showToast('Terjadi kesalahan koneksi!', 'error');
  }
}

// Toast Notification
function showToast(msg, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerText = msg;
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
  }, 3500);
}
