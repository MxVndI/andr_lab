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

    fun generateCrossword(difficulty: String, language: String = "ru"): Pair<Array<Array<CrosswordCell>>, List<CrosswordWord>> {
        // Создаем сетку с черными клетками
        val grid = Array(8) { row -> Array(8) { col -> CrosswordCell(row, col, isBlack = true) } }
        val words = mutableListOf<CrosswordWord>()

        val dbWords = runBlocking {
            wordsRepository.getWordsForGrid(difficulty, language)
        }

        if (dbWords.isEmpty()) {
            return Pair(grid, words)
        }

        // Размещаем первое слово горизонтально в центре
        val firstWord = dbWords[0]
        val firstRow = 3
        val firstCol = (8 - firstWord.word.length) / 2
        val firstCrosswordWord = CrosswordWord(
            word = firstWord.word,
            clue = firstWord.clue,
            row = firstRow,
            col = firstCol,
            direction = Direction.HORIZONTAL,
            number = 1
        )
        placeWord(grid, firstCrosswordWord)
        words.add(firstCrosswordWord)

        var wordNumber = 2

        // Размещаем остальные слова, пересекаясь с уже размещенными
        for (i in 1 until dbWords.size) {
            val wordEntity = dbWords[i]
            var placed = false

            // Сортируем существующие слова по приоритету (сначала те, что в центре)
            val sortedWords = words.sortedBy {
                val centerR = it.row + (if (it.direction == Direction.VERTICAL) it.word.length / 2 else 0)
                val centerC = it.col + (if (it.direction == Direction.HORIZONTAL) it.word.length / 2 else 0)
                kotlin.math.abs(centerR - 4) + kotlin.math.abs(centerC - 4)
            }

            // Пробуем разместить слово, пересекаясь с уже размещенными
            for (existingWord in sortedWords) {
                // Ищем все возможные пересечения
                val intersections = mutableListOf<Pair<Int, Int>>()

                for (existingCharIndex in existingWord.word.indices) {
                    val existingChar = existingWord.word[existingCharIndex].uppercaseChar()
                    // Ищем все вхождения этой буквы в новом слове
                    for (newCharIndex in wordEntity.word.indices) {
                        if (wordEntity.word[newCharIndex].uppercaseChar() == existingChar) {
                            intersections.add(Pair(existingCharIndex, newCharIndex))
                        }
                    }
                }

                // Пробуем каждое возможное пересечение
                for ((existingCharIndex, newCharIndex) in intersections) {
                    // Вычисляем позицию пересечения
                    val intersectionRow = if (existingWord.direction == Direction.HORIZONTAL) {
                        existingWord.row
                    } else {
                        existingWord.row + existingCharIndex
                    }
                    val intersectionCol = if (existingWord.direction == Direction.HORIZONTAL) {
                        existingWord.col + existingCharIndex
                    } else {
                        existingWord.col
                    }

                    // Пробуем разместить новое слово перпендикулярно
                    val newDirection = if (existingWord.direction == Direction.HORIZONTAL) {
                        Direction.VERTICAL
                    } else {
                        Direction.HORIZONTAL
                    }

                    // Вычисляем начальную позицию нового слова
                    val newRow = if (newDirection == Direction.HORIZONTAL) {
                        intersectionRow
                    } else {
                        intersectionRow - newCharIndex
                    }
                    val newCol = if (newDirection == Direction.HORIZONTAL) {
                        intersectionCol - newCharIndex
                    } else {
                        intersectionCol
                    }

                    // Проверяем, что слово помещается в сетку
                    if (newRow >= 0 && newCol >= 0) {
                        val endRow = if (newDirection == Direction.VERTICAL) newRow + wordEntity.word.length - 1 else newRow
                        val endCol = if (newDirection == Direction.HORIZONTAL) newCol + wordEntity.word.length - 1 else newCol

                        if (endRow < 8 && endCol < 8) {
                            val crosswordWord = CrosswordWord(
                                word = wordEntity.word,
                                clue = wordEntity.clue,
                                row = newRow,
                                col = newCol,
                                direction = newDirection,
                                number = wordNumber
                            )

                            if (canPlaceWord(grid, crosswordWord)) {
                                placeWord(grid, crosswordWord)
                                words.add(crosswordWord)
                                wordNumber++
                                placed = true
                                break
                            }
                        }
                    }
                }
                if (placed) break
            }

            // Если не удалось разместить через пересечение, пробуем разместить рядом (fallback)
            if (!placed) {
                for (existingWord in words) {
                    val newDirection = if (existingWord.direction == Direction.HORIZONTAL) {
                        Direction.VERTICAL
                    } else {
                        Direction.HORIZONTAL
                    }

                    // Пробуем несколько позиций рядом с существующим словом
                    val positions = if (newDirection == Direction.HORIZONTAL) {
                        listOf(
                            Pair(existingWord.row + 1, existingWord.col),
                            Pair(existingWord.row - 1, existingWord.col),
                            Pair(existingWord.row, existingWord.col + existingWord.word.length),
                            Pair(existingWord.row, existingWord.col - wordEntity.word.length)
                        )
                    } else {
                        listOf(
                            Pair(existingWord.row, existingWord.col + 1),
                            Pair(existingWord.row, existingWord.col - 1),
                            Pair(existingWord.row + existingWord.word.length, existingWord.col),
                            Pair(existingWord.row - wordEntity.word.length, existingWord.col)
                        )
                    }

                    for ((newRow, newCol) in positions) {
                        if (newRow >= 0 && newCol >= 0) {
                            val endRow = if (newDirection == Direction.VERTICAL) newRow + wordEntity.word.length - 1 else newRow
                            val endCol = if (newDirection == Direction.HORIZONTAL) newCol + wordEntity.word.length - 1 else newCol

                            if (endRow < 8 && endCol < 8) {
                                val crosswordWord = CrosswordWord(
                                    word = wordEntity.word,
                                    clue = wordEntity.clue,
                                    row = newRow,
                                    col = newCol,
                                    direction = newDirection,
                                    number = wordNumber
                                )

                                if (canPlaceWord(grid, crosswordWord)) {
                                    placeWord(grid, crosswordWord)
                                    words.add(crosswordWord)
                                    wordNumber++
                                    placed = true
                                    break
                                }
                            }
                        }
                        if (placed) break
                    }
                    if (placed) break
                }
            }
        }

        numberCells(grid, words)
        return Pair(grid, words)
    }

    private fun canPlaceWord(grid: Array<Array<CrosswordCell>>, word: CrosswordWord): Boolean {
        val r = word.row
        val c = word.col

        // Проверяем границы
        if (r < 0 || c < 0) return false
        if (word.direction == Direction.HORIZONTAL) {
            if (c + word.word.length > 8) return false
        } else {
            if (r + word.word.length > 8) return false
        }

        // Проверяем каждую букву
        for ((index, ch) in word.word.withIndex()) {
            val currentR = if (word.direction == Direction.HORIZONTAL) r else r + index
            val currentC = if (word.direction == Direction.HORIZONTAL) c + index else c

            if (currentR >= 8 || currentC >= 8) return false

            val cell = grid[currentR][currentC]

            // Если клетка уже содержит букву, она должна совпадать
            if (!cell.isBlack) {
                if (cell.letter != ch.uppercaseChar()) return false
            }
        }

        // Проверяем клетки непосредственно перед и после слова
        if (word.direction == Direction.HORIZONTAL) {
            // Перед словом
            if (c > 0 && !grid[r][c - 1].isBlack) return false
            // После слова
            if (c + word.word.length < 8 && !grid[r][c + word.word.length].isBlack) return false

            // Проверяем соседние клетки по вертикали для каждой буквы
            for (i in word.word.indices) {
                val currentC = c + i
                // Сверху
                if (r > 0) {
                    val topCell = grid[r - 1][currentC]
                    if (!topCell.isBlack) {
                        // Если сверху есть буква, проверяем что она не создает нежелательное пересечение
                        // кроме случаев когда это пересечение с другим словом
                        val hasVerticalWordAbove = r > 1 && !grid[r - 2][currentC].isBlack
                        if (!hasVerticalWordAbove && topCell.letter != word.word[i].uppercaseChar()) {
                            return false
                        }
                    }
                }
                // Снизу
                if (r < 7) {
                    val bottomCell = grid[r + 1][currentC]
                    if (!bottomCell.isBlack) {
                        val hasVerticalWordBelow = r < 6 && !grid[r + 2][currentC].isBlack
                        if (!hasVerticalWordBelow && bottomCell.letter != word.word[i].uppercaseChar()) {
                            return false
                        }
                    }
                }
            }
        } else {
            // Перед словом
            if (r > 0 && !grid[r - 1][c].isBlack) return false
            // После слова
            if (r + word.word.length < 8 && !grid[r + word.word.length][c].isBlack) return false

            // Проверяем соседние клетки по горизонтали для каждой буквы
            for (i in word.word.indices) {
                val currentR = r + i
                // Слева
                if (c > 0) {
                    val leftCell = grid[currentR][c - 1]
                    if (!leftCell.isBlack) {
                        val hasHorizontalWordLeft = c > 1 && !grid[currentR][c - 2].isBlack
                        if (!hasHorizontalWordLeft && leftCell.letter != word.word[i].uppercaseChar()) {
                            return false
                        }
                    }
                }
                // Справа
                if (c < 7) {
                    val rightCell = grid[currentR][c + 1]
                    if (!rightCell.isBlack) {
                        val hasHorizontalWordRight = c < 6 && !grid[currentR][c + 2].isBlack
                        if (!hasHorizontalWordRight && rightCell.letter != word.word[i].uppercaseChar()) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    private fun placeWord(grid: Array<Array<CrosswordCell>>, word: CrosswordWord) {
        var r = word.row
        var c = word.col
        for (ch in word.word) {
            if (r < 8 && c < 8) {
                grid[r][c].isBlack = false
                grid[r][c].letter = ch.uppercaseChar()
                if (word.direction == Direction.HORIZONTAL) c++ else r++
            }
        }
    }

    private fun numberCells(grid: Array<Array<CrosswordCell>>, words: List<CrosswordWord>) {
        // Сначала сбрасываем все номера
        for (row in grid) {
            for (cell in row) {
                cell.number = null
            }
        }

        var number = 1
        for (word in words) {
            val r = word.row
            val c = word.col
            if (grid[r][c].number == null) {
                grid[r][c].number = number
                number++
            }
        }
    }
}