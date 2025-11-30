package com.example.labs

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun StatisticsScreen(
    userName: String,
    userLevel: Int,
    onBack: () -> Unit,
    themeManager: ThemeManager
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val viewModel = remember { CrosswordViewModel(context) }
    val userStats by viewModel.userStats.collectAsState()

    val completedPuzzles = userStats?.completedPuzzles ?: 0
    val averageTime = userStats?.averageTime ?: 0L
    val totalScore = userStats?.totalScore ?: 0

    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showAvatarDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadAvatar(context, authManager)?.let {
            avatarBitmap = it
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            saveAvatarFromUri(context, authManager, it)?.let { bitmap ->
                avatarBitmap = bitmap
            }
        }
    }
    Surface( modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    ThemeSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))
                    LanguageSwitchButton(themeManager = themeManager)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                                text = "${stringResource(R.string.level)}: $userLevel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .clickable { showAvatarDialog = true }
                        ) {
                            if (avatarBitmap != null) {
                                Image(
                                    bitmap = avatarBitmap!!.asImageBitmap(),
                                    contentDescription = stringResource(R.string.user),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = stringResource(R.string.user),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.progress_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        title = stringResource(R.string.completed_puzzles),
                        value = "$completedPuzzles"
                    )

                    StatItem(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.current_level),
                        value = "$userLevel"
                    )

                    StatItem(
                        icon = Icons.Default.KeyboardArrowUp,
                        title = stringResource(R.string.to_next_level),
                        value = "${3 - (completedPuzzles % 3)}"
                    )

                    StatItem(
                        icon = Icons.Default.DateRange,
                        title = stringResource(R.string.average_time),
                        value = "${averageTime / 60000} ${stringResource(R.string.minutes)}"
                    )

                    StatItem(
                        icon = Icons.Default.Star,
                        title = stringResource(R.string.total_score),
                        value = "$totalScore"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.achievements_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 1,
                        title = stringResource(R.string.novice_achievement),
                        description = stringResource(R.string.novice_description)
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 3,
                        title = stringResource(R.string.amateur_achievement),
                        description = stringResource(R.string.amateur_description)
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 5,
                        title = stringResource(R.string.expert_achievement),
                        description = stringResource(R.string.expert_description)
                    )

                    AchievementItem(
                        achieved = completedPuzzles >= 10,
                        title = stringResource(R.string.master_achievement),
                        description = stringResource(R.string.master_description)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                                context.getString(R.string.share_stats_text, userName, userLevel, completedPuzzles)
                            )
                        }
                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                context.getString(R.string.share_stats)
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Share, stringResource(R.string.share_stats))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.share_stats))
                }

                OutlinedButton(
                    onClick = { showAvatarDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoCamera, stringResource(R.string.change_avatar))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.change_avatar))
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.back_to_crossword))
                }
            }
        }
    }

    if (showAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showAvatarDialog = false },
            title = { Text(stringResource(R.string.change_avatar)) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                            showAvatarDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, stringResource(R.string.select_image))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.select_image))
                    }
                    if (avatarBitmap != null) {
                        TextButton(
                            onClick = {
                                authManager.removeAvatar()
                                avatarBitmap = null
                                val avatarFile = File(context.filesDir, "avatar_${authManager.getCurrentUser()}.jpg")
                                if (avatarFile.exists()) {
                                    avatarFile.delete()
                                }
                                showAvatarDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, stringResource(R.string.remove_avatar))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.remove_avatar))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarDialog = false }) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }
}

private fun loadAvatar(context: android.content.Context, authManager: AuthManager): Bitmap? {
    val avatarPath = authManager.getAvatarPath()
    return if (avatarPath != null) {
        val file = File(avatarPath)
        if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    } else {
        null
    }
}

private fun saveAvatarFromUri(
    context: android.content.Context,
    authManager: AuthManager,
    uri: Uri
): Bitmap? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val avatarFile = File(context.filesDir, "avatar_${authManager.getCurrentUser()}.jpg")
        val outputStream = FileOutputStream(avatarFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.close()

        authManager.saveAvatarPath(avatarFile.absolutePath)

        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
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