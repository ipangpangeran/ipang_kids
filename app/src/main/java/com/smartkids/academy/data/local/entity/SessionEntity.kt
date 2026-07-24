package com.smartkids.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateTimestamp: Long,
    val durationSeconds: Long,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val dailyChallengeCompleted: Boolean
)
