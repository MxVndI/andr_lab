package com.example.labs

data class CrosswordCell(
    val row: Int,
    val col: Int,
    var letter: Char? = null,
    var number: Int? = null,
    var isBlack: Boolean = false
)

data class CrosswordWord(
    val word: String,
    val clue: String,
    val row: Int,
    val col: Int,
    val direction: Direction,
    var number: Int? = null
)

enum class Direction {
    HORIZONTAL, VERTICAL
}