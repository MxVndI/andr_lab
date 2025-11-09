package com.example.labs

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatisticsScreen(
    userName: String,
    userLevel: Int,
    onBack: () -> Unit,
    themeManager: ThemeManager
) {
    val context = LocalContext.current
    val viewModel = remember { CrosswordViewModel(context) }
    val userStats by viewModel.userStats.collectAsState()

    val completedPuzzles = userStats?.completedPuzzles ?: 0
    val averageTime = userStats?.averageTime ?: 0L
    val totalScore = userStats?.totalScore ?: 0
    Surface( modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header с кнопками настроек
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Статистика",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    ThemeSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))
                    LanguageSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Уровень: $userLevel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Пользователь",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Прогресс",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Решено кроссвордов",
                        value = "$completedPuzzles"
                    )

                    StatItem(
                        icon = Icons.Default.Star,
                        title = "Текущий уровень",
                        value = "$userLevel"
                    )

                    StatItem(
                        icon = Icons.Default.KeyboardArrowUp,
                        title = "До следующего уровня",
                        value = "${3 - (completedPuzzles % 3)}"
                    )

                    StatItem(
                        icon = Icons.Default.DateRange,
                        title = "Среднее время",
                        value = "${averageTime / 60000} мин"
                    )

                    StatItem(
                        icon = Icons.Default.Star,
                        title = "Общий счет",
                        value = "$totalScore"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Achievements
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Достижения",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 1,
                        title = "Новичок",
                        description = "Решите первый кроссворд"
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 3,
                        title = "Любитель",
                        description = "Решите 3 кроссворда"
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 5,
                        title = "Эксперт",
                        description = "Решите 5 кроссвордов"
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 10,
                        title = "Мастер",
                        description = "Решите 10 кроссвордов"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Моя статистика в Кроссворд Генераторе: $userName, уровень $userLevel. " +
                                        "Решено кроссвордов: $completedPuzzles. Присоединяйтесь!"
                            )
                        }
                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Поделиться статистикой"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Share, "Поделиться")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Поделиться статистикой")
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Вернуться к кроссворду")
                }
            }
        }
    }
}

@Composable
fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AchievementItem(achieved: Boolean, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (achieved) Icons.Default.Check else Icons.Default.Close,
            contentDescription = title,
            tint = if (achieved) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (achieved) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.outline
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (achieved) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
            )
        }
    }
}