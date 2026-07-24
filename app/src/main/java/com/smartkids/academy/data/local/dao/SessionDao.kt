package com.smartkids.academy.data.local.dao

import androidx.room.*
import com.smartkids.academy.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM learning_sessions ORDER BY dateTimestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM learning_sessions WHERE subject = :subject ORDER BY dateTimestamp DESC")
    fun getSessionsForSubject(subject: String): Flow<List<SessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("SELECT SUM(durationSeconds) FROM learning_sessions")
    fun getTotalDuration(): Flow<Long?>

    @Query("SELECT COUNT(*) FROM learning_sessions WHERE dailyChallengeCompleted = 1")
    fun getCompletedDailyChallengesCount(): Flow<Int>
}
