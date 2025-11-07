// CrosswordGenerator.kt
package com.example.labs

class CrosswordGenerator {
    fun generateCrossword(difficulty: String): Pair<Array<Array<CrosswordCell>>, List<CrosswordWord>> {
        // Создаем пустую сетку 8x8
        val grid = Array(8) { row ->
            Array(8) { col ->
                CrosswordCell(row, col, isBlack = true)
            }
        }

        val words = mutableListOf<CrosswordWord>()

        // Простой кроссворд с несколькими словами
        when (difficulty) {
            "Средний" -> {
                // Горизонтальные слова
                placeWord(grid, "КОТ", 1, 1, Direction.HORIZONTAL)
                placeWord(grid, "ДОМ", 3, 1, Direction.HORIZONTAL)
                placeWord(grid, "НОС", 5, 1, Direction.HORIZONTAL)

                // Вертикальные слова
                placeWord(grid, "КОД", 1, 1, Direction.VERTICAL)
                placeWord(grid, "ТОМ", 1, 3, Direction.VERTICAL)
                placeWord(grid, "ОСА", 3, 3, Direction.VERTICAL)

                words.addAll(listOf(
                    CrosswordWord("КОТ", "Домашний питомец", 1, 1, Direction.HORIZONTAL, 1),
                    CrosswordWord("ДОМ", "Место жительства", 3, 1, Direction.HORIZONTAL, 3),
                    CrosswordWord("НОС", "Орган обоняния", 5, 1, Direction.HORIZONTAL, 5),
                    CrosswordWord("КОД", "Набор символов", 1, 1, Direction.VERTICAL, 1),
                    CrosswordWord("ТОМ", "Имя или том книги", 1, 3, Direction.VERTICAL, 2),
                    CrosswordWord("ОСА", "Летающее насекомое", 3, 3, Direction.VERTICAL, 4)
                ))
            }
            "Сложный" -> {
                // Более сложный кроссворд
                placeWord(grid, "СЛОН", 1, 1, Direction.HORIZONTAL)
                placeWord(grid, "ЛИСА", 3, 1, Direction.HORIZONTAL)
                placeWord(grid, "ВОЛК", 5, 1, Direction.HORIZONTAL)

                placeWord(grid, "СОВА", 1, 1, Direction.VERTICAL)
                placeWord(grid, "ЛОСЬ", 1, 3, Direction.VERTICAL)
                placeWord(grid, "ИВА", 3, 4, Direction.VERTICAL)

                words.addAll(listOf(
                    CrosswordWord("СЛОН", "Крупное животное", 1, 1, Direction.HORIZONTAL, 1),
                    CrosswordWord("ЛИСА", "Хитрая животное", 3, 1, Direction.HORIZONTAL, 3),
                    CrosswordWord("ВОЛК", "Лесной хищник", 5, 1, Direction.HORIZONTAL, 5),
                    CrosswordWord("СОВА", "Ночная птица", 1, 1, Direction.VERTICAL, 1),
                    CrosswordWord("ЛОСЬ", "Лесной великан", 1, 3, Direction.VERTICAL, 2),
                    CrosswordWord("ИВА", "Плакучее дерево", 3, 4, Direction.VERTICAL, 4)
                ))
            }
            else -> { // Легкий
                // Самый простой кроссворд
                placeWord(grid, "КОТ", 2, 2, Direction.HORIZONTAL)
                placeWord(grid, "ДОМ", 4, 2, Direction.HORIZONTAL)

                placeWord(grid, "КОД", 2, 2, Direction.VERTICAL)
                placeWord(grid, "ТОМ", 2, 4, Direction.VERTICAL)

                words.addAll(listOf(
                    CrosswordWord("КОТ", "Домашний питомец", 2, 2, Direction.HORIZONTAL, 1),
                    CrosswordWord("ДОМ", "Место жительства", 4, 2, Direction.HORIZONTAL, 3),
                    CrosswordWord("КОД", "Набор символов", 2, 2, Direction.VERTICAL, 1),
                    CrosswordWord("ТОМ", "Имя или том книги", 2, 4, Direction.VERTICAL, 2)
                ))
            }
        }

        // Пронумеруем клетки
        numberCells(grid, words)

        return Pair(grid, words)
    }

    private fun placeWord(grid: Array<Array<CrosswordCell>>, word: String, row: Int, col: Int, direction: Direction) {
        var currentRow = row
        var currentCol = col

        for (i in word.indices) {
            if (currentRow < 8 && currentCol < 8) {
                grid[currentRow][currentCol].isBlack = false
                grid[currentRow][currentCol].letter = word[i]

                if (direction == Direction.HORIZONTAL) {
                    currentCol++
                } else {
                    currentRow++
                }
            }
        }
    }

    private fun numberCells(grid: Array<Array<CrosswordCell>>, words: List<CrosswordWord>) {
        var currentNumber = 1

        words.forEach { word ->
            val row = word.row
            val col = word.col

            if (grid[row][col].number == null) {
                grid[row][col].number = currentNumber
                currentNumber++
            }
        }
    }
}