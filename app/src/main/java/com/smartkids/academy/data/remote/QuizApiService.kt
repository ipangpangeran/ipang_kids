package com.smartkids.academy.data.remote

import com.smartkids.academy.ui.screens.MathCategory
import com.smartkids.academy.ui.screens.MathQuestion
import com.smartkids.academy.ui.screens.QuizDifficulty
import com.smartkids.academy.ui.screens.ReadingQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppSettings(
    val headerTitle: String = "Anka Games 🌟",
    val homeTitle: String = "Anka Games",
    val homeSubtitle: String = "Kuis & Mini Games Edukasi Anak"
)

enum class BrainCategory(val title: String, val icon: String) {
    MEMORY_MATCH("Pencocokan Memori", "🃏"),
    PATTERN_SEQUENCE("Pola & Urutan", "🧩"),
    SHADOW_MATCH("Tebak Bayangan", "👤")
}

data class BrainQuestion(
    val id: Int = 0,
    val category: BrainCategory,
    val difficulty: QuizDifficulty,
    val questionText: String,
    val emojiSet: List<String>,
    val options: List<String>,
    val correctAnswer: String,
    val explanationTip: String = ""
)

object QuizApiService {
    private const val BASE_URL = "https://api.ipangpangeran.com"

    suspend fun fetchAppSettings(): AppSettings? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                val url = URL("$BASE_URL/api/settings?_t=$timestamp")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                    useCaches = false
                    setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                    setRequestProperty("Pragma", "no-cache")
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    if (json.optBoolean("success")) {
                        val dataObj = json.getJSONObject("data")
                        return@withContext AppSettings(
                            headerTitle = dataObj.optString("headerTitle", "Anka Games 🌟"),
                            homeTitle = dataObj.optString("homeTitle", "Anka Games"),
                            homeSubtitle = dataObj.optString("homeSubtitle", "Kuis & Mini Games Edukasi Anak")
                        )
                    }
                }
            } catch (_: Exception) {}
            return@withContext null
        }
    }

    suspend fun fetchMathQuestions(category: MathCategory, difficulty: QuizDifficulty): List<MathQuestion>? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                val urlString = "$BASE_URL/api/math?_t=$timestamp"
                val url = URL(urlString)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                    useCaches = false
                    setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                    setRequestProperty("Pragma", "no-cache")
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    if (json.optBoolean("success")) {
                        val array = json.getJSONArray("data")
                        val list = mutableListOf<MathQuestion>()

                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            val catName = obj.optString("category", "ADDITION")
                            val diffName = obj.optString("difficulty", "EASY")
                            val catEnum = try { MathCategory.valueOf(catName) } catch (_: Exception) { MathCategory.ADDITION }
                            val diffEnum = try { QuizDifficulty.valueOf(diffName) } catch (_: Exception) { QuizDifficulty.EASY }

                            val optsArray = obj.getJSONArray("options")
                            val options = listOf(
                                optsArray.optInt(0, 0),
                                optsArray.optInt(1, 0),
                                optsArray.optInt(2, 0),
                                optsArray.optInt(3, 0)
                            )

                            val q = MathQuestion(
                                category = catEnum,
                                difficulty = diffEnum,
                                questionText = obj.getString("questionText"),
                                visualRepresentation = obj.optString("visualRepresentation", ""),
                                options = options,
                                correctAnswer = obj.optInt("correctAnswer", options[0]),
                                explanationTip = obj.optString("explanationTip", "")
                            )

                            val categoryMatches = (category == MathCategory.MIXED || q.category == category)
                            val difficultyMatches = (difficulty == QuizDifficulty.ALL || q.difficulty == difficulty)

                            if (categoryMatches && difficultyMatches) {
                                list.add(q)
                            }
                        }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            } catch (_: Exception) {}
            return@withContext null
        }
    }

    suspend fun fetchReadingQuestions(): List<ReadingQuestion>? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                val url = URL("$BASE_URL/api/reading?_t=$timestamp")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                    useCaches = false
                    setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                    setRequestProperty("Pragma", "no-cache")
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    if (json.optBoolean("success")) {
                        val array = json.getJSONArray("data")
                        val list = mutableListOf<ReadingQuestion>()

                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            val optsArray = obj.getJSONArray("options")
                            val options = listOf(
                                optsArray.optString(0, ""),
                                optsArray.optString(1, ""),
                                optsArray.optString(2, ""),
                                optsArray.optString(3, "")
                            )

                            list.add(
                                ReadingQuestion(
                                    categoryTitle = obj.optString("categoryTitle", "Tebak Gambar & Kata"),
                                    questionText = obj.getString("questionText"),
                                    emoji = obj.optString("emoji", ""),
                                    options = options,
                                    correctAnswer = obj.optString("correctAnswer", options[0]),
                                    explanationTip = obj.optString("explanationTip", "")
                                )
                            )
                        }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            } catch (_: Exception) {}
            return@withContext null
        }
    }

    suspend fun fetchBrainQuestions(): List<BrainQuestion>? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = System.currentTimeMillis()
                val url = URL("$BASE_URL/api/brain?_t=$timestamp")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 4000
                    readTimeout = 4000
                    useCaches = false
                    setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                    setRequestProperty("Pragma", "no-cache")
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    if (json.optBoolean("success")) {
                        val array = json.getJSONArray("data")
                        val list = mutableListOf<BrainQuestion>()

                        for (i in 0 until array.length()) {
                            val obj = array.getJSONObject(i)
                            val catName = obj.optString("category", "MEMORY_MATCH")
                            val diffName = obj.optString("difficulty", "EASY")

                            val catEnum = try { BrainCategory.valueOf(catName) } catch (_: Exception) { BrainCategory.MEMORY_MATCH }
                            val diffEnum = try { QuizDifficulty.valueOf(diffName) } catch (_: Exception) { QuizDifficulty.EASY }

                            val emojiArray = obj.optJSONArray("emojiSet")
                            val emojiSet = mutableListOf<String>()
                            if (emojiArray != null) {
                                for (e in 0 until emojiArray.length()) {
                                    emojiSet.add(emojiArray.optString(e, ""))
                                }
                            }

                            val optsArray = obj.getJSONArray("options")
                            val options = listOf(
                                optsArray.optString(0, ""),
                                optsArray.optString(1, ""),
                                optsArray.optString(2, ""),
                                optsArray.optString(3, "")
                            )

                            list.add(
                                BrainQuestion(
                                    id = obj.optInt("id", 0),
                                    category = catEnum,
                                    difficulty = diffEnum,
                                    questionText = obj.getString("questionText"),
                                    emojiSet = emojiSet,
                                    options = options,
                                    correctAnswer = obj.optString("correctAnswer", options[0]),
                                    explanationTip = obj.optString("explanationTip", "")
                                )
                            )
                        }
                        if (list.isNotEmpty()) return@withContext list
                    }
                }
            } catch (_: Exception) {}
            return@withContext null
        }
    }
}
