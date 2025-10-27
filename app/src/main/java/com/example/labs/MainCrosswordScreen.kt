// MainCrosswordScreen.kt
package com.example.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.labs.CrosswordGenerator
import com.example.labs.DropdownMenuBox

@Composable
fun MainCrosswordScreen(
    onLogout: () -> Unit
) {
    val generator = remember { CrosswordGenerator() }
    var difficulty by remember { mutableStateOf("Легкий") }
    var isDarkTheme by remember { mutableStateOf(false) }

    var grid by remember { mutableStateOf(Array(8) { Array(8) { CrosswordCell(0, 0, isBlack = true) } }) }
    var words by remember { mutableStateOf(listOf<CrosswordWord>()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var userGrid by remember { mutableStateOf(Array(8) { Array(8) { null as Char? } }) }

    // Функция для генерации нового кроссворда
    fun generateNewCrossword() {
        val result = generator.generateCrossword(difficulty)
        grid = result.first
        words = result.second
        selectedCell = null
        userGrid = Array(8) { Array(8) { null as Char? } }
    }

    // Генерируем кроссворд при первом запуске
    LaunchedEffect(Unit) {
        generateNewCrossword()
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
                }
            )
        }

        // Simple virtual keyboard for input
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Выберите букву:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        OutlinedButton(
                            onClick = {
                                try {
                                    selectedCell?.let { (row, col) ->
                                        if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                                            userGrid[row][col] = null
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore errors
                                }
                            },
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "Очистить",
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // Simple keyboard with basic letters
                    val basicLetters = "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЫЭЮЯ"
                    
                    // First row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        basicLetters.substring(0, 8).forEach { letter ->
                            OutlinedButton(
                                onClick = {
                                    try {
                                        selectedCell?.let { (row, col) ->
                                            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                                                userGrid[row][col] = letter
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore errors
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Second row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        basicLetters.substring(8, 16).forEach { letter ->
                            OutlinedButton(
                                onClick = {
                                    try {
                                        selectedCell?.let { (row, col) ->
                                            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                                                userGrid[row][col] = letter
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore errors
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Third row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        basicLetters.substring(16, 24).forEach { letter ->
                            OutlinedButton(
                                onClick = {
                                    try {
                                        selectedCell?.let { (row, col) ->
                                            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                                                userGrid[row][col] = letter
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore errors
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Fourth row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        basicLetters.substring(24, 32).forEach { letter ->
                            OutlinedButton(
                                onClick = {
                                    try {
                                        selectedCell?.let { (row, col) ->
                                            if (row < grid.size && col < grid[0].size && !grid[row][col].isBlack) {
                                                userGrid[row][col] = letter
                                            }
                                        }
                                    } catch (e: Exception) {
                                        // Ignore errors
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Text(
                                    text = letter.toString(),
                                    fontSize = 14.sp
                                )
                            }
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
