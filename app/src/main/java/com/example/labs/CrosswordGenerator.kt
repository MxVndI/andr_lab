package com.example.labs

import android.content.Context
import kotlinx.coroutines.runBlocking

class CrosswordGenerator(private val context: Context) {

    private val database = CrosswordDatabase.getInstance(context)
    private val wordsRepository = WordsRepository(database.wordsDao())

    init {
        runBlocking {
            wordsRepository.initializeWords()
        }
    }

    fun generateCrossword(difficulty: String): Pair<Array<Array<CrosswordCell>>, List<CrosswordWord>> {
        val grid = Array(8) { row -> Array(8) { col -> CrosswordCell(row, col, isBlack = true) } }
        val words = mutableListOf<CrosswordWord>()

        when (difficulty) {
            "Easy" -> {
                val easyWords = listOf(
                    CrosswordWord("КОТ", "Домашний питомец", 2, 2, Direction.HORIZONTAL, 1),
                    CrosswordWord("ДОМ", "Место жительства", 4, 2, Direction.HORIZONTAL, 2),
                    CrosswordWord("КОД", "Набор символов", 2, 2, Direction.VERTICAL, 3),
                    CrosswordWord("ТОМ", "Имя или том книги", 2, 4, Direction.VERTICAL, 4)
                )
                easyWords.forEach { placeWord(grid, it) }
                words.addAll(easyWords)
            }
            "Легкий" -> {
                val easyWords = listOf(
                    CrosswordWord("КОТ", "Домашний питомец", 2, 2, Direction.HORIZONTAL, 1),
                    CrosswordWord("ДОМ", "Место жительства", 4, 2, Direction.HORIZONTAL, 2),
                    CrosswordWord("КОД", "Набор символов", 2, 2, Direction.VERTICAL, 3),
                    CrosswordWord("ТОМ", "Имя или том книги", 2, 4, Direction.VERTICAL, 4)
                )
                easyWords.forEach { placeWord(grid, it) }
                words.addAll(easyWords)
            }
            "Medium" -> {
                val mediumWords = listOf(
                    CrosswordWord("НОС", "Орган обоняния", 1, 1, Direction.HORIZONTAL, 1),
                    CrosswordWord("ОСА", "Летающее насекомое", 3, 1, Direction.HORIZONTAL, 2),
                    CrosswordWord("РЕКА", "Водный поток", 5, 1, Direction.HORIZONTAL, 3),
                    CrosswordWord("ЛЕС", "Много деревьев", 1, 1, Direction.VERTICAL, 4),
                    CrosswordWord("МОРЕ", "Большой водоем", 1, 3, Direction.VERTICAL, 5),
                    CrosswordWord("ГОРА", "Высокий холм", 3, 3, Direction.VERTICAL, 6)
                )
                mediumWords.forEach { placeWord(grid, it) }
                words.addAll(mediumWords)
            }
            "Средний" -> {
                val mediumWords = listOf(
                    CrosswordWord("НОС", "Орган обоняния", 1, 1, Direction.HORIZONTAL, 1),
                    CrosswordWord("ОСА", "Летающее насекомое", 3, 1, Direction.HORIZONTAL, 2),
                    CrosswordWord("РЕКА", "Водный поток", 5, 1, Direction.HORIZONTAL, 3),
                    CrosswordWord("ЛЕС", "Много деревьев", 1, 1, Direction.VERTICAL, 4),
                    CrosswordWord("МОРЕ", "Большой водоем", 1, 3, Direction.VERTICAL, 5),
                    CrosswordWord("ГОРА", "Высокий холм", 3, 3, Direction.VERTICAL, 6)
                )
                mediumWords.forEach { placeWord(grid, it) }
                words.addAll(mediumWords)
            }
            "Hard" -> {
                val hardWords = listOf(
                    CrosswordWord("СЛОН", "Крупное животное", 1, 1, Direction.HORIZONTAL, 1),
                    CrosswordWord("ЛИСА", "Хитрое животное", 3, 1, Direction.HORIZONTAL, 2),
                    CrosswordWord("ВОЛК", "Лесной хищник", 5, 1, Direction.HORIZONTAL, 3),
                    CrosswordWord("СОВА", "Ночная птица", 1, 1, Direction.VERTICAL, 4),
                    CrosswordWord("ЛОСЬ", "Лесной великан", 1, 3, Direction.VERTICAL, 5),
                    CrosswordWord("ТИГР", "Полосатый хищник", 3, 4, Direction.VERTICAL, 6)
                )
                hardWords.forEach { placeWord(grid, it) }
                words.addAll(hardWords)
            }
            "Сложный" -> {
                val hardWords = listOf(
                    CrosswordWord("СЛОН", "Крупное животное", 1, 1, Direction.HORIZONTAL, 1),
                    CrosswordWord("ЛИСА", "Хитрое животное", 3, 1, Direction.HORIZONTAL, 2),
                    CrosswordWord("ВОЛК", "Лесной хищник", 5, 1, Direction.HORIZONTAL, 3),
                    CrosswordWord("СОВА", "Ночная птица", 1, 1, Direction.VERTICAL, 4),
                    CrosswordWord("ЛОСЬ", "Лесной великан", 1, 3, Direction.VERTICAL, 5),
                    CrosswordWord("ТИГР", "Полосатый хищник", 3, 4, Direction.VERTICAL, 6)
                )
                hardWords.forEach { placeWord(grid, it) }
                words.addAll(hardWords)
            }
        }

        numberCells(grid, words)
        return Pair(grid, words)
    }

    private fun placeWord(grid: Array<Array<CrosswordCell>>, word: CrosswordWord) {
        var r = word.row
        var c = word.col
        for (ch in word.word) {
            if (r < 8 && c < 8) {
                grid[r][c].isBlack = false
                grid[r][c].letter = ch
                if (word.direction == Direction.HORIZONTAL) c++ else r++
            }
        }
    }

    private fun numberCells(grid: Array<Array<CrosswordCell>>, words: List<CrosswordWord>) {
        var number = 1
        words.forEach { w ->
            val r = w.row
            val c = w.col
            if (grid[r][c].number == null) {
                grid[r][c].number = number
                number++
            }
        }
    }
}
