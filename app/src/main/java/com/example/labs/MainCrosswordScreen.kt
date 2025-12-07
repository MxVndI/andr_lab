package com.example.labs

import CoinManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterial3Api
@Composable
fun MainCrosswordScreen(
    onLogout: () -> Unit,
    onShowStats: (String, Int) -> Unit,
    themeManager: ThemeManager
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val coinManager = remember { CoinManager(context) }
    val viewModel = remember { CrosswordViewModel(context) }
    val generator = remember { CrosswordGenerator(context) }
    val scope = rememberCoroutineScope()
    val yooMoneyService = remember { YooMoneyService(context) }

    var showCoinShopDialog by remember { mutableStateOf(false) }

    // Монетки пользователя через CoinManager
    val userCoins by coinManager.userCoins.observeAsState(0)

    // Устанавливаем текущего пользователя при инициализации
    LaunchedEffect(Unit) {
        authManager.getCurrentEmail()?.let { email ->
            coinManager.setCurrentUserEmail(email)
        }
    }

    // Используем строковые ресурсы для difficulty
    val difficultyOptions = listOf(
        stringResource(R.string.easy_difficulty),
        stringResource(R.string.medium_difficulty),
        stringResource(R.string.hard_difficulty)
    )
    var difficulty by remember {
        mutableStateOf(difficultyOptions[0])
    }

    var grid by remember { mutableStateOf(Array(8) { Array(8) { CrosswordCell(0, 0, isBlack = true) } }) }
    var words by remember { mutableStateOf(listOf<CrosswordWord>()) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var userGrid by remember { mutableStateOf(Array(8) { Array(8) { null as Char? } }) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var alreadyWon by remember { mutableStateOf(false) }
    var isFirstLoad by remember { mutableStateOf(true) }
    var showNotEnoughCoinsDialog by remember { mutableStateOf(false) }
    var showHintUsedDialog by remember { mutableStateOf(false) }
    var lastHintCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Флаг для отслеживания правильно угаданных слов
    // Используем индекс слова в списке как идентификатор
    var correctlyGuessedWordIndices by remember { mutableStateOf(emptySet<Int>()) }

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

    // Функция для проверки, правильно ли угадано слово
    fun isWordGuessedCorrectly(word: CrosswordWord): Boolean {
        for (i in 0 until word.word.length) {
            val row = if (word.direction == Direction.HORIZONTAL) word.row else word.row + i
            val col = if (word.direction == Direction.HORIZONTAL) word.col + i else word.col

            if (row < grid.size && col < grid[0].size) {
                val userLetter = userGrid[row][col]
                val correctLetter = grid[row][col].letter
                if (userLetter != correctLetter) {
                    return false
                }
            }
        }
        return true
    }

    // Функция для обновления списка правильно угаданных слов
    fun updateCorrectlyGuessedWords() {
        val newCorrectlyGuessed = mutableSetOf<Int>()
        words.forEachIndexed { index, word ->
            if (isWordGuessedCorrectly(word)) {
                newCorrectlyGuessed.add(index)
            }
        }
        correctlyGuessedWordIndices = newCorrectlyGuessed
    }

    // Проверяем победу и обновляем угаданные слова
    LaunchedEffect(userGrid) {
        if (isFirstLoad || words.isEmpty()) return@LaunchedEffect

        updateCorrectlyGuessedWords()

        if (checkVictory() && !alreadyWon) {
            showVictoryDialog = true
            alreadyWon = true
            viewModel.recordCrosswordCompletion(difficulty, 120000L, 100)
            // Награждаем монетками за победу
            scope.launch {
                val email = authManager.getCurrentEmail()
                if (email != null) {
                    coinManager.addCoins(email, 10)
                }
            }
        }
    }

    fun generateNewCrossword() {
        val currentLanguage = themeManager.currentLanguage
        val result = generator.generateCrossword(difficulty, currentLanguage)
        grid = result.first
        words = result.second
        selectedCell = null
        userGrid = Array(8) { Array(8) { null as Char? } }
        correctlyGuessedWordIndices = emptySet()
        showVictoryDialog = false
        alreadyWon = false
        lastHintCell = null
        focusManager.clearFocus()
    }

    LaunchedEffect(Unit) {
        generateNewCrossword()
        isFirstLoad = false
    }

    LaunchedEffect(difficulty) {
        if (!isFirstLoad) {
            generateNewCrossword()
        }
    }

    // Перегенерируем кроссворд при смене языка
    LaunchedEffect(themeManager.currentLanguage) {
        if (!isFirstLoad) {
            generateNewCrossword()
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

    // Функция для получения цвета клетки
    fun getCellColor(row: Int, col: Int): Color {
        if (grid[row][col].isBlack) return Color.Unspecified

        // Подсвечиваем последнюю открытую подсказку
        lastHintCell?.let { (hintRow, hintCol) ->
            if (row == hintRow && col == hintCol) {
                return Color(0xFFFFD700).copy(alpha = 0.3f)
            }
        }

        // Проверяем, является ли клетка частью правильно угаданного слова
        words.forEachIndexed { index, word ->
            if (correctlyGuessedWordIndices.contains(index)) {
                val isInWord = if (word.direction == Direction.HORIZONTAL) {
                    word.row == row && col >= word.col && col < word.col + word.word.length
                } else {
                    word.col == col && row >= word.row && row < word.row + word.word.length
                }

                if (isInWord) {
                    return Color(0xFF4CAF50)
                }
            }
        }

        return Color.Unspecified
    }

    // Функция для использования подсказки
    fun useHint() {
        scope.launch {
            val email = authManager.getCurrentEmail() ?: return@launch

            if (userCoins >= 1) {
                val success = coinManager.spendCoins(email, 1)
                if (success) {
                    val randomCell = coinManager.getRandomUnfilledLetter(grid, userGrid)
                    randomCell?.let { (row, col) ->
                        val correctLetter = grid[row][col].letter
                        val newUserGrid = Array(8) { i -> Array(8) { j -> userGrid[i][j] } }
                        newUserGrid[row][col] = correctLetter
                        userGrid = newUserGrid
                        lastHintCell = Pair(row, col)
                        showHintUsedDialog = true
                    }
                }
            } else {
                showNotEnoughCoinsDialog = true
            }
        }
    }

    // Диалог победы
    if (showVictoryDialog) {
        AlertDialog(
            onDismissRequest = { showVictoryDialog = false },
            title = { Text(stringResource(R.string.congratulations)) },
            text = {
                Column {
                    Text(stringResource(R.string.victory_message, completedPuzzles, userLevel))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.coins_reward, 10),
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
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

    // Диалог "Недостаточно монет"
    if (showNotEnoughCoinsDialog) {
        AlertDialog(
            onDismissRequest = { showNotEnoughCoinsDialog = false },
            title = { Text(stringResource(R.string.not_enough_coins)) },
            text = { Text(stringResource(R.string.need_more_coins)) },
            confirmButton = {
                Button(
                    onClick = { showNotEnoughCoinsDialog = false }
                ) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    // Диалог "Подсказка использована"
    if (showHintUsedDialog) {
        AlertDialog(
            onDismissRequest = { showHintUsedDialog = false },
            title = { Text(stringResource(R.string.hint_used)) },
            text = { Text(stringResource(R.string.letter_revealed)) },
            confirmButton = {
                Button(
                    onClick = { showHintUsedDialog = false }
                ) {
                    Text(stringResource(R.string.ok))
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Отображение монеток с меню
                    var coinMenuExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopStart)
                    ) {
                        // Кликабельные монетки
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .clickable { coinMenuExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = stringResource(R.string.coins),
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = userCoins.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.menu),
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Выпадающее меню
                        DropdownMenu(
                            expanded = coinMenuExpanded,
                            onDismissRequest = { coinMenuExpanded = false },
                            modifier = Modifier.width(200.dp)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AddShoppingCart,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.buy_coins))
                                    }
                                },
                                onClick = {
                                    coinMenuExpanded = false
                                    showCoinShopDialog = true
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.payment_history))
                                    }
                                },
                                onClick = {
                                    coinMenuExpanded = false
                                    scope.launch {
                                        val payments = yooMoneyService.getUserPayments()
                                        // TODO: Показать историю платежей
                                    }
                                }
                            )

                            Divider(modifier = Modifier.padding(horizontal = 8.dp))

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.about_coins))
                                    }
                                },
                                onClick = {
                                    coinMenuExpanded = false
                                    // TODO: Показать информацию о монетах
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    ThemeSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))
                    LanguageSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                onShowStats(
                                    authManager.getCurrentEmail()?.substringBefore("@") ?: "Гость",
                                    userLevel
                                )
                            }
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
                        options = difficultyOptions
                    )
                }

                Row {
                    // Кнопка подсказки
                    IconButton(
                        onClick = { useHint() },
                        enabled = userCoins >= 1
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Lightbulb,
                                stringResource(R.string.hint_button),
                                tint = if (userCoins >= 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "1",
                                fontSize = 10.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = { generateNewCrossword() }) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.new_crossword_button))
                    }
                    IconButton(onClick = {
                        userGrid = Array(8) { Array(8) { null as Char? } }
                        selectedCell = null
                        correctlyGuessedWordIndices = emptySet()
                        lastHintCell = null
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
                        }
                    },
                    cellBackgroundColor = { row, col -> getCellColor(row, col) }
                )
            }

            if (selectedCell != null) {
                val (row, col) = selectedCell!!
                val currentWord = words.find { word ->
                    (word.direction == Direction.HORIZONTAL && word.row == row && col >= word.col && col < word.col + word.word.length) ||
                            (word.direction == Direction.VERTICAL && word.col == col && row >= word.row && row < word.row + word.word.length)
                }

                if (currentWord != null) {
                    val wordIndex = words.indexOf(currentWord)
                    val isWordGuessed = correctlyGuessedWordIndices.contains(wordIndex)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isWordGuessed) {
                                Color(0xFFE8F5E9)
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = "${stringResource(R.string.hint)}: ${currentWord.clue}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isWordGuessed) {
                                        Color(0xFF2E7D32)
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    },
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isWordGuessed) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = stringResource(R.string.word_guessed),
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            val directionText = if (currentWord.direction == Direction.HORIZONTAL) {
                                stringResource(R.string.horizontal)
                            } else {
                                stringResource(R.string.vertical)
                            }
                            Text(
                                text = stringResource(R.string.direction_hint, directionText) + " | " + stringResource(R.string.word_length, currentWord.word.length),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isWordGuessed) {
                                    Color(0xFF2E7D32).copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                }
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
                            text = "${stringResource(R.string.words_count, words.size)} (${stringResource(R.string.guessed_count, correctlyGuessedWordIndices.size)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val wordsPerColumn = (words.size + 1) / 2
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                words.take(wordsPerColumn).forEachIndexed { index, word ->
                                    val isWordGuessed = correctlyGuessedWordIndices.contains(index)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${index + 1}. ",
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWordGuessed) {
                                                Color(0xFF4CAF50)
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = word.clue,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isWordGuessed) {
                                                Color(0xFF4CAF50)
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            fontWeight = if (isWordGuessed) FontWeight.Bold else FontWeight.Normal,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val directionText = if (word.direction == Direction.HORIZONTAL) {
                                            "→"
                                        } else {
                                            "↓"
                                        }
                                        Text(
                                            text = directionText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isWordGuessed) {
                                                Color(0xFF4CAF50)
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isWordGuessed) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = stringResource(R.string.word_guessed),
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (words.size > wordsPerColumn) {
                                Column(modifier = Modifier.weight(1f)) {
                                    words.drop(wordsPerColumn).forEachIndexed { index, word ->
                                        val actualIndex = index + wordsPerColumn
                                        val isWordGuessed = correctlyGuessedWordIndices.contains(actualIndex)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${actualIndex + 1}. ",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isWordGuessed) {
                                                    Color(0xFF4CAF50)
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = word.clue,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isWordGuessed) {
                                                    Color(0xFF4CAF50)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                fontWeight = if (isWordGuessed) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            val directionText = if (word.direction == Direction.HORIZONTAL) {
                                                "→"
                                            } else {
                                                "↓"
                                            }
                                            Text(
                                                text = directionText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isWordGuessed) {
                                                    Color(0xFF4CAF50)
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isWordGuessed) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    Icons.Default.CheckCircle,
                                                    contentDescription = stringResource(R.string.word_guessed),
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(16.dp)
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

            // Диалог магазина монет
            if (showCoinShopDialog) {
                CoinShopDialog(
                    onDismiss = { showCoinShopDialog = false },
                    coinManager = coinManager,
                    authManager = authManager
                )
            }
        }
    }
}