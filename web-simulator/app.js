/* ==========================================
   IP SMART KIDS - MAIN APPLICATION CORE
   ========================================== */

const app = {
  // Global State
  state: {
    stars: 0,
    coins: 0,
    streak: 0,
    level: "BEGINNER", // BEGINNER, EXPLORER, LEARNER, READY_FOR_SCHOOL
    avatar: "🦁",
    theme: "default",
    purchasedItems: ["avatar_lion"],
    equippedItems: ["avatar_lion"],
    sessions: [],
    studyDays: 0,
    lastActiveDate: null,
    totalDuration: 0, // seconds
    dailyChallenge: {
      mathSolved: 0,
      wordsSpelled: 0,
      drawingsCompleted: 0,
      isCompleted: false
    }
  },

  activeScreen: "home",
  sessionStartTimestamp: null,

  // Initialize Simulator
  init() {
    this.loadState();
    this.sessionStartTimestamp = Date.now();
    
    // Add periodic tracker for study time duration
    setInterval(() => {
      if (this.sessionStartTimestamp) {
        const delta = Math.floor((Date.now() - this.sessionStartTimestamp) / 1000);
        this.state.totalDuration += delta;
        this.sessionStartTimestamp = Date.now();
        this.saveState();
      }
    }, 15000); // Save time every 15s

    // UI Render
    this.renderHeader();
    
    // Set theme
    this.applyTheme(this.state.theme);

    // Initial Screen Setup
    this.navigate("home");

    // Initialize submodules
    canvasModule.init();
    gameModule.init();
    rewardsModule.init();
    parentDashboard.init();
  },

  // State Persistence
  saveState() {
    localStorage.setItem("ip_smart_kids_state", JSON.stringify(this.state));
  },

  loadState() {
    const saved = localStorage.getItem("ip_smart_kids_state");
    if (saved) {
      try {
        this.state = JSON.parse(saved);
        // Date check for learning streak
        const todayStr = new Date().toDateString();
        if (this.state.lastActiveDate !== todayStr) {
          if (this.state.lastActiveDate) {
            const lastDate = new Date(this.state.lastActiveDate);
            const diffTime = Math.abs(new Date(todayStr) - lastDate);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            if (diffDays === 1) {
              this.state.streak += 1;
            } else if (diffDays > 1) {
              this.state.streak = 1;
            }
          } else {
            this.state.streak = 1;
          }
          this.state.lastActiveDate = todayStr;
          this.state.studyDays += 1;
          this.saveState();
        }
      } catch (e) {
        console.error("Gagal memuat save state: ", e);
      }
    } else {
      // Default setup
      this.state.lastActiveDate = new Date().toDateString();
      this.state.streak = 1;
      this.state.studyDays = 1;
      this.saveState();
    }
  },

  // Screen Routing
  navigate(screenId) {
    this.playSound("click");
    
    // Hide all screens
    document.querySelectorAll(".app-screen").forEach(scr => {
      scr.classList.remove("active");
    });
    
    // Show target screen
    const target = document.getElementById(`screen-${screenId}`);
    if (target) {
      target.classList.add("active");
      this.activeScreen = screenId;
    }

    // Trigger sub-view updates
    if (screenId === "math") {
      gameModule.startMathGame();
    } else if (screenId === "reading") {
      gameModule.startReadingGame();
    } else if (screenId === "brain") {
      gameModule.startBrainGame();
    } else if (screenId === "rewards") {
      rewardsModule.renderShop();
    } else if (screenId === "parent") {
      parentDashboard.renderStats();
    }

    // Speak title for child orientation
    if (screenId === "home") {
      this.speak("Aplikasi Belajar Pintar");
    } else if (screenId === "math") {
      this.speak("Belajar Matematika");
    } else if (screenId === "reading") {
      this.speak("Belajar Membaca");
    } else if (screenId === "writing") {
      this.speak("Belajar Menulis");
    } else if (screenId === "brain") {
      this.speak("Permainan Otak");
    }
  },

  // Render Stats in Header
  renderHeader() {
    document.getElementById("stat-stars").innerText = this.state.stars;
    document.getElementById("stat-coins").innerText = this.state.coins;
    document.getElementById("stat-streak").innerText = this.state.streak;
    document.getElementById("header-avatar").innerText = this.state.avatar;
    
    // Map Level
    let lvlLabel = "🌱 Beginner";
    if (this.state.level === "EXPLORER") lvlLabel = "🌿 Explorer";
    if (this.state.level === "LEARNER") lvlLabel = "🌳 Learner";
    if (this.state.level === "READY_FOR_SCHOOL") lvlLabel = "🚀 Ready for School";
    document.getElementById("header-level").innerText = lvlLabel;
  },

  // Apply Theme Styling
  applyTheme(themeName) {
    document.body.className = "";
    document.body.classList.add(`theme-${themeName}`);
    this.state.theme = themeName;
    this.saveState();
  },

  // Play child-friendly sound effects
  playSound(type) {
    const audio = document.getElementById(`audio-${type}`);
    if (audio) {
      audio.currentTime = 0;
      audio.play().catch(err => {
        // Autoplay policy blocker catches
        console.log("Audio block bypass: click interface required first");
      });
    }
  },

  // Indonesian Text-to-Speech
  speak(text) {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'id-ID';
      utterance.rate = 0.9; // Slightly slower for kids
      window.speechSynthesis.speak(utterance);
    }
  },

  // Show reward celebration popup
  triggerCelebration(stars, coins, message = "Kamu hebat sekali!") {
    this.playSound("complete");
    this.state.stars += stars;
    this.state.coins += coins;
    
    // Check adaptive level upgrade
    if (this.state.stars > 50 && this.state.level === "BEGINNER") {
      this.state.level = "EXPLORER";
      this.speak("Hebat! Kamu naik level ke Explorer!");
    } else if (this.state.stars > 120 && this.state.level === "EXPLORER") {
      this.state.level = "LEARNER";
      this.speak("Keren! Kamu naik level ke Learner!");
    } else if (this.state.stars > 250 && this.state.level === "LEARNER") {
      this.state.level = "READY_FOR_SCHOOL";
      this.speak("Selamat! Kamu sudah siap masuk Sekolah Dasar!");
    }

    this.saveState();
    this.renderHeader();

    // Setup celebration overlay
    document.getElementById("celebration-message").innerText = message;
    document.getElementById("earned-stars").innerText = stars;
    document.getElementById("earned-coins").innerText = coins;
    
    const overlay = document.getElementById("celebration-overlay");
    overlay.style.display = "flex";
  },

  closeCelebration() {
    this.playSound("click");
    document.getElementById("celebration-overlay").style.display = "none";
  },

  // Log a completed learning session
  logSession(subject, score, total) {
    const session = {
      date: new Date().toLocaleDateString('id-ID'),
      timestamp: Date.now(),
      subject: subject,
      score: score,
      total: total,
      duration: 30 // Approx seconds
    };
    this.state.sessions.push(session);
    
    // Check daily challenge progress
    if (subject === "Matematika") this.state.dailyChallenge.mathSolved += total;
    if (subject === "Membaca") this.state.dailyChallenge.wordsSpelled += total;
    if (subject === "Menulis") this.state.dailyChallenge.drawingsCompleted += 1;
    
    this.checkDailyMissions();
    this.saveState();
  },

  checkDailyMissions() {
    if (this.state.dailyChallenge.isCompleted) return;

    const mathTarget = 5;
    const readingTarget = 2;
    const writingTarget = 1;

    if (this.state.dailyChallenge.mathSolved >= mathTarget &&
        this.state.dailyChallenge.wordsSpelled >= readingTarget &&
        this.state.dailyChallenge.drawingsCompleted >= writingTarget) {
      
      this.state.dailyChallenge.isCompleted = true;
      this.triggerCelebration(20, 20, "Misi Harian Selesai! Kamu sungguh rajin! 🌟");
      document.getElementById("daily-challenge-text").innerText = "Misi Harian Selesai! 🎉 Datang kembali besok.";
    }
  }
};

// Start app on load
window.addEventListener("DOMContentLoaded", () => {
  app.init();
});
