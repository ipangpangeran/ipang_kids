package com.smartkids.academy.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartkids.academy.data.local.dao.ProgressDao
import com.smartkids.academy.data.local.dao.SessionDao
import com.smartkids.academy.data.local.entity.ProgressEntity
import com.smartkids.academy.data.local.entity.RewardEntity
import com.smartkids.academy.data.local.entity.AchievementEntity
import com.smartkids.academy.data.local.entity.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProgressEntity::class,
        SessionEntity::class,
        RewardEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun progressDao(): ProgressDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ip_smart_kids_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.progressDao())
                }
            }
        }

        private suspend fun populateInitialData(progressDao: ProgressDao) {
            // Populate initial subjects progress
            val initialProgress = listOf(
                ProgressEntity("MATHEMATICS", "BEGINNER", 0, 0, 0, 0, 0, System.currentTimeMillis()),
                ProgressEntity("READING", "BEGINNER", 0, 0, 0, 0, 0, System.currentTimeMillis()),
                ProgressEntity("WRITING", "BEGINNER", 0, 0, 0, 0, 0, System.currentTimeMillis()),
                ProgressEntity("BRAIN_GAMES", "BEGINNER", 0, 0, 0, 0, 0, System.currentTimeMillis())
            )
            initialProgress.forEach { progressDao.insertOrUpdateProgress(it) }

            // Populate achievements
            val initialAchievements = listOf(
                AchievementEntity("first_lesson", "First Step 🌱", "Completed your first learning activity!", false, null),
                AchievementEntity("math_hero", "Math Hero 🍎", "Correctly solved 10 math visual equations!", false, null),
                AchievementEntity("reading_explorer", "Reading Explorer 📖", "Successfully spelled 5 words!", false, null),
                AchievementEntity("writing_champion", "Writing Champion ✏️", "Traced 3 letters perfectly!", false, null),
                AchievementEntity("brain_master", "Brain Master 🧠", "Completed memory game level 3!", false, null),
                AchievementEntity("ready_for_sd", "Ready for Elementary School 🌳", "Unlock all badges and level up!", false, null)
            )
            progressDao.insertAchievements(initialAchievements)

            // Populate reward shop items
            val initialRewards = listOf(
                RewardEntity("avatar_lion", "Lion Avatar 🦁", 10, false, false, "AVATAR"),
                RewardEntity("avatar_panda", "Panda Avatar 🐼", 15, false, false, "AVATAR"),
                RewardEntity("avatar_rabbit", "Rabbit Avatar 🐰", 15, false, false, "AVATAR"),
                RewardEntity("theme_space", "Cosmic Space Theme 🚀", 25, false, false, "THEME"),
                RewardEntity("theme_jungle", "Jungle Safari Theme 🌳", 25, false, false, "THEME"),
                RewardEntity("sticker_dino", "Dinosaur Sticker 🦖", 5, false, false, "STICKER"),
                RewardEntity("sticker_star", "Shiny Star Sticker 🌟", 5, false, false, "STICKER")
            )
            progressDao.insertRewards(initialRewards)
        }
    }
}
