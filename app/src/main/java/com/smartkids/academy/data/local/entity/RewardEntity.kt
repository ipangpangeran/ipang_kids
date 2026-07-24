package com.smartkids.academy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey val id: String, // e.g. "avatar_lion", "theme_space"
    val name: String,
    val cost: Int,
    val isPurchased: Boolean,
    val isEquipped: Boolean,
    val rewardType: String // "AVATAR", "THEME", "STICKER"
)
