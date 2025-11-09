package com.example.labs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    suspend fun getProgress(userId: String): UserProgress?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertProgress(progress: UserProgress)

    @Update
    suspend fun updateProgress(progress: UserProgress)

    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun deleteProgress(userId: String)
}