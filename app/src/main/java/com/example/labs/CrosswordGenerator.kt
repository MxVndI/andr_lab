// CrosswordGenerator.kt
package com.example.labs

class CrosswordGenerator {
    private var grid = Array(8) { row -> Array(8) { col -> CrosswordCell(row, col, isBlack = true) } }
    private var words = mutableListOf<CrosswordWord>()
    private var currentNumber = 1

    companion object {
        val EASY_WORDS = listOf(
            // Животные
            CrosswordWord("КОТ", "Домашний питомец", 1, 1, Direction.HORIZONTAL),
            CrosswordWord("СОН", "Что делают ночью", 1, 1, Direction.VERTICAL),
            CrosswordWord("ДОМ", "Место жительства", 3, 1, Direction.HORIZONTAL),
            CrosswordWord("РОТ", "Часть лица", 1, 3, Direction.VERTICAL),
            CrosswordWord("НОС", "Орган обоняния", 3, 3, Direction.HORIZONTAL),
            CrosswordWord("РУКА", "Конечность человека", 1, 5, Direction.VERTICAL),
            CrosswordWord("НОГА", "Конечность для ходьбы", 5, 1, Direction.HORIZONTAL),
            CrosswordWord("ГЛАЗ", "Орган зрения", 3, 5, Direction.VERTICAL),
            CrosswordWord("СОБАКА", "Лучший друг человека", 1, 7, Direction.HORIZONTAL),
            CrosswordWord("ПТИЦА", "Летающее животное", 5, 3, Direction.VERTICAL),
            CrosswordWord("РЫБА", "Живет в воде", 7, 1, Direction.HORIZONTAL),
            CrosswordWord("МЫШЬ", "Маленький грызун", 3, 7, Direction.VERTICAL),
            // Еда
            CrosswordWord("ХЛЕБ", "Основной продукт питания", 1, 9, Direction.HORIZONTAL),
            CrosswordWord("МОЛОКО", "Белый напиток", 5, 5, Direction.VERTICAL),
            CrosswordWord("МЯСО", "Белковая пища", 7, 3, Direction.HORIZONTAL),
            CrosswordWord("СЫР", "Молочный продукт", 9, 1, Direction.VERTICAL)
        )

        val MEDIUM_WORDS = listOf(
            // Космос
            CrosswordWord("СОЛНЦЕ", "Источник света и тепла", 1, 1, Direction.HORIZONTAL),
            CrosswordWord("ЛУНА", "Спутник Земли", 1, 2, Direction.VERTICAL),
            CrosswordWord("ЗВЕЗДА", "Небесное тело", 3, 1, Direction.VERTICAL),
            CrosswordWord("ПЛАНЕТА", "Небесное тело вокруг звезды", 5, 1, Direction.HORIZONTAL),
            CrosswordWord("КОСМОС", "Вселенная", 1, 4, Direction.VERTICAL),
            CrosswordWord("ГАЛАКТИКА", "Скопление звезд", 7, 1, Direction.HORIZONTAL),
            CrosswordWord("КОМЕТА", "Небесное тело с хвостом", 3, 4, Direction.VERTICAL),
            CrosswordWord("АСТЕРОИД", "Малый небесный объект", 5, 4, Direction.HORIZONTAL),
            // Природа
            CrosswordWord("ЛЕС", "Много деревьев", 1, 6, Direction.VERTICAL),
            CrosswordWord("РЕКА", "Водный поток", 3, 6, Direction.HORIZONTAL),
            CrosswordWord("ГОРА", "Высокая возвышенность", 5, 6, Direction.VERTICAL),
            CrosswordWord("ОЗЕРО", "Водоем", 7, 3, Direction.HORIZONTAL),
            CrosswordWord("МОРЕ", "Большой водоем", 9, 1, Direction.VERTICAL),
            // Техника
            CrosswordWord("ТЕЛЕФОН", "Средство связи", 1, 8, Direction.HORIZONTAL),
            CrosswordWord("КОМПЬЮТЕР", "Электронная машина", 3, 8, Direction.VERTICAL),
            CrosswordWord("АВТОМОБИЛЬ", "Транспортное средство", 5, 8, Direction.HORIZONTAL)
        )

        val HARD_WORDS = listOf(
            // Программирование
            CrosswordWord("ПРОГРАММА", "Набор инструкций для компьютера", 1, 0, Direction.HORIZONTAL),
            CrosswordWord("АЛГОРИТМ", "Последовательность действий", 1, 2, Direction.VERTICAL),
            CrosswordWord("ФУНКЦИЯ", "Блок кода с определенной задачей", 3, 1, Direction.HORIZONTAL),
            CrosswordWord("ПЕРЕМЕННАЯ", "Хранилище данных", 1, 4, Direction.VERTICAL),
            CrosswordWord("СТРУКТУРА", "Организация данных", 5, 1, Direction.HORIZONTAL),
            CrosswordWord("ИНТЕРФЕЙС", "Способ взаимодействия", 1, 6, Direction.VERTICAL),
            CrosswordWord("БАЗАДАННЫХ", "Хранилище информации", 7, 1, Direction.HORIZONTAL),
            CrosswordWord("СЕРВЕР", "Компьютер для обработки запросов", 3, 6, Direction.VERTICAL),
            // Наука
            CrosswordWord("МАТЕМАТИКА", "Наука о числах", 1, 8, Direction.HORIZONTAL),
            CrosswordWord("ФИЗИКА", "Наука о природе", 3, 8, Direction.VERTICAL),
            CrosswordWord("ХИМИЯ", "Наука о веществах", 5, 8, Direction.HORIZONTAL),
            CrosswordWord("БИОЛОГИЯ", "Наука о живом", 7, 3, Direction.VERTICAL),
            CrosswordWord("ГЕОГРАФИЯ", "Наука о Земле", 9, 1, Direction.HORIZONTAL),
            // Искусство
            CrosswordWord("МУЗЫКА", "Искусство звуков", 1, 10, Direction.VERTICAL),
            CrosswordWord("ЖИВОПИСЬ", "Искусство рисования", 3, 10, Direction.HORIZONTAL),
            CrosswordWord("ЛИТЕРАТУРА", "Искусство слова", 5, 10, Direction.VERTICAL),
            CrosswordWord("ТЕАТР", "Искусство представления", 7, 5, Direction.HORIZONTAL),
            CrosswordWord("КИНО", "Искусство движущихся картинок", 9, 3, Direction.VERTICAL)
        )
    }

    fun generateCrossword(difficulty: String): Pair<Array<Array<CrosswordCell>>, List<CrosswordWord>> {
        initializeGrid()
        words.clear()
        currentNumber = 1

        val wordList = when (difficulty) {
            "Средний" -> MEDIUM_WORDS.take(6) // Ограничиваем количество слов
            "Сложный" -> HARD_WORDS.take(6)
            else -> EASY_WORDS.take(6)
        }

        // Простое размещение слов
        placeWordsSimple(wordList)
        numberCells()

        return Pair(grid, words)
    }

    private fun initializeGrid() {
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                grid[i][j] = CrosswordCell(i, j, isBlack = true)
            }
        }
    }

    private fun canPlaceWord(word: CrosswordWord): Boolean {
        val wordText = word.word
        var row = word.row
        var col = word.col

        for (i in wordText.indices) {
            if (row >= 8 || col >= 8) return false

            val currentCell = grid[row][col]
            if (!currentCell.isBlack && currentCell.letter != wordText[i]) {
                return false
            }

            if (word.direction == Direction.HORIZONTAL) col++ else row++
        }
        return true
    }

    private fun placeWord(word: CrosswordWord) {
        val wordText = word.word
        var row = word.row
        var col = word.col

        for (i in wordText.indices) {
            grid[row][col].isBlack = false
            grid[row][col].letter = wordText[i]

            if (word.direction == Direction.HORIZONTAL) col++ else row++
        }
    }

    private fun placeWordsSimple(wordList: List<CrosswordWord>) {
        // Размещаем слова в фиксированных позициях для стабильности
        wordList.forEachIndexed { index, word ->
            val positionedWord = when (index) {
                0 -> word.copy(row = 1, col = 1, direction = Direction.HORIZONTAL)
                1 -> word.copy(row = 1, col = 1, direction = Direction.VERTICAL)
                2 -> word.copy(row = 3, col = 1, direction = Direction.HORIZONTAL)
                3 -> word.copy(row = 1, col = 3, direction = Direction.VERTICAL)
                4 -> word.copy(row = 5, col = 1, direction = Direction.HORIZONTAL)
                5 -> word.copy(row = 1, col = 5, direction = Direction.VERTICAL)
                else -> word
            }
            
            if (canPlaceWord(positionedWord)) {
                placeWord(positionedWord)
                words.add(positionedWord)
            }
        }
    }

    private fun placeWordsSmart(wordList: List<CrosswordWord>) {
        // Сначала размещаем первое слово в центре
        if (wordList.isNotEmpty()) {
            val firstWord = wordList[0].copy(row = 3, col = 1)
            if (canPlaceWord(firstWord)) {
                placeWord(firstWord)
                words.add(firstWord)
            }
        }

        // Затем пытаемся разместить остальные слова, пересекаясь с уже размещенными
        wordList.drop(1).forEach { word ->
            val bestPosition = findBestPosition(word)
            if (bestPosition != null) {
                val positionedWord = word.copy(row = bestPosition.first, col = bestPosition.second)
                if (canPlaceWord(positionedWord)) {
                    placeWord(positionedWord)
                    words.add(positionedWord)
                }
            }
        }
    }

    private fun findBestPosition(word: CrosswordWord): Pair<Int, Int>? {
        val wordText = word.word
        val maxAttempts = 50
        var attempts = 0

        while (attempts < maxAttempts) {
            val row = (0 until 8).random()
            val col = (0 until 8).random()
            
            val testWord = word.copy(row = row, col = col)
            if (canPlaceWord(testWord)) {
                return Pair(row, col)
            }
            attempts++
        }
        return null
    }

    private fun placeWords(wordList: List<CrosswordWord>) {
        wordList.forEach { word ->
            if (canPlaceWord(word)) {
                placeWord(word)
                words.add(word)
            }
        }
    }

    private fun numberCells() {
        currentNumber = 1
        words.forEach { word ->
            val row = word.row
            val col = word.col

            if (grid[row][col].number == null) {
                grid[row][col].number = currentNumber
                word.number = currentNumber
                currentNumber++
            } else {
                word.number = grid[row][col].number
            }
        }
    }

    fun checkSolution(userGrid: Array<Array<Char?>>): Boolean {
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                if (!grid[i][j].isBlack) {
                    if (userGrid[i][j] != grid[i][j].letter) {
                        return false
                    }
                }
            }
        }
        return true
    }
}