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
                title = { Text("Anka Games 🌟", fontWeight = FontWeight.Bold) },
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
                        text = "Anka Games",
                        fontSize = if (isLandscape) 20.sp else 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Kuis & Mini Games Edukasi Anak",
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

@Composable
fun BrainGamesScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧠 Brain Games", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Pencocokan memori, bayangan & urutan gambar", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Kembali ke Home")
        }
    }
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
