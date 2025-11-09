package com.example.labs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WordsRepository(private val wordsDao: CrosswordWordsDao) {

    suspend fun initializeWords() {
        val wordCount = wordsDao.getWordCountByDifficulty("Легкий")
        if (wordCount == 0) {
            createDefaultWords()
        }
    }

    private suspend fun createDefaultWords() {
        val defaultWords = listOf(
            CrosswordWordEntity(word = "КОТ", clue = "Домашний питомец", difficulty = "Легкий", length = 3),
            CrosswordWordEntity(word = "ДОМ", clue = "Место жительства", difficulty = "Легкий", length = 3),
            CrosswordWordEntity(word = "КОД", clue = "Набор символов", difficulty = "Легкий", length = 3),
            CrosswordWordEntity(word = "ТОМ", clue = "Имя или том книги", difficulty = "Легкий", length = 3),
            CrosswordWordEntity(word = "РОТ", clue = "Часть лица", difficulty = "Легкий", length = 3),
            CrosswordWordEntity(word = "СОК", clue = "Напиток из фруктов", difficulty = "Легкий", length = 3),
            CrosswordWordEntity(word = "НОС", clue = "Орган обоняния", difficulty = "Средний", length = 3),
            CrosswordWordEntity(word = "ОСА", clue = "Летающее насекомое", difficulty = "Средний", length = 3),
            CrosswordWordEntity(word = "РЕКА", clue = "Водный поток", difficulty = "Средний", length = 4),
            CrosswordWordEntity(word = "ЛЕС", clue = "Много деревьев", difficulty = "Средний", length = 3),
            CrosswordWordEntity(word = "СЛОН", clue = "Крупное животное", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "ЛИСА", clue = "Хитрая животное", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "ВОЛК", clue = "Лесной хищник", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "СОВА", clue = "Ночная птица", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "ЛОСЬ", clue = "Лесной великан", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "ИВА", clue = "Плакучее дерево", difficulty = "Сложный", length = 3),
            CrosswordWordEntity(word = "ТИГР", clue = "Полосатый хищник", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "ЗМЕЯ", clue = "Ползучее пресмыкающееся", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "ОРЁЛ", clue = "Хищная птица", difficulty = "Сложный", length = 4),
            CrosswordWordEntity(word = "МОРЕ", clue = "Большой водоем", difficulty = "Средний", length = 4),
            CrosswordWordEntity(word = "ГОРА", clue = "Высокий холм", difficulty = "Средний", length = 4),
            CrosswordWordEntity(word = "ПОЛЕ", clue = "Открытое пространство", difficulty = "Средний", length = 4),
            CrosswordWordEntity(word = "РЕПА", clue = "Овощ", difficulty = "Легкий", length = 4),
            CrosswordWordEntity(word = "ЛУНА", clue = "Спутник Земли", difficulty = "Средний", length = 4),
            CrosswordWordEntity(word = "ЗВЕЗДА", clue = "Небесное тело", difficulty = "Сложный", length = 6),
            CrosswordWordEntity(word = "СОЛНЦЕ", clue = "Центр системы", difficulty = "Сложный", length = 6)
        )

        wordsDao.insertAll(defaultWords)
    }

    suspend fun getWordsForGrid(difficulty: String): List<CrosswordWordEntity> {
        return withContext(Dispatchers.IO) {
            when (difficulty) {
                "Легкий" -> wordsDao.getRandomWordsByDifficultyAndLength(difficulty, 3, 6)
                "Средний" -> wordsDao.getRandomWordsByDifficultyAndLength(difficulty, 4, 8)
                "Сложный" -> wordsDao.getRandomWordsByDifficultyAndLength(difficulty, 6, 10)
                else -> wordsDao.getRandomWordsByDifficulty("Легкий", 4)
            }
        }
    }
}