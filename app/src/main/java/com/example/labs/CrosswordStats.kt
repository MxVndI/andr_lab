package com.example.labs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crossword_stats")
data class CrosswordStats(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val difficulty: String,
    val completionTime: Long,
    val dateCompleted: Long,
    val score: Int
)