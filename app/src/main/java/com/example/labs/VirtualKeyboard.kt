// VirtualKeyboard.kt
package com.example.labs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirtualKeyboard(
    onLetterClicked: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val russianLetters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"
    val rows = listOf(
        russianLetters.substring(0, 8),
        russianLetters.substring(8, 16),
        russianLetters.substring(16, 24),
        russianLetters.substring(24, 33)
    )

    Column(modifier = modifier) {
        rows.forEach { rowLetters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                rowLetters.forEach { letter ->
                    OutlinedButton(
                        onClick = { onLetterClicked(letter) },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                        )
                    ) {
                        Text(
                            letter.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}