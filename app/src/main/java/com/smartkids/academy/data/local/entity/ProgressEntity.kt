package com.smartkids.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_progress")
data class ProgressEntity(
    @PrimaryKey val subject: String, // e.g. "MATHEMATICS", "READING", "WRITING", "BRAIN_GAMES"
    val level: String, // "BEGINNER", "EXPLORER", "LEARNER", "READY_FOR_SCHOOL"
    val stars: Int,
    val coins: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val currentStreak: Int, // Daily streak
    val lastActiveTimestamp: Long
)
