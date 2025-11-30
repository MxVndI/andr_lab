package com.example.labs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CrosswordGrid(
    grid: Array<Array<CrosswordCell>>,
    userGrid: Array<Array<Char?>>,
    selectedCell: Pair<Int, Int>?,
    onCellSelected: (Int, Int) -> Unit,
    onLetterInput: (Int, Int, Char?) -> Unit,
    modifier: Modifier = Modifier
) {
    val cellSize = 40.dp

    Card(
        modifier = modifier
            .padding(8.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in grid.indices) {
                Row {
                    for (j in grid[i].indices) {
                        val cell = grid[i][j]
                        val isSelected = selectedCell?.let { it.first == i && it.second == j } == true
                        val userLetter = userGrid[i][j]
                        val focusRequester = remember { FocusRequester() }

                        // Автоматически запрашиваем фокус для выбранной клетки
                        LaunchedEffect(isSelected) {
                            if (isSelected && !cell.isBlack) {
                                focusRequester.requestFocus()
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        cell.isBlack -> Color(0xFF2C2C2C)
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (cell.isBlack) Color(0xFF2C2C2C) else MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable(enabled = !cell.isBlack) {
                                    onCellSelected(i, j)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!cell.isBlack) {
                                if (cell.number != null) {
                                    Text(
                                        text = cell.number.toString(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(3.dp)
                                    )
                                }

                                var textFieldValue by remember(userLetter, isSelected) {
                                    mutableStateOf(
                                        TextFieldValue(
                                            text = userLetter?.toString() ?: "",
                                            selection = if (isSelected) TextRange(0, userLetter?.toString()?.length ?: 0) else TextRange(0)
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BasicTextField(
                                        value = textFieldValue,
                                        onValueChange = { newValue ->
                                            // Ограничиваем ввод одной буквы
                                            val newText = newValue.text.uppercase().take(1)
                                            if (newText.isEmpty() || newText.all { it.isLetter() }) {
                                                textFieldValue = TextFieldValue(
                                                    text = newText,
                                                    selection = TextRange(newText.length)
                                                )
                                                onLetterInput(i, j, newText.firstOrNull())
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .focusRequester(focusRequester)
                                            .focusable(),
                                        textStyle = TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        ),
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Text,
                                            autoCorrect = false
                                        ),
                                        singleLine = true,
                                        readOnly = false
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