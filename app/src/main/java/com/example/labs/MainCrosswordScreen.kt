// MainCrosswordScreen.kt
package com.example.labs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun MainCrosswordScreen(
    onLogout: () -> Unit
) {
    val generator = remember { CrosswordGenerator() }
    var difficulty by remember { mutableStateOf("Легкий") }

    var grid by remember { mutableStateOf(Array(8) { Array(8) { CrosswordCell(0, 0, isBlack = true) } }) }
    var words by remember { mutableStateOf(listOf<CrosswordWord>()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var userGrid by remember { mutableStateOf(Array(8) { Array(8) { null as Char? } }) }

    // Для управления вводом
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var inputText by remember { mutableStateOf("") }

    // Функция для генерации нового кроссворда
    fun generateNewCrossword() {
        val result = generator.generateCrossword(difficulty)
        grid = result.first
        words = result.second
        selectedCell = null
        userGrid = Array(8) { Array(8) { null as Char? } }
        inputText = ""
        focusManager.clearFocus()
    }

    // Генерируем кроссворд при первом запуске
    LaunchedEffect(Unit) {
        generateNewCrossword()
    }

    // Функция для ввода буквы в выбранную клетку
    fun inputLetter(letter: Char) {
        selectedCell?.let { (row, col) ->
            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                // Создаем копию userGrid для обновления
                val newUserGrid = Array(8) { i -> Array(8) { j -> userGrid[i][j] } }
                newUserGrid[row][col] = letter
                userGrid = newUserGrid
            }
        }
    }

    // Функция для очистки выбранной клетки
    fun clearSelectedCell() {
        selectedCell?.let { (row, col) ->
            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                val newUserGrid = Array(8) { i -> Array(8) { j -> userGrid[i][j] } }
                newUserGrid[row][col] = null
                userGrid = newUserGrid
            }
        }
    }

    // Автоматически фокусируемся на поле ввода при выборе клетки
    LaunchedEffect(selectedCell) {
        if (selectedCell != null) {
            delay(100)
            focusRequester.requestFocus()
            inputText = ""
        } else {
            focusManager.clearFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Compact header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Кроссворд",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Выйти",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Compact controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Difficulty selector - compact
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
                        generateNewCrossword()
                    },
                    options = listOf("Легкий", "Средний", "Сложный")
                )
            }

            // Action buttons
            Row {
                IconButton(onClick = { generateNewCrossword() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Новый кроссворд",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    userGrid = Array(8) { Array(8) { null as Char? } }
                    selectedCell = null
                    inputText = ""
                    focusManager.clearFocus()
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Очистить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Crossword grid - main content
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

        // Простое поле ввода
        if (selectedCell != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Введите букву для выбранной клетки:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Простое текстовое поле
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

                    // Кнопки управления
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

        // Show hints for current word when cell is selected
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
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
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

        // Words list with hints
        if (words.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Слова в кроссворде (${words.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Show words in two columns
                    val wordsPerColumn = (words.size + 1) / 2
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Left column
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
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

                        // Right column
                        if (words.size > wordsPerColumn) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
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