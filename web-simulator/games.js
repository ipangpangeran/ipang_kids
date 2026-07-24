/* ==========================================
   IP SMART KIDS - GAME ENGINE & MODULES
   ========================================== */

const gameModule = {
  // Current game state
  currentSubject: "", // "math", "reading", "brain"
  mathSubTopic: "addition", // addition, subtraction, multiplication, division, recognition
  readingSubTopic: "syllables", // alphabets, syllables, word_match
  brainSubTopic: "memory",
  
  // Game session stats
  correctAnswers: 0,
  totalQuestions: 0,
  currentQuestionIndex: 0,
  quizAnswersList: [],
  correctValue: null,

  // Memory Game parameters
  memoryCards: [],
  selectedCards: [],
  matchedPairs: 0,

  init() {
    // Initializer stub
  },

  // ==========================================
  // MATHEMATICS GAME GENERATORS
  // ==========================================
  startMathGame() {
    this.currentSubject = "math";
    this.correctAnswers = 0;
    this.totalQuestions = 0;
    this.currentQuestionIndex = 0;

    // Pick topic based on adaptive level
    const level = app.state.level;
    if (level === "BEGINNER") {
      this.mathSubTopic = Math.random() > 0.5 ? "recognition" : "addition";
    } else if (level === "EXPLORER") {
      const topics = ["addition", "subtraction", "recognition"];
      this.mathSubTopic = topics[Math.floor(Math.random() * topics.length)];
    } else {
      // Learner / Ready for SD
      const topics = ["addition", "subtraction", "multiplication", "division", "word_problems"];
      this.mathSubTopic = topics[Math.floor(Math.random() * topics.length)];
    }

    this.generateMathQuestion();
  },

  generateMathQuestion() {
    const ws = document.getElementById("math-workspace");
    const feedback = document.getElementById("math-feedback");
    feedback.innerText = "";
    ws.innerHTML = "";

    const emojiList = ["🍎", "🍌", "🧸", "🚗", "🌟", "🎈", "🐱", "🐶", "🥕"];
    const emoji = emojiList[Math.floor(Math.random() * emojiList.length)];
    
    let questionText = "";
    let visualHtml = "";
    let options = [];
    let correct = 0;

    const level = app.state.level;
    let rangeMax = 10;
    if (level === "EXPLORER") rangeMax = 20;
    if (level === "LEARNER" || level === "READY_FOR_SCHOOL") rangeMax = 50;

    switch (this.mathSubTopic) {
      case "recognition": {
        // Find missing number
        const start = Math.floor(Math.random() * (rangeMax - 5)) + 1;
        const missingIndex = Math.floor(Math.random() * 4);
        correct = start + missingIndex;
        
        questionText = "Angka berapa yang hilang pada urutan ini?";
        visualHtml = `<div class="visual-items-box">`;
        for (let i = 0; i < 4; i++) {
          if (i === missingIndex) {
            visualHtml += `<span class="math-operator">❓</span>`;
          } else {
            visualHtml += `<span style="font-family: 'Fredoka One'; font-size: 40px; margin: 0 10px;">${start + i}</span>`;
          }
        }
        visualHtml += `</div>`;

        options = this.generateOptions(correct, start, start + 6);
        break;
      }

      case "addition": {
        const num1 = Math.floor(Math.random() * (rangeMax / 2)) + 1;
        const num2 = Math.floor(Math.random() * (rangeMax / 2)) + 1;
        correct = num1 + num2;
        
        questionText = "Berapa jumlah benda di bawah ini?";
        
        const visual1 = Array(num1).fill(emoji).join("");
        const visual2 = Array(num2).fill(emoji).join("");
        
        visualHtml = `
          <div class="visual-items-box">
            <span style="font-size: 36px; max-width: 250px; word-break: break-all;">${visual1}</span>
            <span class="math-operator">+</span>
            <span style="font-size: 36px; max-width: 250px; word-break: break-all;">${visual2}</span>
          </div>
        `;
        
        options = this.generateOptions(correct, 2, rangeMax);
        break;
      }

      case "subtraction": {
        const num1 = Math.floor(Math.random() * (rangeMax / 2)) + 5;
        const num2 = Math.floor(Math.random() * (num1 - 2)) + 1;
        correct = num1 - num2;

        questionText = "Kurangi jumlah benda di bawah ini:";
        
        const visual1 = Array(num1).fill(emoji).join("");
        const crossOut = Array(num2).fill("❌").join("");

        visualHtml = `
          <div class="visual-items-box">
            <span style="font-size: 32px; max-width: 250px; word-break: break-all;">${visual1}</span>
            <span class="math-operator">-</span>
            <span style="font-size: 32px; max-width: 250px; word-break: break-all;">${crossOut}</span>
          </div>
        `;

        options = this.generateOptions(correct, 1, rangeMax);
        break;
      }

      case "multiplication": {
        const groups = Math.floor(Math.random() * 3) + 2; // 2 to 4
        const count = Math.floor(Math.random() * 3) + 2; // 2 to 4
        correct = groups * count;

        questionText = `Ada ${groups} keranjang, setiap keranjang berisi ${count} ${emoji}. Berapa total semuanya?`;
        
        let basketsHtml = "";
        for (let i = 0; i < groups; i++) {
          const basketItems = Array(count).fill(emoji).join("");
          basketsHtml += `<div style="border: 3px solid #ffb74d; border-radius: 12px; padding: 10px; margin: 5px; font-size: 24px; background: #fffdf5;">🧺 ${basketItems}</div>`;
        }

        visualHtml = `<div style="display: flex; flex-wrap: wrap; justify-content: center; margin-bottom: 20px;">${basketsHtml}</div>`;
        options = this.generateOptions(correct, 4, 20);
        break;
      }

      case "division": {
        const shares = Math.floor(Math.random() * 3) + 2; // 2, 3, 4 children
        correct = Math.floor(Math.random() * 3) + 2; // candy per share
        const totalItems = shares * correct;

        questionText = `Bagikan ${totalItems} permen 🍬 secara merata kepada ${shares} anak. Berapa permen yang didapat setiap anak?`;
        
        const candies = Array(totalItems).fill("🍬").join(" ");
        visualHtml = `
          <div class="visual-items-box" style="flex-direction: column; font-size: 24px; gap: 8px;">
            <div style="font-size:32px;">${candies}</div>
            <div style="font-size: 40px; margin-top: 10px;">👦👧👧 (Dibagi ke ${shares} anak)</div>
          </div>
        `;

        options = this.generateOptions(correct, 1, 10);
        break;
      }

      case "word_problems": {
        const num1 = Math.floor(Math.random() * 5) + 3;
        const num2 = Math.floor(Math.random() * 4) + 2;
        correct = num1 + num2;

        questionText = `Budi memiliki ${num1} balon 🎈. Ayah memberikan ${num2} balon lagi. Berapa total balon Budi sekarang?`;
        visualHtml = `<div style="font-family: var(--font-fun); font-size: 48px; margin-bottom: 24px;">🎈 + 🎈 = ❓</div>`;
        options = this.generateOptions(correct, 2, 15);
        break;
      }
    }

    this.correctValue = correct;

    // Render Question
    const qTitle = document.createElement("div");
    qTitle.className = "question-title";
    qTitle.innerText = questionText;
    ws.appendChild(qTitle);

    const vBox = document.createElement("div");
    vBox.innerHTML = visualHtml;
    ws.appendChild(vBox);

    // Audio Instruction
    app.speak(questionText);

    // Render Options
    const optGrid = document.createElement("div");
    optGrid.className = "options-grid";
    options.forEach(opt => {
      const btn = document.createElement("button");
      btn.className = "option-btn";
      btn.innerText = opt;
      btn.onclick = () => this.checkMathAnswer(opt, btn);
      optGrid.appendChild(btn);
    });
    ws.appendChild(optGrid);
  },

  generateOptions(correct, min, max) {
    const list = new Set([correct]);
    while (list.size < 4) {
      const rand = Math.floor(Math.random() * (max - min + 1)) + min;
      if (rand >= 0) list.add(rand);
    }
    return Array.from(list).sort((a, b) => a - b);
  },

  checkMathAnswer(chosen, buttonElement) {
    this.totalQuestions++;
    const feedback = document.getElementById("math-feedback");
    
    if (chosen === this.correctValue) {
      this.correctAnswers++;
      app.playSound("success");
      feedback.style.color = "var(--color-success)";
      feedback.innerText = "Hebat! Jawabanmu Benar! 🌟";
      buttonElement.style.backgroundColor = "rgba(46, 204, 113, 0.2)";
      buttonElement.style.borderColor = "var(--color-success)";
      
      // Move to next question or end session
      setTimeout(() => {
        if (this.totalQuestions >= 5) {
          app.logSession("Matematika", this.correctAnswers, this.totalQuestions);
          app.triggerCelebration(this.correctAnswers * 2, this.correctAnswers, "Hebat! Kamu menyelesaikan latihan Matematika!");
          app.navigate("home");
        } else {
          // Adjust subtopic adaptively based on accuracy
          this.generateMathQuestion();
        }
      }, 1500);
    } else {
      app.playSound("fail");
      feedback.style.color = "var(--color-danger)";
      feedback.innerText = "Ayo coba lagi! Kamu pasti bisa! 💪";
      buttonElement.style.backgroundColor = "rgba(231, 76, 60, 0.2)";
      buttonElement.style.borderColor = "var(--color-danger)";
    }
  },

  // ==========================================
  // READING MODULE GENERATORS
  // ==========================================
  startReadingGame() {
    this.currentSubject = "reading";
    this.correctAnswers = 0;
    this.totalQuestions = 0;
    
    const level = app.state.level;
    if (level === "BEGINNER") {
      this.readingSubTopic = "alphabets";
    } else if (level === "EXPLORER") {
      this.readingSubTopic = Math.random() > 0.5 ? "alphabets" : "syllables";
    } else {
      this.readingSubTopic = Math.random() > 0.5 ? "syllables" : "word_match";
    }

    this.generateReadingQuestion();
  },

  generateReadingQuestion() {
    const ws = document.getElementById("reading-workspace");
    const feedback = document.getElementById("reading-feedback");
    feedback.innerText = "";
    ws.innerHTML = "";

    const alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("");
    const syllables = ["BA", "BI", "BU", "BE", "BO", "CA", "CI", "CU", "CE", "CO", "DA", "DI", "DU", "DE", "DO", "KA", "KI", "KU", "KE", "KO", "MA", "MI", "MU", "ME", "MO", "PA", "PI", "PU", "PE", "PO"];
    const wordList = [
      { word: "BABI", syllables: ["BA", "BI"], emoji: "🐷", translation: "Babi" },
      { word: "KAKI", syllables: ["KA", "KI"], emoji: "🦶", translation: "Kaki" },
      { word: "MATA", syllables: ["MA", "TA"], emoji: "👁️", translation: "Mata" },
      { word: "TOPI", syllables: ["TO", "PI"], emoji: "🤠", translation: "Topi" },
      { word: "BOLA", syllables: ["BO", "LA"], emoji: "⚽", translation: "Bola" },
      { word: "BUKU", syllables: ["BU", "KU"], emoji: "📖", translation: "Buku" },
      { word: "ROTI", syllables: ["RO", "TI"], emoji: "🍞", translation: "Roti" }
    ];

    switch (this.readingSubTopic) {
      case "alphabets": {
        // Alphabet Phonics/Sound Match
        const correctChar = alphabets[Math.floor(Math.random() * alphabets.length)];
        this.correctValue = correctChar;

        const qTitle = document.createElement("div");
        qTitle.className = "question-title";
        qTitle.innerText = "Tekan kartu suara, lalu pilih huruf yang sesuai!";
        ws.appendChild(qTitle);

        // Sound Card
        const soundCard = document.createElement("div");
        soundCard.className = "word-card";
        soundCard.innerHTML = "🔊 Putar Suara";
        soundCard.onclick = () => {
          app.playSound("click");
          app.speak(correctChar);
        };
        ws.appendChild(soundCard);
        
        // Auto trigger speak
        setTimeout(() => app.speak(correctChar), 500);

        const options = this.generateReadingOptions(correctChar, alphabets);
        const optGrid = document.createElement("div");
        optGrid.className = "options-grid";
        options.forEach(opt => {
          const btn = document.createElement("button");
          btn.className = "option-btn";
          btn.innerText = opt;
          btn.onclick = () => this.checkReadingAnswer(opt, btn);
          optGrid.appendChild(btn);
        });
        ws.appendChild(optGrid);
        break;
      }

      case "syllables": {
        // Syllable Matching
        const correctSyllable = syllables[Math.floor(Math.random() * syllables.length)];
        this.correctValue = correctSyllable;

        const qTitle = document.createElement("div");
        qTitle.className = "question-title";
        qTitle.innerText = `Pilihlah suku kata yang berbunyi: "${correctSyllable}"`;
        ws.appendChild(qTitle);

        const soundCard = document.createElement("div");
        soundCard.className = "word-card";
        soundCard.innerHTML = `🔊 "${correctSyllable}"`;
        soundCard.onclick = () => {
          app.playSound("click");
          app.speak(correctSyllable);
        };
        ws.appendChild(soundCard);

        setTimeout(() => app.speak(correctSyllable), 500);

        const options = this.generateReadingOptions(correctSyllable, syllables);
        const optGrid = document.createElement("div");
        optGrid.className = "options-grid";
        options.forEach(opt => {
          const btn = document.createElement("button");
          btn.className = "option-btn";
          btn.innerText = opt;
          btn.onclick = () => this.checkReadingAnswer(opt, btn);
          optGrid.appendChild(btn);
        });
        ws.appendChild(optGrid);
        break;
      }

      case "word_match": {
        // Word Picture Match
        const currentItem = wordList[Math.floor(Math.random() * wordList.length)];
        this.correctValue = currentItem.word;

        const qTitle = document.createElement("div");
        qTitle.className = "question-title";
        qTitle.innerText = `Pilih kata yang mewakili gambar di bawah!`;
        ws.appendChild(qTitle);

        // Render Image/Emoji
        const imgCard = document.createElement("div");
        imgCard.style.fontSize = "90px";
        imgCard.style.margin = "20px 0";
        imgCard.innerText = currentItem.emoji;
        imgCard.onclick = () => {
          app.speak(currentItem.translation);
        };
        ws.appendChild(imgCard);

        setTimeout(() => app.speak("Tebak gambar ini"), 500);

        const options = this.generateReadingOptions(currentItem.word, wordList.map(item => item.word));
        const optGrid = document.createElement("div");
        optGrid.className = "options-grid";
        options.forEach(opt => {
          const btn = document.createElement("button");
          btn.className = "option-btn";
          btn.style.fontSize = "22px";
          btn.innerText = opt;
          btn.onclick = () => this.checkReadingAnswer(opt, btn);
          optGrid.appendChild(btn);
        });
        ws.appendChild(optGrid);
        break;
      }
    }
  },

  generateReadingOptions(correct, fullList) {
    const list = new Set([correct]);
    while (list.size < 4) {
      const rand = fullList[Math.floor(Math.random() * fullList.length)];
      list.add(rand);
    }
    return Array.from(list).sort();
  },

  checkReadingAnswer(chosen, btn) {
    this.totalQuestions++;
    const feedback = document.getElementById("reading-feedback");

    if (chosen === this.correctValue) {
      this.correctAnswers++;
      app.playSound("success");
      feedback.style.color = "var(--color-success)";
      feedback.innerText = "Hebat! Jawabanmu Benar! 🌟";
      btn.style.backgroundColor = "rgba(46, 204, 113, 0.2)";
      btn.style.borderColor = "var(--color-success)";
      
      // Auto speak the word/letter sound for positive feedback
      app.speak(chosen);

      setTimeout(() => {
        if (this.totalQuestions >= 5) {
          app.logSession("Membaca", this.correctAnswers, this.totalQuestions);
          app.triggerCelebration(this.correctAnswers * 2, this.correctAnswers, "Bagus sekali! Kemampuan membacamu meningkat!");
          app.navigate("home");
        } else {
          this.generateReadingQuestion();
        }
      }, 1500);
    } else {
      app.playSound("fail");
      feedback.style.color = "var(--color-danger)";
      feedback.innerText = "Yuk, coba lagi! Kamu pasti bisa! 💪";
      btn.style.backgroundColor = "rgba(231, 76, 60, 0.2)";
      btn.style.borderColor = "var(--color-danger)";
    }
  },

  // ==========================================
  // BRAIN GAMES MODULE (MEMORY MATCH)
  // ==========================================
  startBrainGame() {
    this.currentSubject = "brain";
    this.correctAnswers = 0;
    this.totalQuestions = 0;
    this.matchedPairs = 0;
    this.selectedCards = [];

    this.generateBrainGame();
  },

  generateBrainGame() {
    const ws = document.getElementById("brain-workspace");
    const feedback = document.getElementById("brain-feedback");
    feedback.innerText = "";
    ws.innerHTML = "";

    const qTitle = document.createElement("div");
    qTitle.className = "question-title";
    qTitle.innerText = "Temukan pasangan kartu gambar yang sama! 🧠";
    ws.appendChild(qTitle);

    // Dynamic grid based on level
    const level = app.state.level;
    let cardCount = 4; // 2x2
    if (level === "EXPLORER") cardCount = 6; // 3x2
    if (level === "LEARNER" || level === "READY_FOR_SCHOOL") cardCount = 8; // 4x2

    const emojisPool = ["🦄", "🦊", "🦕", "🐹", "🐝", "🦀", "🐬", "🐞", "🦁", "🐼"];
    
    // Choose cards
    const gameEmojis = [];
    const chosenEmojis = emojisPool.sort(() => 0.5 - Math.random()).slice(0, cardCount / 2);
    
    // Double them
    chosenEmojis.forEach(emoji => {
      gameEmojis.push(emoji);
      gameEmojis.push(emoji);
    });

    // Shuffle
    this.memoryCards = gameEmojis.sort(() => 0.5 - Math.random()).map((emoji, index) => ({
      id: index,
      emoji: emoji,
      flipped: false,
      matched: false
    }));

    // Grid Container
    const grid = document.createElement("div");
    grid.className = "memory-grid";
    
    // Adjust Columns dynamically
    if (cardCount === 4) grid.style.gridTemplateColumns = "repeat(2, 1fr)";
    else if (cardCount === 6) grid.style.gridTemplateColumns = "repeat(3, 1fr)";
    else grid.style.gridTemplateColumns = "repeat(4, 1fr)";

    this.memoryCards.forEach(card => {
      const cardEl = document.createElement("div");
      cardEl.className = "memory-card";
      cardEl.innerText = "❓";
      cardEl.onclick = () => this.flipCard(card, cardEl);
      grid.appendChild(cardEl);
    });

    ws.appendChild(grid);
  },

  flipCard(card, element) {
    if (card.flipped || card.matched || this.selectedCards.length >= 2) return;

    app.playSound("click");
    card.flipped = true;
    element.innerText = card.emoji;
    element.classList.add("flipped");
    this.selectedCards.push({ card, element });

    if (this.selectedCards.length === 2) {
      const first = this.selectedCards[0];
      const second = this.selectedCards[1];

      if (first.card.emoji === second.card.emoji) {
        // Match found
        first.card.matched = true;
        second.card.matched = true;
        this.matchedPairs++;
        this.selectedCards = [];
        app.playSound("success");

        if (this.matchedPairs === this.memoryCards.length / 2) {
          setTimeout(() => {
            app.logSession("Brain Games", 1, 1);
            app.triggerCelebration(8, 8, "Luar biasa! Daya ingatmu sangat hebat! 🧠✨");
            app.navigate("home");
          }, 1000);
        }
      } else {
        // No match
        setTimeout(() => {
          app.playSound("fail");
          first.card.flipped = false;
          second.card.flipped = false;
          first.element.innerText = "❓";
          second.element.innerText = "❓";
          first.element.classList.remove("flipped");
          second.element.classList.remove("flipped");
          this.selectedCards = [];
        }, 1200);
      }
    }
  }
};
