package com.smartkids.academy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

data class LetterCard(
    val letter: String,
    val word: String,
    val syllable: String,
    val emoji: String,
    val color: Color
)

data class SyllableGroup(
    val title: String,
    val syllables: List<String>,
    val exampleWords: List<Pair<String, String>> // Word, Emoji
)

data class ReadingQuestion(
    val categoryTitle: String,
    val questionText: String,
    val emoji: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanationTip: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Abjad, 1: Suku Kata, 2: Kuis Baca

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📖 Pembelajaran Membaca", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftOrangeReading,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFFF8F0))
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = SoftOrangeReading
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🔤 Abjad A-Z", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📚 Suku Kata", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("🎯 Kuis Baca", fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> AlphabetView()
                1 -> SyllableWordsView()
                2 -> ReadingQuizView()
            }
        }
    }
}

@Composable
fun AlphabetView() {
    val letters = remember { getAlphabetCards() }
    var selectedLetter by remember { mutableStateOf<LetterCard?>(letters.first()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        selectedLetter?.let { item ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${item.letter}  ${item.letter.lowercase()}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = item.color
                        )
                        Text(
                            text = "${item.syllable} (${item.word})",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkText
                        )
                    }
                    Text(
                        text = item.emoji,
                        fontSize = 54.sp
                    )
                }
            }
        }

        Text(
            text = "Pilih Huruf untuk Belajar Suara & Kata:",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 70.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(letters) { item ->
                val isSelected = selectedLetter?.letter == item.letter
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) item.color else Color.White)
                        .border(
                            width = 2.dp,
                            color = item.color,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedLetter = item },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = item.letter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.White else item.color
                        )
                        Text(
                            text = item.emoji,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SyllableWordsView() {
    val groups = remember { getSyllableGroups() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(groups) { group ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = group.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftOrangeReading
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        group.syllables.forEach { syl ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SoftOrangeReading.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = syl,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = DarkText
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Contoh Kata:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        group.exampleWords.forEach { (word, emoji) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = word, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingQuizView() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context, scope) }

    var questions by remember { mutableStateOf(getRichReadingQuestions()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isSyncing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshTrigger) {
        isSyncing = true
        val remoteQuestions = QuizApiService.fetchReadingQuestions()
        if (!remoteQuestions.isNullOrEmpty()) {
            questions = remoteQuestions
            if (refreshTrigger > 0) {
                android.widget.Toast.makeText(context, "Soal Membaca diperbarui dari server! 🔄", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        isSyncing = false
    }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var correctCountInSession by remember { mutableIntStateOf(0) }

    val isQuizFinished = currentQuestionIndex >= questions.size
    val q = questions.getOrNull(currentQuestionIndex)

    var selectedOption by remember { mutableStateOf<String?>(null) }
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
        // Refresh Trigger Button Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { refreshTrigger++ },
                label = { Text(if (isSyncing) "Syncing..." else "🔄 Refresh Soal Live", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color.White)
            )
        }

        // Score & Progress Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Skor Membaca: $score", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SoftOrangeReading)
            Text(
                text = "Soal ${minOf(currentQuestionIndex + 1, questions.size)} / ${questions.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SoftOrangeReading
            )
            Icon(Icons.Default.Star, contentDescription = null, tint = SoftOrangeReading)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isQuizFinished) {
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
                    Text("🎉 KUIS SELESAI! 🎉", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kamu telah menjawab semua 15 Soal Membaca!",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "🏆 Skor Total: $score", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SoftOrangeReading)
                    Text(text = "✅ Benar: $correctCountInSession dari ${questions.size} Soal", fontSize = 16.sp, color = DarkText)
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            currentQuestionIndex = 0
                            correctCountInSession = 0
                            isAnswered = false
                            selectedOption = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftOrangeReading),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Ulangi Kuis Membaca 🔄", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        } else {
            q?.let { item ->
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
                        Text(
                            text = item.categoryTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftOrangeReading
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (item.emoji.isNotEmpty()) {
                            Text(text = item.emoji, fontSize = 64.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Text(
                            text = item.questionText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = DarkText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

            // Options list
            item.options.forEach { option ->
                val buttonColor = when {
                    !isAnswered -> SoftOrangeReading
                    option == item.correctAnswer -> Color(0xFF4CAF50)
                    option == selectedOption -> Color(0xFFF44336)
                    else -> Color.Gray
                }

                Button(
                    onClick = {
                        if (!isAnswered) {
                            selectedOption = option
                            isAnswered = true
                            isCorrect = (option == item.correctAnswer)
                            if (isCorrect) {
                                score += 10
                                correctCountInSession++
                                SoundManager.playCorrectSound()
                            } else {
                                SoundManager.playWrongSound()
                            }

                            scope.launch {
                                val currentProgress = db.progressDao().getProgressForSubject("READING")
                                    ?: com.smartkids.academy.data.local.entity.ProgressEntity("READING", "BEGINNER", 0, 0, 0, 0, 0, System.currentTimeMillis())

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
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(vertical = 3.dp)
                ) {
                    Text(text = option, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(visible = isAnswered) {
                Column {
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
                                    text = if (isCorrect) "Pintar Sekali! Pembaca Hebat! 🌟 (+10 Skor)" else "Jawaban yang tepat: ${item.correctAnswer} 👍",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    fontSize = 15.sp
                                )
                            }
                            if (item.explanationTip.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "💡 Petunjuk: ${item.explanationTip}",
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
                        colors = ButtonDefaults.buttonColors(containerColor = SoftOrangeReading),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Soal Selanjutnya ➡️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        ConfettiCanvas(isVisible = isAnswered && isCorrect)
    }
}
}
}

fun getAlphabetCards(): List<LetterCard> {
    return listOf(
        LetterCard("A", "Apel", "A-pel", "🍎", Color(0xFFE53935)),
        LetterCard("B", "Bola", "Bo-la", "⚽", Color(0xFF1E88E5)),
        LetterCard("C", "Cangkir", "Cang-kir", "☕", Color(0xFF43A047)),
        LetterCard("D", "Daun", "Da-un", "🍃", Color(0xFFFB8C00)),
        LetterCard("E", "Elang", "E-lang", "🦅", Color(0xFF8E24AA)),
        LetterCard("F", "Foto", "Fo-to", "📷", Color(0xFF00ACC1)),
        LetterCard("G", "Gajah", "Ga-jah", "🐘", Color(0xFF3949AB)),
        LetterCard("H", "Harimau", "Ha-ri-mau", "🐅", Color(0xFFFFB300)),
        LetterCard("I", "Ikan", "I-kan", "🐟", Color(0xFF039BE5)),
        LetterCard("J", "Jeruk", "Je-ruk", "🍊", Color(0xFFF57C00)),
        LetterCard("K", "Kucing", "Ku-cing", "🐱", Color(0xFFD81B60)),
        LetterCard("L", "Lilin", "Li-lin", "🕯️", Color(0xFF5E35B1)),
        LetterCard("M", "Mama", "Ma-ma", "👩", Color(0xFFE53935)),
        LetterCard("N", "Nanas", "Na-nas", "🍍", Color(0xFF7CB342)),
        LetterCard("O", "Orangutan", "O-rang-u-tan", "🦧", Color(0xFF8D6E63)),
        LetterCard("P", "Pisang", "Pi-sang", "🍌", Color(0xFFFDD835)),
        LetterCard("Q", "Qori", "Qo-ri", "📖", Color(0xFF00897B)),
        LetterCard("R", "Rumah", "Ru-mah", "🏠", Color(0xFFC0CA33)),
        LetterCard("S", "Sapi", "Sa-pi", "🐮", Color(0xFF5C6BC0)),
        LetterCard("T", "Topi", "To-pi", "🧢", Color(0xFF26A69A)),
        LetterCard("U", "Udang", "U-dang", "🦐", Color(0xFFFF7043)),
        LetterCard("V", "Vatikan", "Va-si", "🏺", Color(0xFFAB47BC)),
        LetterCard("W", "Wortel", "Wor-tel", "🥕", Color(0xFFFFA726)),
        LetterCard("X", "Xilofon", "Xi-lo-fon", "🎼", Color(0xFF26C6DA)),
        LetterCard("Y", "Yoyo", "Yo-yo", "🪀", Color(0xFFEC407A)),
        LetterCard("Z", "Zebra", "Ze-bra", "🦓", Color(0xFF78909C))
    )
}

fun getSyllableGroups(): List<SyllableGroup> {
    return listOf(
        SyllableGroup("Suku Kata B", listOf("BA", "BI", "BU", "BE", "BO"), listOf("Baju" to "👕", "Buku" to "📚", "Bola" to "⚽")),
        SyllableGroup("Suku Kata M", listOf("MA", "MI", "MU", "ME", "MO"), listOf("Mama" to "👩", "Meja" to "🪑", "Mobil" to "🚗")),
        SyllableGroup("Suku Kata P", listOf("PA", "PI", "PU", "PE", "PO"), listOf("Papa" to "👨", "Pintu" to "🚪", "Pisang" to "🍌")),
        SyllableGroup("Suku Kata K", listOf("KA", "KI", "KU", "KE", "KO"), listOf("Kaki" to "🦶", "Kuda" to "🐴", "Kopi" to "☕")),
        SyllableGroup("Suku Kata S", listOf("SA", "SI", "SU", "SE", "SO"), listOf("Sapi" to "🐮", "Susu" to "🥛", "Sepatu" to "👟"))
    )
}

fun getRichReadingQuestions(): List<ReadingQuestion> {
    return listOf(
        ReadingQuestion("Tebak Gambar & Kata", "Gambar apakah ini?", "🍎", listOf("Apel", "Jeruk", "Pisang", "Nanas"), "Apel", "A-P-E-L mengeja Apel"),
        ReadingQuestion("Lengkapi Suku Kata", "Lengkapi suku kata: KA - ___", "🦶", listOf("KI", "KU", "KE", "KO"), "KI", "KA + KI menjadi KAKI"),
        ReadingQuestion("Tebak Gambar & Kata", "Pilih kata yang sesuai dengan gambar ini:", "⚽", listOf("Bola", "Buku", "Baju", "Batu"), "Bola", "BO + LA menjadi BOLA"),
        ReadingQuestion("Lengkapi Suku Kata", "Lengkapi kata: MA - ___", "👩", listOf("MA", "PA", "TA", "SA"), "MA", "MA + MA menjadi MAMA"),
        ReadingQuestion("Lengkapi Suku Kata", "Lengkapi suku kata: SU - ___", "🥛", listOf("SU", "SA", "SI", "SO"), "SU", "SU + SU menjadi SUSU"),
        ReadingQuestion("Mengenal Huruf Depan", "Manakah kata yang diawali dengan huruf 'K'?", "🐱", listOf("Kucing", "Gajah", "Sapi", "Babi"), "Kucing", "K-U-C-I-N-G berawalan huruf K"),
        ReadingQuestion("Mengenal Huruf Depan", "Manakah kata yang diawali dengan huruf 'B'?", "📚", listOf("Buku", "Topi", "Meja", "Pintu"), "Buku", "B-U-K-U berawalan huruf B"),
        ReadingQuestion("Tebak Gambar & Kata", "Gambar hewan apakah ini?", "🐘", listOf("Gajah", "Jerapah", "Kuda", "Sapi"), "Gajah", "G-A-J-A-H mengeja Gajah"),
        ReadingQuestion("Lengkapi Suku Kata", "Lengkapi suku kata: PI - ___", "🍌", listOf("SANG", "KIR", "PA", "TA"), "SANG", "PI + SANG menjadi PISANG"),
        ReadingQuestion("Lengkapi Suku Kata", "Lengkapi suku kata: MO - ___", "🚗", listOf("BIL", "TOR", "TAR", "PA"), "BIL", "MO + BIL menjadi MOBIL"),
        ReadingQuestion("Lengkapi Suku Kata", "Lengkapi suku kata: TO - ___", "🧢", listOf("PI", "PA", "PU", "PE"), "PI", "TO + PI menjadi TOPI"),
        ReadingQuestion("Lengkapi Suku Kata", "Lengkapi suku kata: KU - ___", "🐴", listOf("DA", "DU", "DI", "DO"), "DA", "KU + DA menjadi KUDA"),
        ReadingQuestion("Cerita Pendek & Pemahaman", "Kucing Budi berwarna putih. Kucing Budi sangat suka minum susu. Pertanyaan: Apa warna kucing Budi?", "🐱", listOf("Putih", "Hitam", "Cokelat", "Kuning"), "Putih", "Di dalam cerita tertulis: 'Kucing Budi berwarna putih'"),
        ReadingQuestion("Cerita Pendek & Pemahaman", "Budi pergi ke sekolah naik sepeda warna merah. Pertanyaan: Apa warna sepeda Budi?", "🚲", listOf("Merah", "Biru", "Hijau", "Kuning"), "Merah", "Di dalam cerita tertulis: 'sepeda warna merah'"),
        ReadingQuestion("Cerita Pendek & Pemahaman", "Ani suka membaca buku di perpustakaan bersama kawan. Pertanyaan: Di mana Ani membaca buku?", "🏫", listOf("Perpustakaan", "Taman", "Pasar", "Pantai"), "Perpustakaan", "Di dalam cerita: 'membaca buku di perpustakaan'")
    )
}
