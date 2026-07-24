/* ==========================================
   IP SMART KIDS - HANDWRITING CANVAS MODULE
   ========================================== */

const canvasModule = {
  canvas: null,
  ctx: null,
  isDrawing: false,
  tool: "pen", // "pen" or "eraser"
  brushColor: "#2c3e50",
  brushSize: 8,
  bgType: "lines", // "lines" or "grid"
  
  // Tracing Guide State
  currentGuideChar: "A",
  guideType: "letters", // "letters" or "numbers"

  // Undo/Redo Stacks
  strokes: [],
  undoneStrokes: [],
  currentStroke: [],

  init() {
    this.canvas = document.getElementById("handwriting-canvas");
    if (!this.canvas) return;

    this.ctx = this.canvas.getContext("2d");
    this.resizeCanvas();

    // Event Listeners for drawing
    this.canvas.addEventListener("mousedown", (e) => this.startDrawing(e));
    this.canvas.addEventListener("mousemove", (e) => this.draw(e));
    this.canvas.addEventListener("mouseup", () => this.stopDrawing());
    this.canvas.addEventListener("mouseleave", () => this.stopDrawing());

    // Touch Support
    this.canvas.addEventListener("touchstart", (e) => {
      e.preventDefault();
      const touch = e.touches[0];
      const rect = this.canvas.getBoundingClientRect();
      this.startDrawing({
        clientX: touch.clientX,
        clientY: touch.clientY
      });
    }, { passive: false });

    this.canvas.addEventListener("touchmove", (e) => {
      e.preventDefault();
      const touch = e.touches[0];
      this.draw({
        clientX: touch.clientX,
        clientY: touch.clientY
      });
    }, { passive: false });

    this.canvas.addEventListener("touchend", () => this.stopDrawing());

    // Window resize
    window.addEventListener("resize", () => this.resizeCanvas());

    // Setup sidebar guidelines
    this.renderGuides();
    this.selectGuide("A");
  },

  resizeCanvas() {
    if (!this.canvas) return;
    
    // Save current drawings
    const tempStrokes = [...this.strokes];
    
    const rect = this.canvas.parentElement.getBoundingClientRect();
    this.canvas.width = rect.width;
    this.canvas.height = rect.height;

    // Restore strokes
    this.strokes = tempStrokes;
    this.redraw();
  },

  startDrawing(e) {
    this.isDrawing = true;
    app.playSound("click");
    const pos = this.getMousePos(e);
    this.currentStroke = [{ x: pos.x, y: pos.y }];
    this.undoneStrokes = [];
  },

  draw(e) {
    if (!this.isDrawing) return;
    const pos = this.getMousePos(e);
    this.currentStroke.push({ x: pos.x, y: pos.y });
    
    // Draw in real-time
    this.ctx.lineWidth = this.tool === "eraser" ? 24 : this.brushSize;
    this.ctx.lineCap = "round";
    this.ctx.lineJoin = "round";
    this.ctx.strokeStyle = this.tool === "eraser" ? "#ffffff" : this.brushColor;

    const strokeLength = this.currentStroke.length;
    if (strokeLength > 1) {
      const p1 = this.currentStroke[strokeLength - 2];
      const p2 = this.currentStroke[strokeLength - 1];
      this.ctx.beginPath();
      this.ctx.moveTo(p1.x, p1.y);
      this.ctx.lineTo(p2.x, p2.y);
      this.ctx.stroke();
    }
  },

  stopDrawing() {
    if (!this.isDrawing) return;
    this.isDrawing = false;
    if (this.currentStroke.length > 0) {
      this.strokes.push({
        points: this.currentStroke,
        tool: this.tool,
        color: this.brushColor,
        size: this.tool === "eraser" ? 24 : this.brushSize
      });
    }
    this.currentStroke = [];
    this.redraw();
  },

  getMousePos(e) {
    const rect = this.canvas.getBoundingClientRect();
    return {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top
    };
  },

  setTool(tool) {
    this.tool = tool;
    document.querySelectorAll(".tool-btn").forEach(btn => btn.classList.remove("active"));
    document.getElementById(`tool-${tool}`).classList.add("active");
  },

  undo() {
    if (this.strokes.length > 0) {
      const popped = this.strokes.pop();
      this.undoneStrokes.push(popped);
      this.redraw();
    }
  },

  redo() {
    if (this.undoneStrokes.length > 0) {
      const popped = this.undoneStrokes.pop();
      this.strokes.push(popped);
      this.redraw();
    }
  },

  clear() {
    this.strokes = [];
    this.undoneStrokes = [];
    this.redraw();
  },

  toggleBackground() {
    this.bgType = this.bgType === "lines" ? "grid" : "lines";
    this.redraw();
  },

  // Draw guides, background, and strokes
  redraw() {
    this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

    // 1. Draw helper backgrounds
    if (this.bgType === "grid") {
      this.ctx.strokeStyle = "#e2e8f0";
      this.ctx.lineWidth = 1;
      const gridSize = 40;
      for (let x = 0; x < this.canvas.width; x += gridSize) {
        this.ctx.beginPath();
        this.ctx.moveTo(x, 0);
        this.ctx.lineTo(x, this.canvas.height);
        this.ctx.stroke();
      }
      for (let y = 0; y < this.canvas.height; y += gridSize) {
        this.ctx.beginPath();
        this.ctx.moveTo(0, y);
        this.ctx.lineTo(this.canvas.width, y);
        this.ctx.stroke();
      }
    } else {
      // Handwriting Lines (Lined paper guidelines)
      this.ctx.lineWidth = 1;
      const lineSpacing = 30;
      let y = lineSpacing;
      let index = 0;
      while (y < this.canvas.height) {
        this.ctx.strokeStyle = (index % 3 === 2) ? "rgba(231, 76, 60, 0.25)" : "rgba(74, 144, 226, 0.2)";
        this.ctx.beginPath();
        this.ctx.moveTo(0, y);
        this.ctx.lineTo(this.canvas.width, y);
        this.ctx.stroke();
        y += lineSpacing;
        index++;
      }
    }

    // 2. Draw faint Tracing Letter Guidance in Background
    if (this.currentGuideChar) {
      this.ctx.fillStyle = "rgba(255, 184, 92, 0.12)";
      this.ctx.font = "800 240px 'Fredoka One'";
      this.ctx.textAlign = "center";
      this.ctx.textBaseline = "middle";
      this.ctx.fillText(this.currentGuideChar, this.canvas.width / 2, this.canvas.height / 2);
    }

    // 3. Draw Recorded Strokes
    this.strokes.forEach(stroke => {
      this.ctx.lineWidth = stroke.size;
      this.ctx.lineCap = "round";
      this.ctx.lineJoin = "round";
      this.ctx.strokeStyle = stroke.tool === "eraser" ? "#ffffff" : stroke.color;

      if (stroke.points.length > 0) {
        this.ctx.beginPath();
        this.ctx.moveTo(stroke.points[0].x, stroke.points[0].y);
        for (let i = 1; i < stroke.points.length; i++) {
          this.ctx.lineTo(stroke.points[i].x, stroke.points[i].y);
        }
        this.ctx.stroke();
      }
    });
  },

  // Setup sidebar guides
  setGuideType(type) {
    this.guideType = type;
    document.querySelectorAll(".guide-tabs .tab-btn").forEach(btn => btn.classList.remove("active"));
    const activeIndex = (type === "letters") ? 0 : 1;
    document.querySelectorAll(".guide-tabs .tab-btn")[activeIndex].classList.add("active");
    this.renderGuides();
  },

  renderGuides() {
    const list = document.getElementById("canvas-guide-chars");
    if (!list) return;

    list.innerHTML = "";
    const items = this.guideType === "letters" 
      ? ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"]
      : ["1","2","3","4","5","6","7","8","9","10"];

    items.forEach(char => {
      const item = document.createElement("div");
      item.className = "char-guide-item";
      if (char === this.currentGuideChar) item.classList.add("active");
      item.innerText = char;
      item.onclick = () => this.selectGuide(char);
      list.appendChild(item);
    });
  },

  selectGuide(char) {
    this.currentGuideChar = char;
    document.querySelectorAll(".char-guide-item").forEach(item => {
      if (item.innerText === char) item.classList.add("active");
      else item.classList.remove("active");
    });
    
    const display = document.getElementById("guided-char-display");
    if (display) display.innerText = char;

    this.clear();
    app.speak(`Ayo tulis ${char}`);
  },

  checkWriting() {
    if (this.strokes.length === 0) {
      app.speak("Gambarlah sesuatu terlebih dahulu");
      return;
    }
    
    // Log the success session and reward
    app.logSession("Menulis", 1, 1);
    app.triggerCelebration(5, 5, `Hebat! Tulisan ${this.currentGuideChar} kamu sangat bagus! ✨`);
    
    // Pick the next letter automatically to guide kids
    this.advanceGuide();
  },

  advanceGuide() {
    const items = this.guideType === "letters" 
      ? ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"]
      : ["1","2","3","4","5","6","7","8","9","10"];
    
    const currentIndex = items.indexOf(this.currentGuideChar);
    if (currentIndex !== -1 && currentIndex < items.length - 1) {
      setTimeout(() => {
        this.selectGuide(items[currentIndex + 1]);
      }, 2000);
    }
  }
};
