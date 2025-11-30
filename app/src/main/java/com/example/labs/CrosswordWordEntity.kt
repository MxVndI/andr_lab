package com.example.labs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crossword_words")
data class CrosswordWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val clue: String,
    val difficulty: String,
    val length: Int,
    val language: String = "ru"
)