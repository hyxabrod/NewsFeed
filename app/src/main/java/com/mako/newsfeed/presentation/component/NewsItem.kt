package com.mako.newsfeed.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.SubcomposeAsyncImage
import com.mako.newsfeed.core.fixUrl
import com.mako.newsfeed.core.toArticleReadableDateTime
import com.mako.newsfeed.ui.theme.Dimens

@Composable
fun NewsItem(
        urlToImage: String,
        title: String,
        sourceName: String,
        publishedAt: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(defaultElevation = Dimens.Elevation4)
    ) {
        Column {
            SubcomposeAsyncImage(
                    model = urlToImage.fixUrl(),
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Column(
                                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement =
                                        androidx.compose.foundation.layout.Arrangement.Center
                        ) { CircularProgressIndicator(modifier = Modifier.width(Dimens.Width24)) }
                    },
                    error = {
                        Box(contentAlignment = Alignment.Center) { Text(text = "No image") }
                    },
            )

            Column(modifier = Modifier.padding(Dimens.Padding16)) {
                Text(
                        text = sourceName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Dimens.Padding8))
                Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                )
                publishedAt.let {
                    Spacer(modifier = Modifier.height(Dimens.Padding8))
                    Text(
                            text = it.toArticleReadableDateTime(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
