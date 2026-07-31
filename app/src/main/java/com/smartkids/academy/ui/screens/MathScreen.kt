package com.smartkids.academy.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartkids.academy.data.local.AppDatabase
import com.smartkids.academy.data.remote.QuizApiService
import com.smartkids.academy.domain.usecase.AdaptiveLearningEngine
import com.smartkids.academy.ui.components.ConfettiCanvas
import com.smartkids.academy.ui.components.SoundManager
import com.smartkids.academy.ui.theme.*
import kotlinx.coroutines.launch

enum class MathCategory(val title: String, val symbol: String, val color: Color) {
    ADDITION("Penjumlahan", "+", Color(0xFF4CAF50)),
    SUBTRACTION("Pengurangan", "-", Color(0xFFFF9800)),
    MULTIPLICATION("Perkalian", "×", Color(0xFF2196F3)),
    DIVISION("Pembagian", "÷", Color(0xFF9C27B0)),
    MIXED("Campuran", "🎲", Color(0xFFE91E63))
}

enum class QuizDifficulty(val title: String) {
    ALL("Semua"),
    EASY("🌱 Mudah"),
    MEDIUM("🌿 Sedang"),
    HARD("🚀 Cerita")
}

data class MathMaterial(
    val category: MathCategory,
    val title: String,
    val conceptExplanation: String,
    val visualEmoji: String,
    val formulaText: String,
    val stepByStep: List<String>
)

data class MathQuestion(
    val category: MathCategory,
    val difficulty: QuizDifficulty,
    val questionText: String,
    val visualRepresentation: String,
    val options: List<Int>,
    val correctAnswer: Int,
    val explanationTip: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathScreen(navController: NavController) {
    var selectedCategoryFilter by remember { mutableStateOf<MathCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎮 Kuis & Game Matematika", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftBluePrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF7F9FC))
        ) {
            MathQuizView(
                categoryFilter = selectedCategoryFilter,
                onSelectCategory = { selectedCategoryFilter = it }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathMaterialsView(
    selectedCategory: MathCategory?,
    onCategorySelect: (MathCategory?) -> Unit
) {
    val materials = remember { getSampleMathMaterials() }
    val filteredMaterials = remember(selectedCategory) {
        if (selectedCategory == null || selectedCategory == MathCategory.MIXED) materials
        else materials.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelect(null) },
                label = { Text("Semua") }
            )
            MathCategory.values().filter { it != MathCategory.MIXED }.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelect(if (selectedCategory == cat) null else cat) },
                    label = { Text("${cat.symbol} ${cat.title}") }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredMaterials) { mat ->
                MaterialCard(material = mat)
            }
        }
    }
}

@Composable
fun MaterialCard(material: MathMaterial) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(material.category.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = material.category.symbol,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = material.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = material.conceptExplanation,
                fontSize = 15.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(material.category.color.copy(alpha = 0.1f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = material.visualEmoji,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = material.formulaText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = material.category.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 Langkah Mudah:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DarkText
            )
            material.stepByStep.forEachIndexed { idx, step ->
                Text(
                    text = "${idx + 1}. $step",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathQuizView(
    categoryFilter: MathCategory?,
    onSelectCategory: (MathCategory?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context, scope) }

    var currentCategory by remember { mutableStateOf(categoryFilter ?: MathCategory.ADDITION) }
    var currentDifficulty by remember { mutableStateOf(QuizDifficulty.ALL) }
    var score by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var correctCountInSession by remember { mutableIntStateOf(0) }

    var questionsList by remember(currentCategory, currentDifficulty) {
        mutableStateOf(get15QuestionsPerCategory(currentCategory, currentDifficulty))
    }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(currentCategory, currentDifficulty, refreshTrigger) {
        isSyncing = true
        val remoteQuestions = QuizApiService.fetchMathQuestions(currentCategory, currentDifficulty)
        if (!remoteQuestions.isNullOrEmpty()) {
            questionsList = remoteQuestions
            if (refreshTrigger > 0) {
                android.widget.Toast.makeText(context, "Soal diperbarui dari server! 🔄", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        isSyncing = false
    }

    val isQuizFinished = currentQuestionIndex >= questionsList.size
    val currentQuestion = questionsList.getOrNull(currentQuestionIndex)

    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Category Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MathCategory.values().forEach { cat ->
                Button(
                    onClick = {
                        currentCategory = cat
                        onSelectCategory(cat)
                        currentQuestionIndex = 0
                        correctCountInSession = 0
                        isAnswered = false
                        selectedOption = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentCategory == cat) cat.color else Color.LightGray
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                ) {
                    Text(cat.symbol, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Difficulty Filter Chips & Live Sync Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                QuizDifficulty.values().forEach { diff ->
                    val isSelected = currentDifficulty == diff
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = {
                            currentDifficulty = diff
                            currentQuestionIndex = 0
                            correctCountInSession = 0
                            isAnswered = false
                            selectedOption = null
                        },
                        label = { Text(diff.title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
            AssistChip(
                onClick = { refreshTrigger++ },
                label = { Text(if (isSyncing) "Syncing..." else "🔄 Refresh", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Score & Progress Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Skor: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Text(
                text = "Soal ${minOf(currentQuestionIndex + 1, questionsList.size)} / ${questionsList.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = currentCategory.color
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFF5722))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Streak: $streak 🔥", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isQuizFinished) {
            // Completion Summary Screen when all 15 questions in category are done!
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉 SELAMAT! 🎉", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kamu telah menyelesaikan Kuis ${currentCategory.title}!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "🏆 Skor Total: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SoftBluePrimary)
                    Text(text = "✅ Benar: $correctCountInSession dari ${questionsList.size} Soal", fontSize = 16.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                currentQuestionIndex = 0
                                correctCountInSession = 0
                                isAnswered = false
                                selectedOption = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ulangi Kuis 🔄", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val nextCategory = when (currentCategory) {
                                    MathCategory.ADDITION -> MathCategory.SUBTRACTION
                                    MathCategory.SUBTRACTION -> MathCategory.MULTIPLICATION
                                    MathCategory.MULTIPLICATION -> MathCategory.DIVISION
                                    MathCategory.DIVISION -> MathCategory.MIXED
                                    MathCategory.MIXED -> MathCategory.ADDITION
                                }
                                currentCategory = nextCategory
                                onSelectCategory(nextCategory)
                                currentQuestionIndex = 0
                                correctCountInSession = 0
                                isAnswered = false
                                selectedOption = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kategori Lain ➡️", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            currentQuestion?.let { q ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kategori: ${q.category.title}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = q.category.color
                            )
                            Text(
                                text = q.difficulty.title,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (q.visualRepresentation.isNotEmpty()) {
                            Text(
                                text = q.visualRepresentation,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Text(
                            text = q.questionText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkText,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multiple Choice Options (2x2 Grid using Rows)
                val chunkedOptions = q.options.chunked(2)
                chunkedOptions.forEach { rowOptions ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowOptions.forEach { option ->
                            val buttonColor = when {
                                !isAnswered -> SoftBluePrimary
                                option == q.correctAnswer -> Color(0xFF4CAF50)
                                option == selectedOption -> Color(0xFFF44336)
                                else -> Color.Gray
                            }

                            Button(
                                onClick = {
                                    if (!isAnswered) {
                                        selectedOption = option
                                        isAnswered = true
                                        isCorrect = (option == q.correctAnswer)

                                        if (isCorrect) {
                                            score += 10
                                            streak++
                                            correctCountInSession++
                                            SoundManager.playCorrectSound(context)
                                        } else {
                                            streak = 0
                                            SoundManager.playWrongSound(context)
                                        }

                                        scope.launch {
                                            val currentProgress = db.progressDao().getProgressForSubject("MATHEMATICS")
                                                ?: com.smartkids.academy.data.local.entity.ProgressEntity("MATHEMATICS", "BEGINNER", 0, 0, 0, 0, 0, System.currentTimeMillis())

                                            val updated = AdaptiveLearningEngine.evaluateProgress(
                                                currentProgress = currentProgress,
                                                newCorrectCount = if (isCorrect) 1 else 0,
                                                newTotalQuestions = 1
                                            )
                                            db.progressDao().insertOrUpdateProgress(updated)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                            ) {
                                Text(
                                    text = option.toString(),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Feedback Card & Next Button
                AnimatedVisibility(visible = isAnswered) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                                        contentDescription = null,
                                        tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isCorrect) "Hebat Sekali! Jawabannya Benar! 🎉 (+10 Skor)" else "Jawaban yang tepat adalah ${q.correctAnswer} 💪",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                        fontSize = 15.sp
                                    )
                                }
                                if (q.explanationTip.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "💡 Petunjuk: ${q.explanationTip}",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                isAnswered = false
                                selectedOption = null
                                currentQuestionIndex++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBluePrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Soal Berikutnya ➡️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        ConfettiCanvas(isVisible = isAnswered && isCorrect)
    }
}
}

fun getSampleMathMaterials(): List<MathMaterial> {
    return listOf(
        MathMaterial(
            category = MathCategory.ADDITION,
            title = "Penjumlahan (+)",
            conceptExplanation = "Penjumlahan artinya menggabungkan dua kelompok benda menjadi satu jumlah yang lebih banyak.",
            visualEmoji = "🍎 + 🍎🍎",
            formulaText = "1 + 2 = 3",
            stepByStep = listOf(
                "Hitung benda kelompok pertama (1 apel)",
                "Hitung benda kelompok kedua (2 apel)",
                "Gabungkan dan hitung semua apel: 1, 2, 3!",
                "Jadi 1 + 2 = 3"
            )
        ),
        MathMaterial(
            category = MathCategory.SUBTRACTION,
            title = "Pengurangan (-)",
            conceptExplanation = "Pengurangan artinya mengambil atau mengurangi beberapa benda dari jumlah semula.",
            visualEmoji = "🎈🎈🎈 ➔ (diambil 1 🎈)",
            formulaText = "3 - 1 = 2",
            stepByStep = listOf(
                "Siapkan 3 balon awal",
                "Kura-kura mengambil 1 balon",
                "Sisa balon yang ada di tangan adalah 2 balon!",
                "Jadi 3 - 1 = 2"
            )
        ),
        MathMaterial(
            category = MathCategory.MULTIPLICATION,
            title = "Perkalian (×)",
            conceptExplanation = "Perkalian adalah penjumlahan berulang dari angka yang sama.",
            visualEmoji = "⭐ ⭐   +   ⭐ ⭐   +   ⭐ ⭐",
            formulaText = "3 × 2 = 6",
            stepByStep = listOf(
                "Ada 3 kelompok bintang",
                "Setiap kelompok berisi 2 bintang",
                "Jumlahkan: 2 + 2 + 2 = 6",
                "Tulis ringkas: 3 × 2 = 6"
            )
        ),
        MathMaterial(
            category = MathCategory.DIVISION,
            title = "Pembagian (÷)",
            conceptExplanation = "Pembagian artinya membagikan sejumlah benda secara adil dan sama banyak kepada beberapa teman.",
            visualEmoji = "🍪 🍪 🍪 🍪 ➔ Dibagi 2 Anak",
            formulaText = "4 ÷ 2 = 2",
            stepByStep = listOf(
                "Ada 4 kue biskuit cokelat",
                "Bagikan kepada 2 anak secara bergantian",
                "Anak A mendapat 2 kue, Anak B mendapat 2 kue",
                "Jadi 4 ÷ 2 = 2"
            )
        )
    )
}

fun get15QuestionsPerCategory(category: MathCategory, difficultyFilter: QuizDifficulty): List<MathQuestion> {
    val allQuestions = when (category) {
        MathCategory.ADDITION -> listOf(
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "2 + 3 = ?", "🍎🍎 + 🍎🍎🍎", listOf(4, 5, 6, 7), 5, "2 ditambahkan 3 menjadi 5"),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "4 + 1 = ?", "🐱🐱🐱🐱 + 🐱", listOf(3, 5, 6, 4), 5, "4 ditambahkan 1 menjadi 5"),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "3 + 3 = ?", "🌟🌟🌟 + 🌟🌟🌟", listOf(5, 6, 7, 8), 6, "3 ditambahkan 3 menjadi 6"),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "5 + 2 = ?", "🎈🎈🎈🎈🎈 + 🎈🎈", listOf(6, 7, 8, 9), 7),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "6 + 4 = ?", "⚽ (6) + ⚽ (4)", listOf(9, 10, 11, 8), 10),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "7 + 3 = ?", "🍪 (7) + 🍪 (3)", listOf(8, 9, 10, 11), 10),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "8 + 5 = ?", "🍎 (8) + 🍎 (5)", listOf(12, 13, 14, 15), 13),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.EASY, "9 + 4 = ?", "🚗 (9) + 🚗 (4)", listOf(11, 12, 13, 14), 13),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.MEDIUM, "12 + 5 = ?", "🔢 Penjumlahan Puluhan", listOf(15, 17, 18, 16), 17),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.MEDIUM, "15 + 10 = ?", "🔢 Penjumlahan Puluhan", listOf(20, 25, 30, 22), 25),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.MEDIUM, "8 + ? = 14", "❓ Cari Angka Rahasia", listOf(5, 6, 7, 8), 6, "14 dikurangi 8 adalah 6"),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.MEDIUM, "20 + 15 = ?", "🔢 Penjumlahan Puluhan", listOf(30, 35, 40, 25), 35),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.HARD, "Budi punya 8 kelereng. Dito memberi 6 kelereng. Total kelereng Budi?", "🔮 Cerita Matematika", listOf(12, 14, 15, 16), 14, "8 + 6 = 14"),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.HARD, "Siti memetik 15 bunga. Ibu memberi 10 bunga lagi. Total bunga Siti?", "🌸 Cerita Matematika", listOf(20, 25, 30, 22), 25, "15 + 10 = 25"),
            MathQuestion(MathCategory.ADDITION, QuizDifficulty.HARD, "7 + 5 + 3 = ?", "🔢 Penjumlahan 3 Angka", listOf(13, 14, 15, 16), 15, "(7 + 3) + 5 = 15")
        )
        MathCategory.SUBTRACTION -> listOf(
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.EASY, "5 - 2 = ?", "🍎🍎🍎🍎🍎 - 🍎🍎", listOf(2, 3, 4, 1), 3),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.EASY, "4 - 1 = ?", "🎈🎈🎈🎈 - 🎈", listOf(2, 3, 4, 5), 3),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.EASY, "6 - 3 = ?", "🌟🌟🌟🌟🌟🌟 - 🌟🌟🌟", listOf(2, 3, 4, 5), 3),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.EASY, "8 - 4 = ?", "🍪 (8) - 🍪 (4)", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.EASY, "7 - 2 = ?", "🐱 (7) - 🐱 (2)", listOf(4, 5, 6, 3), 5),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.EASY, "9 - 5 = ?", "🚗 (9) - 🚗 (5)", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.EASY, "10 - 3 = ?", "⚽ (10) - ⚽ (3)", listOf(6, 7, 8, 5), 7),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.MEDIUM, "12 - 4 = ?", "🔢 Pengurangan Belasan", listOf(6, 7, 8, 9), 8),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.MEDIUM, "15 - 7 = ?", "🔢 Pengurangan Belasan", listOf(7, 8, 9, 6), 8),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.MEDIUM, "20 - 8 = ?", "🔢 Pengurangan Puluhan", listOf(10, 12, 14, 11), 12),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.MEDIUM, "18 - ? = 10", "❓ Cari Angka Rahasia", listOf(6, 7, 8, 9), 8, "18 - 8 = 10"),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.MEDIUM, "30 - 12 = ?", "🔢 Pengurangan Puluhan", listOf(15, 18, 20, 16), 18),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.HARD, "Ani punya 20 biskuit. Membagikan 8 biskuit ke teman. Sisa biskuit Ani?", "🍪 Cerita Pengurangan", listOf(10, 11, 12, 14), 12, "20 - 8 = 12"),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.HARD, "Ada 25 balon di pesta. 5 balon meletus. Berapa sisa balon?", "🎈 Cerita Pengurangan", listOf(15, 20, 22, 18), 20, "25 - 5 = 20"),
            MathQuestion(MathCategory.SUBTRACTION, QuizDifficulty.HARD, "14 - 6 - 2 = ?", "🔢 Pengurangan Berturut", listOf(5, 6, 7, 8), 6, "14 - 6 = 8, lalu 8 - 2 = 6")
        )
        MathCategory.MULTIPLICATION -> listOf(
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.EASY, "2 × 3 = ?", "(🍎🍎) (🍎🍎) (🍎🍎)", listOf(5, 6, 7, 8), 6),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.EASY, "3 × 3 = ?", "(⭐⭐⭐) (⭐⭐⭐) (⭐⭐⭐)", listOf(6, 8, 9, 12), 9),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.EASY, "4 × 2 = ?", "(🎈🎈) (🎈🎈) (🎈🎈) (🎈🎈)", listOf(6, 8, 10, 12), 8),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.EASY, "5 × 2 = ?", "(🍪🍪) (🍪🍪) (🍪🍪) (🍪🍪) (🍪🍪)", listOf(8, 10, 12, 15), 10),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.EASY, "2 × 4 = ?", "(🐱🐱🐱🐱) (🐱🐱🐱🐱)", listOf(6, 7, 8, 9), 8),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.MEDIUM, "3 × 4 = ?", "🔢 Perkalian 3", listOf(10, 12, 14, 15), 12),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.MEDIUM, "5 × 4 = ?", "🔢 Perkalian 5", listOf(15, 20, 25, 18), 20),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.MEDIUM, "6 × 3 = ?", "🔢 Perkalian 6", listOf(15, 18, 20, 24), 18),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.MEDIUM, "7 × 2 = ?", "🔢 Perkalian 7", listOf(12, 14, 16, 15), 14),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.MEDIUM, "8 × 3 = ?", "🔢 Perkalian 8", listOf(20, 24, 26, 28), 24),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.MEDIUM, "9 × 2 = ?", "🔢 Perkalian 9", listOf(16, 18, 20, 22), 18),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.MEDIUM, "10 × 3 = ?", "🔢 Perkalian 10", listOf(25, 30, 35, 40), 30),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.HARD, "Ada 4 kotak pensil. Setiap kotak isi 5 pensil. Total pensil?", "✏️ Cerita Perkalian", listOf(15, 20, 25, 18), 20, "4 × 5 = 20"),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.HARD, "Ada 5 kelompok anak. Setiap kelompok isi 3 anak. Total anak?", "👨‍👩‍👧 Cerita Perkalian", listOf(12, 15, 18, 20), 15, "5 × 3 = 15"),
            MathQuestion(MathCategory.MULTIPLICATION, QuizDifficulty.HARD, "6 × 5 = ?", "🔢 Perkalian 6", listOf(25, 30, 35, 40), 30)
        )
        MathCategory.DIVISION -> listOf(
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.EASY, "6 ÷ 2 = ?", "🍎🍎🍎🍎🍎🍎 ÷ 2 kelompok", listOf(2, 3, 4, 5), 3),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.EASY, "8 ÷ 2 = ?", "🎈 (8) ÷ 2 kelompok", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.EASY, "9 ÷ 3 = ?", "🌟 (9) ÷ 3 kelompok", listOf(2, 3, 4, 5), 3),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.EASY, "10 ÷ 2 = ?", "🍪 (10) ÷ 2 anak", listOf(4, 5, 6, 7), 5),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "12 ÷ 3 = ?", "🔢 Pembagian 12", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "15 ÷ 5 = ?", "🔢 Pembagian 15", listOf(2, 3, 4, 5), 3),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "16 ÷ 4 = ?", "🔢 Pembagian 16", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "18 ÷ 2 = ?", "🔢 Pembagian 18", listOf(8, 9, 10, 7), 9),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "20 ÷ 4 = ?", "🔢 Pembagian 20", listOf(4, 5, 6, 8), 5),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "24 ÷ 6 = ?", "🔢 Pembagian 24", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "25 ÷ 5 = ?", "🔢 Pembagian 25", listOf(4, 5, 6, 7), 5),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.MEDIUM, "30 ÷ 5 = ?", "🔢 Pembagian 30", listOf(5, 6, 7, 8), 6),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.HARD, "Pak Guru punya 18 buku dibagikan sama rata ke 3 murid. Setiap murid dapat?", "📚 Cerita Pembagian", listOf(5, 6, 7, 8), 6, "18 ÷ 3 = 6"),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.HARD, "Ibu punya 20 kue dibagikan ke 4 anak. Setiap anak dapat?", "🍪 Cerita Pembagian", listOf(4, 5, 6, 7), 5, "20 ÷ 4 = 5"),
            MathQuestion(MathCategory.DIVISION, QuizDifficulty.HARD, "36 ÷ 6 = ?", "🔢 Pembagian 36", listOf(5, 6, 7, 8), 6)
        )
        MathCategory.MIXED -> listOf(
            MathQuestion(MathCategory.MIXED, QuizDifficulty.EASY, "5 + 4 = ?", "🍎 Penjumlahan", listOf(8, 9, 10, 7), 9),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.EASY, "10 - 6 = ?", "🎈 Pengurangan", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.EASY, "3 × 4 = ?", "⭐ Perkalian", listOf(10, 12, 14, 9), 12),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.EASY, "16 ÷ 4 = ?", "🍪 Pembagian", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "7 + 8 = ?", "🔢 Penjumlahan", listOf(13, 14, 15, 16), 15),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "15 - 9 = ?", "🔢 Pengurangan", listOf(5, 6, 7, 8), 6),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "4 × 5 = ?", "🔢 Perkalian", listOf(15, 20, 25, 18), 20),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "20 ÷ 5 = ?", "🔢 Pembagian", listOf(3, 4, 5, 6), 4),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "12 + 13 = ?", "🔢 Penjumlahan Puluhan", listOf(20, 25, 30, 22), 25),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "25 - 10 = ?", "🔢 Pengurangan Puluhan", listOf(12, 15, 18, 20), 15),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "6 × 3 = ?", "🔢 Perkalian", listOf(15, 18, 20, 24), 18),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.MEDIUM, "18 ÷ 3 = ?", "🔢 Pembagian", listOf(5, 6, 7, 8), 6),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.HARD, "Rudi punya 10 apel, makan 3 apel, beli lagi 5 apel. Berapa apel Rudi?", "🍎 Cerita Campuran", listOf(10, 12, 15, 11), 12, "(10 - 3) + 5 = 12"),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.HARD, "Siti punya 15 kue, dapat 5 kue lagi, dimakan 4 kue. Sisa kue?", "🍰 Cerita Campuran", listOf(14, 15, 16, 18), 16, "(15 + 5) - 4 = 16"),
            MathQuestion(MathCategory.MIXED, QuizDifficulty.HARD, "8 + 6 - 4 = ?", "🔢 Hitung Campuran", listOf(8, 9, 10, 12), 10, "8 + 6 = 14, lalu 14 - 4 = 10")
        )
    }

    return if (difficultyFilter == QuizDifficulty.ALL) {
        allQuestions
    } else {
        val filtered = allQuestions.filter { it.difficulty == difficultyFilter }
        if (filtered.isNotEmpty()) filtered else allQuestions
    }
}
