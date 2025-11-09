package com.example.labs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .alpha(alpha)
        )
    }

    if (visible) {
        onTimeout()
    }
}
