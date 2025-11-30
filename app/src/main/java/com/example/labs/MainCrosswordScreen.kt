package com.example.labs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun MainCrosswordScreen(
    onLogout: () -> Unit,
    onShowStats: (String, Int) -> Unit,
    themeManager: ThemeManager
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val viewModel = remember { CrosswordViewModel(context) }
    val generator = remember { CrosswordGenerator(context) }
    val contextForStrings = LocalContext.current
    var difficulty by remember { mutableStateOf(contextForStrings.getString(R.string.easy_difficulty)) }
    
    // Обновляем difficulty при смене языка
    LaunchedEffect(themeManager.currentLanguage) {
        difficulty = when (difficulty) {
            contextForStrings.getString(R.string.easy_difficulty), "Легкий", "Easy" -> contextForStrings.getString(R.string.easy_difficulty)
            contextForStrings.getString(R.string.medium_difficulty), "Средний", "Medium" -> contextForStrings.getString(R.string.medium_difficulty)
            contextForStrings.getString(R.string.hard_difficulty), "Сложный", "Hard" -> contextForStrings.getString(R.string.hard_difficulty)
            else -> contextForStrings.getString(R.string.easy_difficulty)
        }
    }

    var grid by remember { mutableStateOf(Array(8) { Array(8) { CrosswordCell(0, 0, isBlack = true) } }) }
    var words by remember { mutableStateOf(listOf<CrosswordWord>()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var userGrid by remember { mutableStateOf(Array(8) { Array(8) { null as Char? } }) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var alreadyWon by remember { mutableStateOf(false) }
    var isFirstLoad by remember { mutableStateOf(true) } // <- ДОБАВЛЕНО: флаг первой загрузки

    val focusManager = LocalFocusManager.current

    // Следим за прогрессом из ViewModel
    val userStats by viewModel.userStats.collectAsState()
    val completedPuzzles = userStats?.completedPuzzles ?: 0
    val userLevel = (completedPuzzles / 3) + 1

    // Аватарка пользователя
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Загружаем аватарку при инициализации
    LaunchedEffect(Unit) {
        val avatarPath = authManager.getAvatarPath()
        if (avatarPath != null) {
            val file = File(avatarPath)
            if (file.exists()) {
                avatarBitmap = BitmapFactory.decodeFile(file.absolutePath)
            }
        }
    }

    // Функция проверки победы
    fun checkVictory(): Boolean {
        // Проверяем, что есть слова в кроссворде (не пустая сетка)
        if (words.isEmpty()) return false

        for (i in grid.indices) {
            for (j in grid[i].indices) {
                if (!grid[i][j].isBlack) {
                    val userLetter = userGrid[i][j]
                    val correctLetter = grid[i][j].letter
                    if (userLetter != correctLetter) {
                        return false
                    }
                }
            }
        }
        return true
    }

    // Проверяем победу при каждом изменении userGrid - ИСПРАВЛЕНО
    LaunchedEffect(userGrid) {
        // Пропускаем проверку при первой загрузке или если сетка пустая
        if (isFirstLoad || words.isEmpty()) return@LaunchedEffect

        if (checkVictory() && !alreadyWon) {
            showVictoryDialog = true
            alreadyWon = true
            // Сохраняем в БД через ViewModel только при реальной победе
            viewModel.recordCrosswordCompletion(difficulty, 120000L, 100)
        }
    }

    fun generateNewCrossword() {
        val currentLanguage = themeManager.currentLanguage
        val result = generator.generateCrossword(difficulty, currentLanguage)
        grid = result.first
        words = result.second
        selectedCell = null
        userGrid = Array(8) { Array(8) { null as Char? } }
        showVictoryDialog = false
        alreadyWon = false
        focusManager.clearFocus()
    }

    LaunchedEffect(Unit) {
        generateNewCrossword()
        isFirstLoad = false
    }

    LaunchedEffect(difficulty) {
        if (!isFirstLoad) { // Генерируем только если это не первая загрузка
            generateNewCrossword()
        }
    }

    // Перегенерируем кроссворд при смене языка
    LaunchedEffect(themeManager.currentLanguage) {
        if (!isFirstLoad) {
            generateNewCrossword()
        }
    }

    // Функция для очистки выбранной клетки (можно использовать для кнопки очистки)
    fun clearSelectedCell() {
        selectedCell?.let { (row, col) ->
            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                val newUserGrid = Array(8) { i -> Array(8) { j -> userGrid[i][j] } }
                newUserGrid[row][col] = null
                userGrid = newUserGrid
            }
        }
    }

    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { showVictoryDialog = false },
            title = { Text(stringResource(R.string.congratulations)) },
            text = {
                Text(stringResource(R.string.victory_message, completedPuzzles, (completedPuzzles + 1) / 3 + 1))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVictoryDialog = false
                        generateNewCrossword()
                    }
                ) {
                    Text(stringResource(R.string.new_crossword))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showVictoryDialog = false }
                ) {
                    Text(stringResource(R.string.stay))
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Header с кнопками настроек
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.crossword_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stringResource(R.string.level)}: $userLevel | ${stringResource(R.string.solved)}: $completedPuzzles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Row {
                    ThemeSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))
                    LanguageSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onShowStats(authManager.getUserName(), userLevel) }
                    ) {
                        if (avatarBitmap != null) {
                            Image(
                                bitmap = avatarBitmap!!.asImageBitmap(),
                                contentDescription = stringResource(R.string.statistics),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.AccountCircle,
                                stringResource(R.string.statistics),
                                modifier = Modifier.fillMaxSize(),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, stringResource(R.string.logout))
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.difficulty_label),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    DropdownMenuBox(
                        selectedValue = difficulty,
                        onValueChange = {
                            difficulty = it
                        },
                        options = listOf(
                            stringResource(R.string.easy_difficulty),
                            stringResource(R.string.medium_difficulty),
                            stringResource(R.string.hard_difficulty)
                        )
                    )
                }

                Row {
                    IconButton(onClick = { generateNewCrossword() }) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.new_crossword_button))
                    }
                    IconButton(onClick = {
                        userGrid = Array(8) { Array(8) { null as Char? } }
                        selectedCell = null
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Clear, stringResource(R.string.clear_button))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CrosswordGrid(
                    grid = grid,
                    userGrid = userGrid,
                    selectedCell = selectedCell,
                    onCellSelected = { row, col ->
                        selectedCell = Pair(row, col)
                    },
                    onLetterInput = { row, col, letter ->
                        if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                            val newUserGrid = Array(8) { i -> Array(8) { j -> userGrid[i][j] } }
                            newUserGrid[row][col] = letter
                            userGrid = newUserGrid
                            // Автоматически переходим к следующей клетке, если введена буква
                            if (letter != null) {
                                // Можно добавить логику перехода к следующей клетке
                            }
                        }
                    }
                )
            }

            // Удален отдельный TextField - теперь ввод происходит напрямую в клетках

            if (selectedCell != null) {
                val (row, col) = selectedCell!!
                val currentWord = words.find { word ->
                    (word.direction == Direction.HORIZONTAL && word.row == row && col >= word.col && col < word.col + word.word.length) ||
                            (word.direction == Direction.VERTICAL && word.col == col && row >= word.row && row < word.row + word.word.length)
                }

                if (currentWord != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${stringResource(R.string.hint)}: ${currentWord.clue}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            val directionText = if (currentWord.direction == Direction.HORIZONTAL) {
                                stringResource(R.string.horizontal)
                            } else {
                                stringResource(R.string.vertical)
                            }
                            Text(
                                text = stringResource(R.string.direction_hint, directionText) + " | " + stringResource(R.string.word_length, currentWord.word.length),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            if (words.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(R.string.words_count, words.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val wordsPerColumn = (words.size + 1) / 2
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                words.take(wordsPerColumn).forEach { word ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${word.number}. ",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = word.clue,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val directionText = if (word.direction == Direction.HORIZONTAL) {
                                            "→" // Горизонталь
                                        } else {
                                            "↓" // Вертикаль
                                        }
                                        Text(
                                            text = directionText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (words.size > wordsPerColumn) {
                                Column(modifier = Modifier.weight(1f)) {
                                    words.drop(wordsPerColumn).forEach { word ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${word.number}. ",
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = word.clue,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val directionText = if (word.direction == Direction.HORIZONTAL) {
                                                "→" // Горизонталь
                                            } else {
                                                "↓" // Вертикаль
                                            }
                                            Text(
                                                text = directionText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}