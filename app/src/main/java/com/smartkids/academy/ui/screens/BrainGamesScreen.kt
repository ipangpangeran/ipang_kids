package com.smartkids.academy.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.smartkids.academy.data.remote.BrainCategory
import com.smartkids.academy.data.remote.BrainQuestion
import com.smartkids.academy.data.remote.QuizApiService
import com.smartkids.academy.ui.components.SoundManager
import com.smartkids.academy.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainGamesScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var selectedCategory by remember { mutableStateOf<BrainCategory?>(null) }
    var questionsList by remember { mutableStateOf<List<BrainQuestion>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var isAnswered by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadBrainQuestions() {
        scope.launch {
            isLoading = true
            val remote = QuizApiService.fetchBrainQuestions()
            if (!remote.isNullOrEmpty()) {
                questionsList = if (selectedCategory != null) {
                    remote.filter { it.category == selectedCategory }
                } else {
                    remote
                }
            } else {
                questionsList = listOf(
                    BrainQuestion(
                        category = BrainCategory.MEMORY_MATCH,
                        difficulty = QuizDifficulty.EASY,
                        questionText = "Pencocokan Pasangan Emoji Hewan 🐱🐶",
                        emojiSet = listOf("🐱", "🐶", "🐼", "🦁"),
                        options = listOf("🐱", "🐶", "🐼", "🦁"),
                        correctAnswer = "🐱",
                        explanationTip = "Cocokkan pasangan gambar yang sama!"
                    ),
                    BrainQuestion(
                        category = BrainCategory.PATTERN_SEQUENCE,
                        difficulty = QuizDifficulty.EASY,
                        questionText = "Lengkapi Pola: 🍎, 🍌, 🍎, ❓",
                        emojiSet = listOf("🍎", "🍌", "🍎", "❓"),
                        options = listOf("🍎", "🍌", "🍇", "🍊"),
                        correctAnswer = "🍌",
                        explanationTip = "Pola berulang: Apel, Pisang, Apel, Pisang!"
                    )
                )
            }
            currentIndex = 0
            isAnswered = false
            isLoading = false
        }
    }

    LaunchedEffect(selectedCategory) {
        loadBrainQuestions()
    }

    val currentQuestion = questionsList.getOrNull(currentIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧠 Brain Games", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { loadBrainQuestions() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Live", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftPurpleWriting,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = if (isLandscape) 16.dp else 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Semua 🎮", fontSize = 12.sp) }
                )
                BrainCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.icon} ${cat.title}", fontSize = 12.sp) }
                    )
                }
            }

            // Stats Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Skor Kamu", fontSize = 12.sp, color = Color.Gray)
                        Text("$score Bintang ⭐", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoftPurpleWriting)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Streak", fontSize = 12.sp, color = Color.Gray)
                        Text("$streak 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SoftOrangeReading)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SoftPurpleWriting)
                }
            } else if (currentQuestion != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
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
                                Surface(
                                    color = SoftPurpleWriting.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${currentQuestion.category.icon} ${currentQuestion.category.title}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftPurpleWriting,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = currentQuestion.questionText,
                                    fontSize = if (isLandscape) 18.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = DarkText
                                )

                                if (currentQuestion.emojiSet.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = currentQuestion.emojiSet.joinToString(" "),
                                        fontSize = 42.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 Option Buttons
                        currentQuestion.options.forEach { option ->
                            val buttonColor = when {
                                !isAnswered -> SoftPurpleWriting
                                option == currentQuestion.correctAnswer -> Color(0xFF4CAF50)
                                option == selectedOption -> Color(0xFFF44336)
                                else -> Color.Gray
                            }

                            Button(
                                onClick = {
                                    if (!isAnswered) {
                                        selectedOption = option
                                        isAnswered = true
                                        isCorrect = (option == currentQuestion.correctAnswer)
                                        if (isCorrect) {
                                            score += 10
                                            streak++
                                            SoundManager.playCorrectSound(context)
                                        } else {
                                            streak = 0
                                            SoundManager.playWrongSound(context)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(text = option, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }

                        if (isAnswered) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (isCorrect) "🎉 Hebat! Jawaban Benar!" else "❌ Jawaban Belum Tepat",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                    if (currentQuestion.explanationTip.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = currentQuestion.explanationTip, fontSize = 13.sp, color = DarkText)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (currentIndex < questionsList.size - 1) {
                                        currentIndex++
                                    } else {
                                        currentIndex = 0
                                    }
                                    isAnswered = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftOrangeReading),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Soal Selanjutnya ➡️", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
