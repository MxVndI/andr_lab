package com.example.labs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WordsRepository(private val wordsDao: CrosswordWordsDao) {

    suspend fun initializeWords() {
        val wordCountRu = wordsDao.getWordCountByDifficulty("Легкий", "ru")
        val wordCountEn = wordsDao.getWordCountByDifficulty("Easy", "en")
        if (wordCountRu == 0) {
            createDefaultWordsRu()
        }
        if (wordCountEn == 0) {
            createDefaultWordsEn()
        }
    }

    private suspend fun createDefaultWordsRu() {
        val defaultWords = CrosswordWordsData.russianWords.map { wordData ->
            CrosswordWordEntity(
                word = wordData.word,
                clue = wordData.clue,
                difficulty = wordData.difficulty,
                length = wordData.length,
                language = "ru"
            )
        }
        wordsDao.insertAll(defaultWords)
    }

    private suspend fun createDefaultWordsEn() {
        val defaultWords = CrosswordWordsData.englishWords.map { wordData ->
            CrosswordWordEntity(
                word = wordData.word,
                clue = wordData.clue,
                difficulty = wordData.difficulty,
                length = wordData.length,
                language = "en"
            )
        }
        wordsDao.insertAll(defaultWords)
    }

    suspend fun getWordsForGrid(difficulty: String, language: String): List<CrosswordWordEntity> {
        return withContext(Dispatchers.IO) {
            // Маппинг сложности для разных языков
            val mappedDifficulty = when {
                difficulty == "Easy" || difficulty == "Легкий" || difficulty.contains("Easy", ignoreCase = true) || difficulty.contains("Легкий", ignoreCase = true) -> 
                    if (language == "en") "Easy" else "Легкий"
                difficulty == "Medium" || difficulty == "Средний" || difficulty.contains("Medium", ignoreCase = true) || difficulty.contains("Средний", ignoreCase = true) -> 
                    if (language == "en") "Medium" else "Средний"
                difficulty == "Hard" || difficulty == "Сложный" || difficulty.contains("Hard", ignoreCase = true) || difficulty.contains("Сложный", ignoreCase = true) -> 
                    if (language == "en") "Hard" else "Сложный"
                else -> if (language == "en") "Easy" else "Легкий"
            }
            
            when (mappedDifficulty) {
                "Легкий", "Easy" -> wordsDao.getRandomWordsByDifficultyAndLength(mappedDifficulty, 3, 6, language)
                "Средний", "Medium" -> wordsDao.getRandomWordsByDifficultyAndLength(mappedDifficulty, 4, 8, language)
                "Сложный", "Hard" -> wordsDao.getRandomWordsByDifficultyAndLength(mappedDifficulty, 6, 10, language)
                else -> wordsDao.getRandomWordsByDifficultyAndLength(if (language == "en") "Easy" else "Легкий", 3, 4, language)
            }
        }
    }
}