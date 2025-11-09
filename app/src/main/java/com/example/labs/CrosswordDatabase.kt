package com.example.labs

import androidx.room.*

@Dao
interface CrosswordWordsDao {
    @Query("SELECT * FROM crossword_words WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWordsByDifficulty(difficulty: String, limit: Int): List<CrosswordWordEntity>

    @Query("SELECT * FROM crossword_words WHERE difficulty = :difficulty AND length <= :maxLength ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomWordsByDifficultyAndLength(difficulty: String, maxLength: Int, limit: Int): List<CrosswordWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: CrosswordWordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<CrosswordWordEntity>)

    @Query("DELETE FROM crossword_words")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM crossword_words WHERE difficulty = :difficulty")
    suspend fun getWordCountByDifficulty(difficulty: String): Int
}

@Database(
    entities = [UserProgress::class, CrosswordStats::class, CrosswordWordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CrosswordDatabase : RoomDatabase() {
    abstract fun progressDao(): UserProgressDao
    abstract fun statsDao(): CrosswordStatsDao
    abstract fun wordsDao(): CrosswordWordsDao

    companion object {
        @Volatile
        private var INSTANCE: CrosswordDatabase? = null

        fun getInstance(context: android.content.Context): CrosswordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CrosswordDatabase::class.java,
                    "crossword_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}