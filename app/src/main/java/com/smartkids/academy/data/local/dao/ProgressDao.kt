package com.smartkids.academy.data.local.dao

import androidx.room.*
import com.smartkids.academy.data.local.entity.ProgressEntity
import com.smartkids.academy.data.local.entity.RewardEntity
import com.smartkids.academy.data.local.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Query("SELECT * FROM subject_progress")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM subject_progress WHERE subject = :subject")
    suspend fun getProgressForSubject(subject: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ProgressEntity)

    // Reward operations
    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<RewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<RewardEntity>)

    @Update
    suspend fun updateReward(reward: RewardEntity)

    // Achievement operations
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
}
