package com.smartkids.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String, // e.g. "first_lesson", "math_hero"
    val title: String,
    val description: String,
    val isUnlocked: Boolean,
    val dateUnlockedTimestamp: Long?
)
