package com.smartkids.academy.domain.usecase

import com.smartkids.academy.data.local.entity.ProgressEntity

object AdaptiveLearningEngine {

    enum class Level(val title: String) {
        BEGINNER("🌱 Beginner"),
        EXPLORER("🌿 Explorer"),
        LEARNER("🌳 Learner"),
        READY_FOR_SCHOOL("🚀 Ready for School")
    }

    /**
     * Determines the next difficulty level or updates progress state based on quiz performance.
     * If accuracy is high (> 80%), level up.
     * If accuracy is low (< 50%), increase visual support/remedial repetition.
     */
    fun evaluateProgress(
        currentProgress: ProgressEntity,
        newCorrectCount: Int,
        newTotalQuestions: Int
    ): ProgressEntity {
        if (newTotalQuestions <= 0) return currentProgress

        val updatedAnswered = currentProgress.questionsAnswered + newTotalQuestions
        val updatedCorrect = currentProgress.correctAnswers + newCorrectCount
        val accuracy = (newCorrectCount.toFloat() / newTotalQuestions.toFloat()) * 100

        // Calculate new streak
        val isPerfect = newCorrectCount == newTotalQuestions
        val updatedStreak = if (isPerfect) currentProgress.currentStreak + 1 else 0

        // Adaptive level calculation
        val currentLevelEnum = when (currentProgress.level) {
            "BEGINNER" -> Level.BEGINNER
            "EXPLORER" -> Level.EXPLORER
            "LEARNER" -> Level.LEARNER
            "READY_FOR_SCHOOL" -> Level.READY_FOR_SCHOOL
            else -> Level.BEGINNER
        }

        var nextLevel = currentLevelEnum
        if (accuracy >= 80f && updatedAnswered >= 10) {
            nextLevel = when (currentLevelEnum) {
                Level.BEGINNER -> Level.EXPLORER
                Level.EXPLORER -> Level.LEARNER
                Level.LEARNER -> Level.READY_FOR_SCHOOL
                Level.READY_FOR_SCHOOL -> Level.READY_FOR_SCHOOL
            }
        }

        // Earn rewards based on performance: 1 star per correct answer, bonus for streak
        val starsEarned = newCorrectCount + (if (updatedStreak >= 3) 5 else 0)
        val coinsEarned = (newCorrectCount / 2) + (if (isPerfect) 2 else 0)

        return currentProgress.copy(
            level = nextLevel.name,
            stars = currentProgress.stars + starsEarned,
            coins = currentProgress.coins + coinsEarned,
            questionsAnswered = updatedAnswered,
            correctAnswers = updatedCorrect,
            currentStreak = updatedStreak,
            lastActiveTimestamp = System.currentTimeMillis()
        )
    }
}
