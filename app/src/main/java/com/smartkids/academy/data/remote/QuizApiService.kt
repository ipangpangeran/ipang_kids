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

object QuizApiService {
    private const val BASE_URL = "https://api.ipangpangeran.com"

    suspend fun fetchMathQuestions(category: MathCategory, difficulty: QuizDifficulty): List<MathQuestion>? {
        return withContext(Dispatchers.IO) {
            try {
                val urlString = "$BASE_URL/api/math?category=${category.name}&difficulty=${difficulty.name}"
                val url = URL(urlString)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3500
                    readTimeout = 3500
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
                                optsArray.getInt(0),
                                optsArray.getInt(1),
                                optsArray.getInt(2),
                                optsArray.getInt(3)
                            )

                            list.add(
                                MathQuestion(
                                    category = catEnum,
                                    difficulty = diffEnum,
                                    questionText = obj.getString("questionText"),
                                    visualRepresentation = obj.optString("visualRepresentation", ""),
                                    options = options,
                                    correctAnswer = obj.getInt("correctAnswer"),
                                    explanationTip = obj.optString("explanationTip", "")
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            } catch (_: Exception) {
                // Return null so callers seamlessly fallback to offline local questions
            }
            return@withContext null
        }
    }

    suspend fun fetchReadingQuestions(): List<ReadingQuestion>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("$BASE_URL/api/reading")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 3500
                    readTimeout = 3500
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
                                optsArray.getString(0),
                                optsArray.getString(1),
                                optsArray.getString(2),
                                optsArray.getString(3)
                            )

                            list.add(
                                ReadingQuestion(
                                    categoryTitle = obj.optString("categoryTitle", "Tebak Gambar & Kata"),
                                    questionText = obj.getString("questionText"),
                                    emoji = obj.optString("emoji", ""),
                                    options = options,
                                    correctAnswer = obj.getString("correctAnswer"),
                                    explanationTip = obj.optString("explanationTip", "")
                                )
                            )
                        }
                        return@withContext list
                    }
                }
            } catch (_: Exception) {
                // Return null so callers seamlessly fallback to offline local questions
            }
            return@withContext null
        }
    }
}
