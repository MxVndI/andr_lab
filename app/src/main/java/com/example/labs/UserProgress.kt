package com.example.labs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val userId: String,
    val completedPuzzles: Int,
    val averageTime: Long,
    val favoriteDifficulty: String,
    val totalScore: Int,
    val createdAt: Long
)