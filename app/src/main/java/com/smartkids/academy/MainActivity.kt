package com.smartkids.academy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartkids.academy.ui.theme.*
import com.smartkids.academy.ui.components.HandwritingCanvas

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

// Navigation dashboard card model
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
    val menuItems = listOf(
        NavigationItem("📘 Mathematics", "Numbers & visual calculations", Icons.Default.Add, SoftBluePrimary, SoftBlueSecondary, Screen.Math.route),
        NavigationItem("📖 Reading", "Phonics, letters & simple words", Icons.Default.Book, SoftGreenMath, SoftGreenMath.copy(alpha = 0.8f), Screen.Reading.route),
        NavigationItem("✏️ Writing", "Trace letters, numbers & words", Icons.Default.Edit, SoftOrangeReading, SoftOrangeReading.copy(alpha = 0.8f), Screen.Writing.route),
        NavigationItem("🧠 Brain Games", "Memory match, sequences & patterns", Icons.Default.Star, SoftPurpleWriting, SoftPurpleWriting.copy(alpha = 0.8f), Screen.BrainGames.route),
        NavigationItem("⭐ Rewards Shop", "Spend stars on stickers & stickers", Icons.Default.ShoppingCart, SoftPinkBrain, SoftPinkBrain.copy(alpha = 0.8f), Screen.Rewards.route),
        NavigationItem("📈 Progress / Parent", "Check reports & settings", Icons.Default.Person, DarkText.copy(alpha = 0.6f), DarkText, Screen.ParentDashboard.route)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IP Smart Kids Academy 🌟", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "What do you want to learn today?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkText,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menuItems) { item ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable { navController.navigate(item.route) },
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.horizontalGradient(listOf(item.startColor, item.endColor)))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = Color.White,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = item.title,
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = item.description,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 14.sp
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

// Game module placeholders
@Composable
fun MathScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📘 Mathematics Module", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Addition: 🍎 + 🍎 = 2 🍎s", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Home")
        }
    }
}

@Composable
fun ReadingScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📖 Reading Module", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Spell simple words: B-A-B-I (Babi), K-A-K-I (Kaki)", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Home")
        }
    }
}

@Composable
fun WritingScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✏️ Writing Canvas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { navController.navigateUp() }) {
                Text("Back")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("Trace the alphabet or practice writing numbers below:")
        Spacer(modifier = Modifier.height(8.dp))
        HandwritingCanvas(
            modifier = Modifier.fillMaxSize().weight(1f),
            useGridBackground = false
        )
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
        Text("Memory matching cards, shadow outline matching, pattern sequences", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Home")
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
        Text("⭐ Rewards Shop", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Earn stars in lessons to buy avatars (🦁, 🐰, 🐼) and custom stickers!", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Home")
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
        Text("⚙️ Settings Screen", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { navController.navigateUp() }) {
            Text("Back to Home")
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
            Text("Please enter parent PIN (Default: 1234)", fontSize = 16.sp)
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
                    Text("Verify")
                }
                Button(onClick = { navController.navigateUp() }) {
                    Text("Cancel")
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📈 Learning Analytics & Reports", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Weekly Study Time: 2 hours | Daily Streak: 4 days", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { isPinVerified = false; enteredPin = "" }) {
                Text("Lock Dashboard")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigateUp() }) {
                Text("Back to Home")
            }
        }
    }
}
