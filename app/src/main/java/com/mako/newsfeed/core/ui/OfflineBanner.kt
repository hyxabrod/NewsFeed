package com.mako.newsfeed.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.mako.newsfeed.core.theme.Dimens

@Composable
fun OfflineBanner(modifier: Modifier = Modifier, message: String = "Offline Mode") {
    Box(
            modifier =
                    modifier.fillMaxWidth()
                            .height(Dimens.Height48)
                            .background(Color.Red),
            contentAlignment = Alignment.Center
    ) { Text(text = message, color = Color.White, textAlign = TextAlign.Center) }
}
