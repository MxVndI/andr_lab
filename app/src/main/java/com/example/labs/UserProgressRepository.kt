package com.example.labs

import kotlinx.coroutines.flow.Flow

class UserProgressRepository(
    private val progressDao: UserProgressDao,
    private val statsDao: CrosswordStatsDao
) {

    suspend fun getProgress(userId: String): UserProgress? {
        return progressDao.getProgress(userId)
    }

    suspend fun saveProgress(progress: UserProgress) {
        progressDao.insertProgress(progress)
    }

    suspend fun updateProgress(progress: UserProgress) {
        progressDao.updateProgress(progress)
    }

    suspend fun deleteProgress(userId: String) {
        progressDao.deleteProgress(userId)
    }

    fun getStatsByUser(userId: String): Flow<List<CrosswordStats>> {
        return statsDao.getStatsByUser(userId)
    }

    suspend fun getStatsByUserAndDifficulty(userId: String, difficulty: String): List<CrosswordStats> {
        return statsDao.getStatsByUserAndDifficulty(userId, difficulty)
    }

    suspend fun saveStat(stat: CrosswordStats) {
        statsDao.insertStat(stat)
    }

    suspend fun deleteUserStats(userId: String) {
        statsDao.deleteUserStats(userId)
    }

    suspend fun initializeUserProgress(userId: String, userName: String) {
        val existingProgress = getProgress(userId)
        if (existingProgress == null) {
            val newProgress = UserProgress(
                userId = userId,
                completedPuzzles = 0,
                averageTime = 0L,
                favoriteDifficulty = "Легкий",
                totalScore = 0,
                createdAt = System.currentTimeMillis()
            )
            saveProgress(newProgress)
        }
    }

    suspend fun recordCrosswordCompletion(
        userId: String,
        difficulty: String,
        completionTime: Long,
        score: Int
    ) {
        // Сохраняем статистику попытки
        val stat = CrosswordStats(
            userId = userId,
            difficulty = difficulty,
            completionTime = completionTime,
            dateCompleted = System.currentTimeMillis(),
            score = score
        )
        saveStat(stat)

        // Обновляем общий прогресс пользователя
        val currentProgress = getProgress(userId) ?: return
        val newCompletedPuzzles = currentProgress.completedPuzzles + 1
        val newTotalScore = currentProgress.totalScore + score

        // Обновляем среднее время
        val totalTime = currentProgress.averageTime * currentProgress.completedPuzzles + completionTime
        val newAverageTime = if (newCompletedPuzzles > 0) totalTime / newCompletedPuzzles else 0L

        val updatedProgress = currentProgress.copy(
            completedPuzzles = newCompletedPuzzles,
            averageTime = newAverageTime,
            totalScore = newTotalScore,
            favoriteDifficulty = difficulty
        )

        updateProgress(updatedProgress)
    }
}