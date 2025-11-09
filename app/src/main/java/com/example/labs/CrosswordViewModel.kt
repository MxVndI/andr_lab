package com.example.labs

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrosswordViewModel(private val context: Context) : ViewModel() {
    private val generator = CrosswordGenerator(context)
    private val authManager = AuthManager(context)
    private val database = CrosswordDatabase.getInstance(context)
    private val repository = UserProgressRepository(database.progressDao(), database.statsDao())

    private val _grid = MutableStateFlow(Array(8) { row ->
        Array(8) { col ->
            CrosswordCell(row = row, col = col, isBlack = true)
        }
    })
    val grid: StateFlow<Array<Array<CrosswordCell>>> = _grid.asStateFlow()

    private val _words = MutableStateFlow(listOf<CrosswordWord>())
    val words: StateFlow<List<CrosswordWord>> = _words.asStateFlow()

    private val _userStats = MutableStateFlow<UserProgress?>(null)
    val userStats: StateFlow<UserProgress?> = _userStats.asStateFlow()

    private val _crosswordStats = MutableStateFlow<List<CrosswordStats>>(emptyList())
    val crosswordStats: StateFlow<List<CrosswordStats>> = _crosswordStats.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUser = authManager.getCurrentUser()
            if (currentUser != null) {
                repository.initializeUserProgress(currentUser, authManager.getUserName())
                loadUserProgress(currentUser)
                loadCrosswordStats(currentUser)
            }
        }
    }

    fun generate(difficulty: String) {
        viewModelScope.launch {
            val result = generator.generateCrossword(difficulty)
            _grid.value = result.first
            _words.value = result.second
        }
    }

    private suspend fun loadUserProgress(userId: String) {
        val progress = repository.getProgress(userId)
        _userStats.value = progress
    }

    private fun loadCrosswordStats(userId: String) {
        viewModelScope.launch {
            repository.getStatsByUser(userId).collect { stats ->
                _crosswordStats.value = stats
            }
        }
    }

    fun recordCrosswordCompletion(difficulty: String, completionTime: Long, score: Int) {
        viewModelScope.launch {
            val currentUser = authManager.getCurrentUser() ?: return@launch
            repository.recordCrosswordCompletion(currentUser, difficulty, completionTime, score)

            // Перезагружаем прогресс
            loadUserProgress(currentUser)
        }
    }

    fun checkCrosswordCompletion(userGrid: Array<Array<Char?>>, originalGrid: Array<Array<CrosswordCell>>): Int {
        var correctCells = 0
        var totalCells = 0

        for (i in userGrid.indices) {
            for (j in userGrid[i].indices) {
                if (!originalGrid[i][j].isBlack) {
                    totalCells++
                    val userLetter = userGrid[i][j]
                    val correctLetter = originalGrid[i][j].letter
                    if (userLetter == correctLetter) {
                        correctCells++
                    }
                }
            }
        }

        return if (totalCells > 0) {
            (correctCells * 100) / totalCells
        } else {
            0
        }
    }

    // Убираем методы из AuthManager, используем только данные из БД
    fun getCurrentUser(): String? {
        return authManager.getCurrentUser()
    }

    fun getUserName(): String {
        return authManager.getUserName()
    }

    // Уровень рассчитываем на основе данных из БД
    fun getUserLevel(): Int {
        val completedPuzzles = _userStats.value?.completedPuzzles ?: 0
        return (completedPuzzles / 3) + 1
    }

    // Количество решенных кроссвордов берем из БД
    fun getCompletedPuzzles(): Int {
        return _userStats.value?.completedPuzzles ?: 0
    }

    fun logout() {
        authManager.logout()
    }
}