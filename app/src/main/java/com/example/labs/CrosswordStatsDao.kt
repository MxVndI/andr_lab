package com.example.labs

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CrosswordStatsDao {
    @Query("SELECT * FROM crossword_stats WHERE userId = :userId ORDER BY dateCompleted DESC")
    fun getStatsByUser(userId: String): Flow<List<CrosswordStats>>

    @Query("SELECT * FROM crossword_stats WHERE userId = :userId AND difficulty = :difficulty")
    suspend fun getStatsByUserAndDifficulty(userId: String, difficulty: String): List<CrosswordStats>

    @Insert
    suspend fun insertStat(stat: CrosswordStats)

    @Query("DELETE FROM crossword_stats WHERE userId = :userId")
    suspend fun deleteUserStats(userId: String)
}