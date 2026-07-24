/* ==========================================
   IP SMART KIDS - PARENT DASHBOARD & REWARDS
   ========================================== */

const parentDashboard = {
  correctPin: "1234",

  init() {
    // Initializer
  },

  openPinModal() {
    app.playSound("click");
    document.getElementById("parent-pin-input").value = "";
    document.getElementById("pin-error-msg").style.display = "none";
    document.getElementById("pin-modal").style.display = "flex";
  },

  closePinModal() {
    app.playSound("click");
    document.getElementById("pin-modal").style.display = "none";
  },

  submitPin() {
    const pin = document.getElementById("parent-pin-input").value;
    if (pin === this.correctPin) {
      document.getElementById("pin-modal").style.display = "none";
      app.navigate("parent");
    } else {
      app.playSound("fail");
      document.getElementById("pin-error-msg").style.display = "block";
      document.getElementById("parent-pin-input").value = "";
    }
  },

  renderStats() {
    const sessions = app.state.sessions;
    
    // 1. Calculations
    const studyDays = app.state.studyDays || 1;
    const totalDurationMin = Math.round((app.state.totalDuration || 0) / 60);
    
    let accuracy = 0;
    let totalQs = 0;
    let correctQs = 0;
    
    // Subject stats
    const subjects = {
      "Matematika": { correct: 0, total: 0 },
      "Membaca": { correct: 0, total: 0 },
      "Menulis": { correct: 0, total: 0 },
      "Brain Games": { correct: 0, total: 0 }
    };

    sessions.forEach(s => {
      totalQs += s.total;
      correctQs += s.score;
      if (subjects[s.subject]) {
        subjects[s.subject].total += s.total;
        subjects[s.subject].correct += s.score;
      }
    });

    if (totalQs > 0) {
      accuracy = Math.round((correctQs / totalQs) * 100);
    }

    // 2. Render Cards
    document.getElementById("parent-stat-days").innerText = studyDays;
    document.getElementById("parent-stat-streak").innerText = `${app.state.streak} Hari`;
    document.getElementById("parent-stat-duration").innerText = `${totalDurationMin} Menit`;
    document.getElementById("parent-stat-accuracy").innerText = `${accuracy}%`;

    // 3. Render Progress Bars
    const subjectsList = [
      { id: "math", name: "Matematika" },
      { id: "reading", name: "Membaca" },
      { id: "writing", name: "Menulis" },
      { id: "brain", name: "Brain Games" }
    ];

    subjectsList.forEach(sub => {
      const stats = subjects[sub.name];
      let pct = 0;
      if (stats.total > 0) {
        pct = Math.round((stats.correct / stats.total) * 100);
      }
      document.getElementById(`progress-${sub.id}`).style.width = `${pct}%`;
      document.getElementById(`pct-${sub.id}`).innerText = `${pct}%`;
    });

    // 4. Recommendations Engine
    const recList = document.getElementById("recommendation-list");
    recList.innerHTML = "";

    const lowAccuracySubjects = [];
    subjectsList.forEach(sub => {
      const stats = subjects[sub.name];
      const pct = stats.total > 0 ? (stats.correct / stats.total) : 1;
      if (stats.total > 0 && pct < 0.7) {
        lowAccuracySubjects.push(sub.name);
      }
    });

    if (sessions.length === 0) {
      recList.innerHTML = `<li>Anak Anda baru memulai petualangan! Semangati mereka untuk mencoba modul Matematika atau Membaca terlebih dahulu.</li>`;
    } else {
      if (lowAccuracySubjects.length > 0) {
        lowAccuracySubjects.forEach(subName => {
          recList.innerHTML += `<li>⚠️ Anak Anda membutuhkan sedikit bimbingan di modul <strong>${subName}</strong>. Coba bantu mereka belajar secara santai.</li>`;
        });
      } else {
        recList.innerHTML += `<li>🌟 Luar biasa! Kemampuan anak Anda sangat seimbang. Pertahankan ritme belajar harian ini.</li>`;
      }
      
      // Streak recommendation
      if (app.state.streak < 3) {
        recList.innerHTML += `<li>🔥 Tingkatkan konsistensi belajar anak untuk melatih daya ingat jangka panjang (Target: streak 3 hari berturut-turut).</li>`;
      }
    }

    // 5. Render History Rows
    const tableBody = document.getElementById("parent-history-rows");
    tableBody.innerHTML = "";

    if (sessions.length === 0) {
      tableBody.innerHTML = `<tr><td colspan="5" style="text-align:center;">Belum ada sesi belajar tercatat. Selesaikan quiz terlebih dahulu!</td></tr>`;
    } else {
      // Show last 10 sessions
      const recent = [...sessions].reverse().slice(0, 10);
      recent.forEach(s => {
        const row = document.createElement("tr");
        row.innerHTML = `
          <td>${s.date}</td>
          <td><strong>${s.subject}</strong></td>
          <td>${s.duration} detik</td>
          <td>${s.score}/${s.total}</td>
          <td><span style="color: ${s.score === s.total ? 'var(--color-success)' : 'var(--color-gold)'}; font-weight: bold;">
            ${s.score === s.total ? 'Sempurna 🌟' : 'Bagus 👍'}
          </span></td>
        `;
        tableBody.appendChild(row);
      });
    }
  },

  exportToPDF() {
    app.playSound("click");
    window.print();
  },

  resetProgress() {
    const confirmReset = confirm("Apakah Anda yakin ingin menghapus seluruh kemajuan belajar anak? Tindakan ini tidak dapat dibatalkan.");
    if (confirmReset) {
      localStorage.removeItem("ip_smart_kids_state");
      app.playSound("fail");
      alert("Kemajuan belajar telah di-reset.");
      window.location.reload();
    }
  }
};

// ==========================================
// REWARDS MODULE INVENTORY & SHOP
// ==========================================
const rewardsModule = {
  currentTab: "avatars",
  
  items: [
    // Avatars
    { id: "avatar_lion", name: "Singa Ceria 🦁", cost: 0, type: "avatars", asset: "🦁" },
    { id: "avatar_panda", name: "Panda Lucu 🐼", cost: 10, type: "avatars", asset: "🐼" },
    { id: "avatar_rabbit", name: "Kelinci Lincah 🐰", cost: 15, type: "avatars", asset: "🐰" },
    { id: "avatar_fox", name: "Rubah Pintar 🦊", cost: 20, type: "avatars", asset: "🦊" },
    
    // Stickers
    { id: "sticker_dino", name: "Dino Perkasa 🦖", cost: 5, type: "stickers", asset: "🦖" },
    { id: "sticker_rocket", name: "Roket Angkasa 🚀", cost: 8, type: "stickers", asset: "🚀" },
    { id: "sticker_unicorn", name: "Unicorn Ajaib 🦄", cost: 12, type: "stickers", asset: "🦄" },
    { id: "sticker_star", name: "Bintang Emas 🌟", cost: 5, type: "stickers", asset: "🌟" },
    
    // Themes
    { id: "theme_space", name: "Luar Angkasa 🌌", cost: 25, type: "themes", asset: "theme-space" },
    { id: "theme_jungle", name: "Hutan Tropis 🌳", cost: 25, type: "themes", asset: "theme-jungle" }
  ],

  init() {
    // Check if defaults exist
    if (!app.state.purchasedItems.includes("avatar_lion")) {
      app.state.purchasedItems.push("avatar_lion");
      app.saveState();
    }
  },

  setTab(tabName) {
    app.playSound("click");
    this.currentTab = tabName;
    
    // Toggle active state in UI tabs
    document.querySelectorAll(".rewards-tabs .tab-btn").forEach((btn, idx) => {
      const tabs = ["avatars", "stickers", "themes"];
      if (tabs[idx] === tabName) btn.classList.add("active");
      else btn.classList.remove("active");
    });

    this.renderShop();
  },

  renderShop() {
    const grid = document.getElementById("rewards-grid");
    if (!grid) return;
    grid.innerHTML = "";

    const filtered = this.items.filter(item => item.type === this.currentTab);
    
    filtered.forEach(item => {
      const card = document.createElement("div");
      card.className = "reward-card-item";

      const isOwned = app.state.purchasedItems.includes(item.id);
      const isEquipped = app.state.equippedItems.includes(item.id);

      // Render inner item visual representation
      let visualContent = "";
      if (item.type === "themes") {
        visualContent = `<div class="reward-item-icon" style="font-size: 24px; border-radius: 8px; background: linear-gradient(135deg, #130cb7 0%, #52e5e7 100%);">🎨</div>`;
      } else {
        visualContent = `<div class="reward-item-icon">${item.asset}</div>`;
      }

      card.innerHTML = `
        ${visualContent}
        <div class="reward-item-name">${item.name}</div>
      `;

      const actionBtn = document.createElement("button");
      actionBtn.className = "btn reward-item-buy";

      if (isEquipped) {
        actionBtn.innerText = "Dipakai";
        actionBtn.disabled = true;
        actionBtn.style.backgroundColor = "var(--color-success)";
        actionBtn.style.color = "white";
        actionBtn.style.boxShadow = "none";
      } else if (isOwned) {
        actionBtn.innerText = "Gunakan";
        actionBtn.classList.add("btn-primary");
        actionBtn.onclick = () => this.equipItem(item);
      } else {
        actionBtn.innerText = `⭐ ${item.cost}`;
        actionBtn.classList.add("btn-play");
        actionBtn.onclick = () => this.buyItem(item);
      }

      card.appendChild(actionBtn);
      grid.appendChild(card);
    });
  },

  buyItem(item) {
    if (app.state.stars >= item.cost) {
      app.state.stars -= item.cost;
      app.state.purchasedItems.push(item.id);
      app.saveState();
      app.renderHeader();
      app.playSound("complete");
      app.speak(`Hore! Kamu berhasil mendapatkan ${item.name}`);
      this.renderShop();
    } else {
      app.playSound("fail");
      app.speak("Bintangmu belum cukup. Ayo belajar lagi!");
      alert("Bintang kamu tidak cukup untuk menukarkan item ini!");
    }
  },

  equipItem(item) {
    app.playSound("click");
    
    if (item.type === "avatars") {
      app.state.avatar = item.asset;
      // Filter out old equipped avatars
      app.state.equippedItems = app.state.equippedItems.filter(id => {
        const itemObj = this.items.find(i => i.id === id);
        return itemObj && itemObj.type !== "avatars";
      });
      app.state.equippedItems.push(item.id);
    } else if (item.type === "themes") {
      const themeSkin = item.id.replace("theme_", "");
      app.applyTheme(themeSkin);
      // Filter out old equipped themes
      app.state.equippedItems = app.state.equippedItems.filter(id => {
        const itemObj = this.items.find(i => i.id === id);
        return itemObj && itemObj.type !== "themes";
      });
      app.state.equippedItems.push(item.id);
    } else if (item.type === "stickers") {
      // Stickers are just collected/displayed in shop
      app.state.equippedItems.push(item.id);
      app.speak("Stiker ditambahkan ke buku stikermu!");
    }

    app.saveState();
    app.renderHeader();
    this.renderShop();
  }
};
