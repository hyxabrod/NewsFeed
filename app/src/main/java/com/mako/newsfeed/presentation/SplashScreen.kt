package com.mako.newsfeed.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToList: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onNavigateToList()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "BlinkingText")
    val alpha by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "News Feed",
            fontSize = 32.sp,
            color = Color.Black,
            modifier = Modifier.alpha(alpha)
        )
    }
}
