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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    var difficulty by remember { mutableStateOf("Легкий") }

    var grid by remember { mutableStateOf(Array(8) { Array(8) { CrosswordCell(0, 0, isBlack = true) } }) }
    var words by remember { mutableStateOf(listOf<CrosswordWord>()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var userGrid by remember { mutableStateOf(Array(8) { Array(8) { null as Char? } }) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var alreadyWon by remember { mutableStateOf(false) }
    var isFirstLoad by remember { mutableStateOf(true) } // <- ДОБАВЛЕНО: флаг первой загрузки

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf("") }

    // Следим за прогрессом из ViewModel
    val userStats by viewModel.userStats.collectAsState()
    val completedPuzzles = userStats?.completedPuzzles ?: 0
    val userLevel = (completedPuzzles / 3) + 1

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
        val result = generator.generateCrossword(difficulty)
        grid = result.first
        words = result.second
        selectedCell = null
        userGrid = Array(8) { Array(8) { null as Char? } }
        inputText = ""
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

    fun inputLetter(letter: Char) {
        selectedCell?.let { (row, col) ->
            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                val newUserGrid = Array(8) { i -> Array(8) { j -> userGrid[i][j] } }
                newUserGrid[row][col] = letter
                userGrid = newUserGrid
            }
        }
    }

    fun clearSelectedCell() {
        selectedCell?.let { (row, col) ->
            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                val newUserGrid = Array(8) { i -> Array(8) { j -> userGrid[i][j] } }
                newUserGrid[row][col] = null
                userGrid = newUserGrid
            }
        }
    }

    LaunchedEffect(selectedCell) {
        if (selectedCell != null) {
            delay(100)
            focusRequester.requestFocus()
            inputText = ""
        } else {
            focusManager.clearFocus()
        }
    }

    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { showVictoryDialog = false },
            title = { Text("Поздравляем! 🎉") },
            text = {
                Text("Вы успешно решили кроссворд!\n" +
                        "Решено кроссвордов: ${completedPuzzles}\n" + // Показываем +1 к текущему
                        "Уровень: ${(completedPuzzles + 1) / 3 + 1}")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVictoryDialog = false
                        generateNewCrossword()
                    }
                ) {
                    Text("Новый кроссворд")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showVictoryDialog = false }
                ) {
                    Text("Остаться")
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
                        text = "Кроссворд",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Уровень: $userLevel | Решено: $completedPuzzles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Row {
                    ThemeSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))
                    LanguageSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = {
                        onShowStats(authManager.getUserName(), userLevel)
                    }) {
                        Icon(Icons.Default.AccountCircle, "Статистика")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, "Выйти")
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
                        text = "Сложность:",
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
                        Icon(Icons.Default.Refresh, "Новый кроссворд")
                    }
                    IconButton(onClick = {
                        userGrid = Array(8) { Array(8) { null as Char? } }
                        selectedCell = null
                        inputText = ""
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Clear, "Очистить")
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
                        inputText = ""
                    }
                )
            }

            if (selectedCell != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Введите букву для выбранной клетки:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { newText ->
                                if (newText.length <= 1) {
                                    inputText = newText.uppercase()
                                    if (newText.isNotEmpty()) {
                                        val letter = newText.last().uppercaseChar()
                                        inputLetter(letter)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            placeholder = { Text("Введите одну букву") },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clearSelectedCell()
                                    inputText = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Очистить клетку")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = {
                                    selectedCell = null
                                    inputText = ""
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Готово")
                            }
                        }
                    }
                }
            }

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
                                text = "Подсказка: ${currentWord.clue}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Слово: ${currentWord.word.length} букв",
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
                            text = "Слова в кроссворде (${words.size}):",
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