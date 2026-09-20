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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🎯 Kuis & Game Membaca", fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFFF8F0))
        ) {
            ReadingQuizView()
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
                                SoundManager.playCorrectSound(context)
                            } else {
                                SoundManager.playWrongSound(context)
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
        ReadingQuestion("Cerita & Pemahaman Kritis", "Dimas menemukan dompet di gerbang sekolah. Dimas tidak mengambil uangnya, melainkan memberikan dompet itu ke Satpam sekolah agar dikembalikan kepada pemiliknya.\n\nPertanyaan: Sikap terpuji apakah yang ditunjukkan Dimas?", "👛", listOf("Jujur dan Amanah", "Penakut", "Pelit", "Pemalas"), "Jujur dan Amanah", "Dimas mengembalikan barang yang bukan miliknya"),
        ReadingQuestion("Cerita & Pemahaman Kritis", "Tumbuhan hijau memerlukan cahaya matahari, air, dan udara untuk mengolah makanannya. Proses ini menghasilkan oksigen yang dihirup manusia dan hewan.\n\nPertanyaan: Apakah nama proses pembuatan makanan pada tumbuhan hijau?", "🌱", listOf("Fotosintesis", "Respirasi", "Penguapan", "Pencernaan"), "Fotosintesis", "Pembuatan makanan pada tumbuhan disebut fotosintesis"),
        ReadingQuestion("Cerita & Pemahaman Kritis", "Tumbuhan hijau memerlukan cahaya matahari, air, dan udara untuk mengolah makanannya. Proses ini menghasilkan oksigen yang dihirup manusia dan hewan.\n\nPertanyaan: Gas apakah yang dihasilkan oleh tumbuhan dari proses tersebut?", "🍃", listOf("Oksigen", "Karbondioksida", "Nitrogen", "Asap"), "Oksigen", "Tumbuhan menghasilkan oksigen bagi makhluk hidup"),
        ReadingQuestion("Cerita & Pemahaman Kritis", "Rani berlatih renang setiap sore tanpa kenal lelah. Walaupun pernah kalah dalam lomba, Rani terus berlatih hingga akhirnya menjadi juara pertama tingkat nasional.\n\nPertanyaan: Pesan moral apakah yang bisa kita petik dari kisah Rani?", "🏆", listOf("Pantang menyerah membawa keberhasilan", "Kekalahan adalah akhir dari segalanya", "Tidak perlu berlatih jika sudah pintar", "Juara diperoleh tanpa kerja keras"), "Pantang menyerah membawa keberhasilan", "Kegigihan Rani membuahkan hasil juara"),
        ReadingQuestion("Ungkapan & Idiom", "Siswa yang sering membantu temannya tanpa mengharap balasan dikenal sebagai anak yang 'RINGAN TANGAN'.\n\nApa arti ungkapan 'ringan tangan'?", "🤝", listOf("Suka menolong", "Suka memukul", "Tangannya enteng", "Suka mencuri"), "Suka menolong", "Ringan tangan artinya suka menolong"),
        ReadingQuestion("Ungkapan & Idiom", "Robi menjadi 'BINTANG LAPANGAN' pada pertandingan sepak bola kemarin karena mencetak 3 gol.\n\nApa arti ungkapan 'bintang lapangan'?", "⚽", listOf("Pemain terbaik / paling menonjol", "Bintang yang jatuh di lapangan", "Penonton paling heboh", "Wasit pertandingan"), "Pemain terbaik / paling menonjol", "Bintang lapangan adalah pemain yang paling menonjol/hebat"),
        ReadingQuestion("Ungkapan & Idiom", "Meskipun dari keluarga kaya dan berprestasi, Sinta tidak pernah 'BESAR KEPALA'.\n\nApa arti ungkapan 'besar kepala'?", "🧠", listOf("Sombong / Angkuh", "Kepalanya berukuran besar", "Pintar", "Penyayang"), "Sombong / Angkuh", "Besar kepala artinya sombong"),
        ReadingQuestion("Ungkapan & Idiom", "Karena persaingan bisnis yang ketat, toko baju itu akhirnya 'GULUNG TIKAR'.\n\nApa arti ungkapan 'gulung tikar'?", "🏪", listOf("Bangkrut / Tutup usaha", "Merapikan tikar", "Pindah rumah", "Menjual karpet"), "Bangkrut / Tutup usaha", "Gulung tikar artinya bangkrut"),
        ReadingQuestion("Susun Kalimat SPOK", "Susun kata acak ini menjadi kalimat yang benar:\n\n*membaca - di - Ayah - ruang - koran - tamu*", "📰", listOf("Ayah membaca koran di ruang tamu", "Koran membaca Ayah di ruang tamu", "Di ruang tamu koran membaca Ayah", "Ayah di ruang tamu membaca koran"), "Ayah membaca koran di ruang tamu", "Struktur SPOK: Subjek (Ayah) + Predikat (membaca) + Objek (koran) + Keterangan Tempat (di ruang tamu)"),
        ReadingQuestion("Susun Kalimat SPOK", "Susun kata acak ini menjadi kalimat yang benar:\n\n*lezat - Ibu - kue - memasak - di - dapur*", "🎂", listOf("Ibu memasak kue lezat di dapur", "Kue lezat memasak Ibu di dapur", "Di dapur kue lezat memasak Ibu", "Ibu di dapur kue lezat memasak"), "Ibu memasak kue lezat di dapur", "Subjek (Ibu) + Predikat (memasak) + Objek (kue lezat) + Keterangan (di dapur)"),
        ReadingQuestion("Susun Kalimat SPOK", "Susun kata acak ini menjadi kalimat yang benar:\n\n*sepeda - menaiki - adik - ke - baru - sekolah*", "🚲", listOf("Adik menaiki sepeda baru ke sekolah", "Sepeda baru menaiki adik ke sekolah", "Ke sekolah sepeda baru menaiki adik", "Adik ke sekolah sepeda baru menaiki"), "Adik menaiki sepeda baru ke sekolah", "Adik (Subjek) + menaiki (Predikat) + sepeda baru (Objek) + ke sekolah (Keterangan)"),
        ReadingQuestion("Tata Bahasa & Imbuhan", "Pilihlah kata berimbuhan yang tepat untuk melengkapi kalimat:\n\n'Petugas pemadam kebakaran sedang ___ kobaran api di perumahan.'", "🚒", listOf("memadamkan", "dipadamkan", "pemadam", "terpadam"), "memadamkan", "Kata kerja aktif transitif menggunakan imbuhan me-kan"),
        ReadingQuestion("Tata Bahasa & Imbuhan", "Pilihlah kata berimbuhan yang tepat:\n\n'Kakak sedang ___ pakaian adik yang robek dengan jarum dan benang.'", "🪡", listOf("menjahit", "penjahit", "dijahitkan", "terjahit"), "menjahit", "Tindakan aktif memerlukan kata kerja 'menjahit'"),
        ReadingQuestion("Tata Bahasa & Imbuhan", "Manakah bentuk kata baku dalam Bahasa Indonesia yang benar?", "✏️", listOf("Apotek", "Apotik", "Apoteq", "Apotick"), "Apotek", "Bentuk baku menurut KBBI adalah Apotek (dengan huruf e)"),
        ReadingQuestion("Tata Bahasa & Imbuhan", "Manakah bentuk kata baku dalam Bahasa Indonesia yang benar?", "📜", listOf("Ijazah", "Ijasah", "Izajah", "Idjasah"), "Ijazah", "Bentuk baku menurut KBBI adalah Ijazah (menggunakan z)"),
        ReadingQuestion("Sinonim Level Tinggi", "Apakah persamaan kata (sinonim) dari kata 'DERMAWAN'?", "🎁", listOf("Suka memberi / Pemurah", "Hemat", "Pelit", "Pemberani"), "Suka memberi / Pemurah", "Dermawan berarti pemurah hati atau suka memberi"),
        ReadingQuestion("Sinonim Level Tinggi", "Apakah persamaan kata (sinonim) dari kata 'PARAS'?", "✨", listOf("Wajah / Muka", "Pakaian", "Harta", "Suara"), "Wajah / Muka", "Paras artinya wajah atau rupa"),
        ReadingQuestion("Sinonim Level Tinggi", "Apakah persamaan kata (sinonim) dari kata 'LESTARI'?", "🌿", listOf("Abadi / Kekal", "Cepat rusak", "Sementara", "Musnah"), "Abadi / Kekal", "Lestari artinya bertahan/kekal"),
        ReadingQuestion("Antonim Level Tinggi", "Apakah lawan kata (antonim) dari kata 'GIGIH'?", "🛡️", listOf("Mudah menyerah / Putus asa", "Tekun", "Semangat", "Kuat"), "Mudah menyerah / Putus asa", "Gigih berlawanan dengan mudah menyerah"),
        ReadingQuestion("Antonim Level Tinggi", "Apakah lawan kata (antonim) dari kata 'PELIT'?", "💰", listOf("Dermawan", "Kecil", "Ragu", "Takut"), "Dermawan", "Pelit berlawanan dengan dermawan"),
        ReadingQuestion("Antonim Level Tinggi", "Apakah lawan kata (antonim) dari kata 'TENTRAM'?", "🕊️", listOf("Gelisah / Rusuh", "Damai", "Tenang", "Aman"), "Gelisah / Rusuh", "Tentram berlawanan dengan gelisah/rusuh"),
        ReadingQuestion("Analisis Paragraf Rumpang", "Bacalah kalimat rumpang berikut:\n\n'Hutan Bakau sangat bermanfaat untuk mencegah ___ pantai dari hantaman gelombang laut.'\n\nKata yang paling tepat untuk mengisi titik-titik adalah...", "🌊", listOf("Erosi / Abrasi", "Banjir", "Tsunami", "Longsor"), "Erosi / Abrasi", "Pengikisan pantai oleh gelombang laut disebut abrasi/erosi pantai"),
        ReadingQuestion("Analisis Paragraf Rumpang", "Bacalah kalimat berikut:\n\n'Indonesia adalah negara kepulauan yang kaya akan keanekaragaman suku dan budaya, namun tetap bersatu sesuai semboyan ___.'", "🇮🇩", listOf("Bhinneka Tunggal Ika", "Tut Wuri Handayani", "Garuda Pancasila", "Indonesiaraya"), "Bhinneka Tunggal Ika", "Semboyan persatuan Indonesia adalah Bhinneka Tunggal Ika"),
        ReadingQuestion("Logika & Makna Kata", "Benda ini digunakan untuk menunjuk arah Utara dan Selatan. Benda apakah ini?", "🧭", listOf("Kompas", "Penggaris", "Jam Dinding", "Termometer"), "Kompas", "Kompas adalah penunjuk arah mata angin"),
        ReadingQuestion("Logika & Makna Kata", "Alat medis yang digunakan oleh dokter untuk mendengarkan detak jantung dan suara napas adalah...", "🩺", listOf("Stetoskop", "Mikroskop", "Teleskop", "Termometer"), "Stetoskop", "Dokter menggunakan stetoskop untuk mendengar detak jantung")
    )
}
