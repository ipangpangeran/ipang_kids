package com.smartkids.academy.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.smartkids.academy.domain.usecase.AdaptiveLearningEngine
import com.smartkids.academy.ui.components.ConfettiCanvas
import com.smartkids.academy.ui.components.HandwritingCanvas
import com.smartkids.academy.ui.components.SoundManager
import com.smartkids.academy.ui.theme.*
import kotlinx.coroutines.launch

enum class WritingMode(val title: String, val icon: String) {
    LETTERS("Huruf (A-Z)", "🔤"),
    NUMBERS("Angka (0-9)", "🔢"),
    WORDS("Kata Dasar", "✏️"),
    FREEHAND("Coretan Bebas", "🎨")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context, scope) }

    var selectedMode by remember { mutableStateOf(WritingMode.LETTERS) }
    var selectedTargetText by remember { mutableStateOf("A") }

    var selectedColor by remember { mutableStateOf(Color(0xFF1E88E5)) } // Blue default
    var strokeWidth by remember { mutableFloatStateOf(12f) }
    var isEraseMode by remember { mutableStateOf(false) }
    var showCongratulation by remember { mutableStateOf(false) }

    val lettersList = remember { ('A'..'Z').map { it.toString() } }
    val numbersList = remember { (0..9).map { it.toString() } }
    val wordsList = remember { listOf("BUKU", "MAMA", "PAPA", "BOLA", "SAPI", "SAYA", "ANAK", "KUDA") }

    val colorsList = listOf(
        Color(0xFF1E88E5), // Blue
        Color(0xFFE53935), // Red
        Color(0xFF43A047), // Green
        Color(0xFF8E24AA), // Purple
        Color(0xFFFB8C00), // Orange
        Color(0xFF000000)  // Black
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("✏️ Studio Belajar Menulis", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            showCongratulation = true
                            SoundManager.playStarSound()
                            scope.launch {
                                val currentProgress = db.progressDao().getProgressForSubject("WRITING")
                                    ?: com.smartkids.academy.data.local.entity.ProgressEntity("WRITING", "BEGINNER", 0, 0, 0, 0, 0, System.currentTimeMillis())

                                val updated = AdaptiveLearningEngine.evaluateProgress(
                                    currentProgress = currentProgress,
                                    newCorrectCount = 1,
                                    newTotalQuestions = 1
                                )
                                db.progressDao().insertOrUpdateProgress(updated)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Selesai! ⭐", fontWeight = FontWeight.Bold)
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
        ) {
            // Mode Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WritingMode.values().forEach { mode ->
                    val isSel = selectedMode == mode
                    Button(
                        onClick = {
                            selectedMode = mode
                            selectedTargetText = when (mode) {
                                WritingMode.LETTERS -> "A"
                                WritingMode.NUMBERS -> "1"
                                WritingMode.WORDS -> "BUKU"
                                WritingMode.FREEHAND -> ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) SoftPurpleWriting else Color.White,
                            contentColor = if (isSel) Color.White else SoftPurpleWriting
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text("${mode.icon} ${mode.title.split(" ").first()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Target Items Picker (Letters, Numbers, Words)
            if (selectedMode != WritingMode.FREEHAND) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val list = when (selectedMode) {
                        WritingMode.LETTERS -> lettersList
                        WritingMode.NUMBERS -> numbersList
                        WritingMode.WORDS -> wordsList
                        WritingMode.FREEHAND -> emptyList()
                    }
                    items(list) { item ->
                        val isSelected = selectedTargetText == item
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SoftPurpleWriting else Color.White)
                                .border(1.dp, SoftPurpleWriting, RoundedCornerShape(10.dp))
                                .clickable { selectedTargetText = item }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else SoftPurpleWriting
                            )
                        }
                    }
                }
            }

            // Tools Toolbar (Color Picker & Eraser)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Warna:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    colorsList.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedColor == c && !isEraseMode) 3.dp else 0.dp,
                                    color = DarkText,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedColor = c
                                    isEraseMode = false
                                }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isEraseMode = !isEraseMode }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Penghapus",
                            tint = if (isEraseMode) Color.Red else Color.Gray
                        )
                    }
                }
            }

            // Main Tracing & Writing Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                // Watermark Tracing Outline in Background
                if (selectedMode != WritingMode.FREEHAND && selectedTargetText.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedTargetText,
                            fontSize = if (selectedMode == WritingMode.WORDS) 90.sp else 220.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.LightGray.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Interactive Handwriting Canvas Overlay
                HandwritingCanvas(
                    modifier = Modifier.fillMaxSize(),
                    drawColor = selectedColor,
                    strokeWidth = strokeWidth,
                    isEraseMode = isEraseMode,
                    useGridBackground = (selectedMode == WritingMode.FREEHAND)
                )
            }

            // Celebration Modal/Banner when finished
            AnimatedVisibility(visible = showCongratulation) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🌟", fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Hebat Sekali! Tulisanmu Bagus!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                                Text("Kamu mendapatkan +1 Bintang ⭐", fontSize = 14.sp, color = Color(0xFF388E3C))
                            }
                        }
                        IconButton(onClick = { showCongratulation = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.Gray)
                        }
                    }
                }
            }
            ConfettiCanvas(isVisible = showCongratulation)
        }
    }
}
