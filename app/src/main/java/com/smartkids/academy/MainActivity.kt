package com.smartkids.academy

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartkids.academy.ui.theme.*
import com.smartkids.academy.ui.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.smartkids.academy.ui.components.SoundManager.startBackgroundMusic(this)
        setContent {
            IPSmartKidsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.smartkids.academy.ui.components.SoundManager.resumeBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        com.smartkids.academy.ui.components.SoundManager.pauseBackgroundMusic()
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Math : Screen("math")
    object Reading : Screen("reading")
    object Writing : Screen("writing")
    object BrainGames : Screen("brain_games")
    object Rewards : Screen("rewards")
    object Settings : Screen("settings")
    object ParentDashboard : Screen("parent")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController) }
        composable(Screen.Math.route) { MathScreen(navController) }
        composable(Screen.Reading.route) { ReadingScreen(navController) }
        composable(Screen.Writing.route) { WritingScreen(navController) }
        composable(Screen.BrainGames.route) { BrainGamesScreen(navController) }
        composable(Screen.Rewards.route) { RewardsScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.ParentDashboard.route) { ParentDashboardScreen(navController) }
    }
}

data class NavigationItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var appSettings by remember { mutableStateOf(com.smartkids.academy.data.remote.AppSettings()) }

    LaunchedEffect(Unit) {
        val remoteSettings = com.smartkids.academy.data.remote.QuizApiService.fetchAppSettings()
        if (remoteSettings != null) {
            appSettings = remoteSettings
        }
    }

    val menuItems = listOf(
        NavigationItem("🎮 Kuis Matematika", "Kuis angka & hitungan visual", Icons.Default.Add, SoftBluePrimary, SoftBlueSecondary, Screen.Math.route),
        NavigationItem("🎯 Kuis Membaca", "Tebak kata & gambar interaktif", Icons.Default.Create, SoftGreenMath, SoftGreenMath.copy(alpha = 0.8f), Screen.Reading.route),
        NavigationItem("✏️ Game Menulis", "Latihan menulis huruf & angka", Icons.Default.Edit, SoftOrangeReading, SoftOrangeReading.copy(alpha = 0.8f), Screen.Writing.route),
        NavigationItem("🧠 Brain Games", "Pencocokan memori & pola", Icons.Default.Star, SoftPurpleWriting, SoftPurpleWriting.copy(alpha = 0.8f), Screen.BrainGames.route),
        NavigationItem("⭐ Toko Hadiah", "Tukar bintang dengan stiker", Icons.Default.ShoppingCart, SoftPinkBrain, SoftPinkBrain.copy(alpha = 0.8f), Screen.Rewards.route),
        NavigationItem("📈 Parent Dashboard", "Laporan belajar & pengaturan", Icons.Default.Person, DarkText.copy(alpha = 0.6f), DarkText, Screen.ParentDashboard.route)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appSettings.headerTitle, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = if (isLandscape) 16.dp else 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo Banner (Responsive size)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isLandscape) 4.dp else 10.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_anka_logo),
                    contentDescription = "Anka Games Logo",
                    modifier = Modifier
                        .size(if (isLandscape) 46.dp else 60.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = appSettings.homeTitle,
                        fontSize = if (isLandscape) 20.sp else 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = appSettings.homeSubtitle,
                        fontSize = if (isLandscape) 12.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkText.copy(alpha = 0.7f)
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (isLandscape) 220.dp else 150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(if (isLandscape) 4.dp else 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(menuItems) { item ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 100.dp else 120.dp)
                            .clickable { navController.navigate(item.route) },
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(item.startColor, item.endColor)))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(if (isLandscape) 40.dp else 48.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = if (isLandscape) 16.sp else 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.description,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = if (isLandscape) 12.sp else 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class MemoryCardState(
    val id: Int,
    val emoji: String,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

data class PatternQuizQuestion(
    val title: String,
    val questionText: String,
    val emoji: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainGamesScreen(navController: NavController) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧠 Brain Games - Asah Otak", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
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
                .background(Color(0xFFF3E5F5))
                .padding(12.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab, containerColor = Color.White) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🧩 Memori 4x4 (16 Kartu)", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🔮 Pola & Logika", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                MemoryGameGrid4x4()
            } else {
                PatternLogicQuizView()
            }
        }
    }
}

@Composable
fun MemoryGameGrid4x4() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val emojisPool = remember { listOf("🦄", "🦊", "🦕", "🐝", "🦁", "🐼", "🐬", "🦀") }

    fun generateCards(): List<MemoryCardState> {
        val pairs = (emojisPool + emojisPool).shuffled()
        return pairs.mapIndexed { index, emoji ->
            MemoryCardState(id = index, emoji = emoji)
        }
    }

    var cards by remember { mutableStateOf(generateCards()) }
    var flippedIndices by remember { mutableStateOf<List<Int>>(emptyList()) }
    var matchedPairsCount by remember { mutableIntStateOf(0) }
    var moveCount by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pasangan: $matchedPairsCount / 8", fontWeight = FontWeight.Bold, color = SoftPurpleWriting)
            Text("Langkah: $moveCount", fontWeight = FontWeight.Bold, color = DarkText)
            IconButton(onClick = {
                cards = generateCards()
                flippedIndices = emptyList()
                matchedPairsCount = 0
                moveCount = 0
            }) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = SoftPurpleWriting)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (matchedPairsCount == 8) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🧠 DAYA INGAT HEBAT! 🏆", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selesai dalam $moveCount langkah!", fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            cards = generateCards()
                            flippedIndices = emptyList()
                            matchedPairsCount = 0
                            moveCount = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPurpleWriting)
                    ) {
                        Text("Main Lagi 🔄", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cards.size) { index ->
                    val card = cards[index]
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (card.isMatched) Color(0xFFE8F5E9)
                            else if (card.isFlipped) Color.White
                            else SoftPurpleWriting
                        ),
                        elevation = CardDefaults.cardElevation(4.dp),
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clickable {
                                if (isProcessing || card.isFlipped || card.isMatched || flippedIndices.size >= 2) return@clickable

                                val newCards = cards.toMutableList()
                                newCards[index] = newCards[index].copy(isFlipped = true)
                                cards = newCards

                                val newFlipped = flippedIndices + index
                                flippedIndices = newFlipped

                                if (newFlipped.size == 2) {
                                    moveCount++
                                    val idx1 = newFlipped[0]
                                    val idx2 = newFlipped[1]

                                    if (cards[idx1].emoji == cards[idx2].emoji) {
                                        com.smartkids.academy.ui.components.SoundManager.playCorrectSound(context)
                                        val matchCards = cards.toMutableList()
                                        matchCards[idx1] = matchCards[idx1].copy(isMatched = true)
                                        matchCards[idx2] = matchCards[idx2].copy(isMatched = true)
                                        cards = matchCards
                                        matchedPairsCount++
                                        flippedIndices = emptyList()
                                    } else {
                                        isProcessing = true
                                        com.smartkids.academy.ui.components.SoundManager.playWrongSound(context)
                                        kotlinx.coroutines.GlobalScope.launch {
                                            kotlinx.coroutines.delay(800)
                                            val unflipCards = cards.toMutableList()
                                            unflipCards[idx1] = unflipCards[idx1].copy(isFlipped = false)
                                            unflipCards[idx2] = unflipCards[idx2].copy(isFlipped = false)
                                            cards = unflipCards
                                            flippedIndices = emptyList()
                                            isProcessing = false
                                        }
                                    }
                                }
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (card.isFlipped || card.isMatched) {
                                Text(card.emoji, fontSize = 28.sp)
                            } else {
                                Text("❓", fontSize = 24.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PatternLogicQuizView() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val questions = remember { getPatternQuizQuestions() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    val q = questions.getOrNull(currentIndex)

    if (currentIndex >= questions.size) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧠 KUIS LOGIKA SELESAI! 🎉", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Skor Logika Otak: $score / ${questions.size * 10}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SoftPurpleWriting)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        currentIndex = 0
                        score = 0
                        isAnswered = false
                        selectedOption = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftPurpleWriting)
                ) {
                    Text("Ulangi Kuis Logika 🔄", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        q?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Skor Logika: $score", fontWeight = FontWeight.Bold, color = SoftPurpleWriting)
                    Text("Soal ${currentIndex + 1} / ${questions.size}", fontWeight = FontWeight.Bold, color = DarkText)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SoftPurpleWriting)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (item.emoji.isNotEmpty()) {
                            Text(item.emoji, fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            item.questionText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = DarkText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                item.options.forEach { option ->
                    val buttonColor = when {
                        !isAnswered -> SoftPurpleWriting
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
                                    com.smartkids.academy.ui.components.SoundManager.playCorrectSound(context)
                                } else {
                                    com.smartkids.academy.ui.components.SoundManager.playWrongSound(context)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp).padding(vertical = 3.dp)
                    ) {
                        Text(option, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isAnswered) {
                    Button(
                        onClick = {
                            isAnswered = false
                            selectedOption = null
                            currentIndex++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftPurpleWriting),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Soal Selanjutnya ➡️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun getPatternQuizQuestions(): List<PatternQuizQuestion> {
    return listOf(
        PatternQuizQuestion("Pola Warna Simbol", "Perhatikan urutan pola berikut:\n\n🔴 🔵 🟡 | 🔴 🔵 ___ ?\n\nSimbol manakah yang melengkapi pola?", "🔮", listOf("🟡", "🔴", "🔵", "🟢"), "🟡", "Pola berulang: Merah, Biru, Kuning"),
        PatternQuizQuestion("Pola Deret Hewan", "Perhatikan pola urutan hewan:\n\n🐱 🐶 🐰 | 🐱 🐶 ___ ?\n\nHewan manakah selanjutnya?", "🐾", listOf("🐰", "🐱", "🐶", "🦁"), "🐰", "Pola berulang: Kucing, Anjing, Kelinci"),
        PatternQuizQuestion("Pola Matriks Analogi", "Jika 🐱 -> 🐟 (Kucing makan Ikan),\nmaka 🐰 -> ___ ? (Kelinci makan...)", "🔍", listOf("🥕 (Wortel)", "🍌 (Pisang)", "🍖 (Daging)", "🧀 (Keju)"), "🥕 (Wortel)", "Kelinci sangat menyukai wortel"),
        PatternQuizQuestion("Pola Matriks Analogi", "Jika 🚗 -> 🛣️ (Mobil berjalan di Jalan),\nmaka ✈️ -> ___ ? (Pesawat terbang di...)", "🚀", listOf("☁️ (Udara / Langit)", "🌊 (Laut)", "🚆 (Rel)", "🏠 (Rumah)"), "☁️ (Udara / Langit)", "Pesawat terbang di udara/langit"),
        PatternQuizQuestion("Logika Cermin / Bayangan", "Manakah bayangan cermin yang tepat dari panah arah ↗️ ?", "📐", listOf("↖️", "↘️", "↙️", "⬆️"), "↖️", "Cermin horizontal membalikkan kanan menjadi kiri"),
        PatternQuizQuestion("Logika Deret Angka", "Perhatikan deret angka urutan melompat berikut:\n\n2, 4, 6, 8, 10, ___ ?\n\nBerapakah angka berikutnya?", "🔢", listOf("12", "11", "14", "13"), "12", "Deret bertambah +2 pada setiap langkah"),
        PatternQuizQuestion("Logika Deret Angka", "Perhatikan deret angka berikut:\n\n5, 10, 15, 20, ___ ?\n\nBerapakah angka berikutnya?", "🔢", listOf("25", "22", "30", "24"), "25", "Deret bertambah +5 pada setiap langkah"),
        PatternQuizQuestion("Logika Perbandingan", "Manakah nilai yang paling BESAR di antara pilihan berikut?", "⚖️", listOf("5 × 5 (25)", "3 × 8 (24)", "4 × 6 (24)", "2 × 10 (20)"), "5 × 5 (25)", "5 × 5 = 25 adalah nilai tertinggi"),
        PatternQuizQuestion("Logika Hubungan Benda", "Sendok berhubungan dengan Piring, seperti halnya Pensil berhubungan dengan...", "✏️", listOf("Kertas / Buku Tulis", "Sepatu", "Pintu", "Topi"), "Kertas / Buku Tulis", "Pensil digunakan untuk menulis di kertas"),
        PatternQuizQuestion("Logika Urutan Waktu", "Jika HARI INI adalah hari Rabu, maka DUA HARI LAGI adalah hari...", "📅", listOf("Jumat", "Kamis", "Sabtu", "Minggu"), "Jumat", "Rabu + 2 hari = Jumat")
    )
}

@Composable
fun RewardsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⭐ Toko Hadiah", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Kumpulkan bintang untuk tukar avatar (🦁, 🐰, 🐼) & stiker!", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Kembali ke Home")
        }
    }
}

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚙️ Pengaturan App", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Kembali ke Home")
        }
    }
}

@Composable
fun ParentDashboardScreen(navController: NavController) {
    var isPinVerified by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }

    if (!isPinVerified) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("👨‍👩‍👧 Parent Dashboard", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Masukkan PIN Orang Tua (Default: 1234)", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = enteredPin,
                onValueChange = { enteredPin = it },
                label = { Text("PIN") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    if (enteredPin == "1234") {
                        isPinVerified = true
                    } else {
                        enteredPin = ""
                    }
                }) {
                    Text("Verifikasi")
                }
                Button(onClick = { navController.navigateUp() }) {
                    Text("Batal")
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📈 Analitik Belajar Anak", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Waktu Main Kuis: 2 jam | Streak Harian: 4 hari", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { isPinVerified = false; enteredPin = "" }) {
                Text("Kunci Dashboard")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigateUp() }) {
                Text("Kembali ke Home")
            }
        }
    }
}
