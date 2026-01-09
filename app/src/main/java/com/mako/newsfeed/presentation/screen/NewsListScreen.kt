package com.mako.newsfeed.presentation.screen

import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mako.newsfeed.core.ui.OfflineBanner
import com.mako.newsfeed.presentation.component.NewsItem
import com.mako.newsfeed.presentation.viewmodel.NewsFeedListEffect
import com.mako.newsfeed.presentation.viewmodel.NewsFeedListIntent
import com.mako.newsfeed.presentation.viewmodel.NewsListViewModel
import com.mako.newsfeed.ui.theme.Dimens
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListScreen2(
    onArticleClick: (String) -> Unit,
    viewModel: NewsListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val gridCells =
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GridCells.Fixed(3)
        } else {
            GridCells.Fixed(2)
        }

    val listState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NewsFeedListEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                NewsFeedListEffect.ScrollToTop -> {
                    listState.scrollToItem(0)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("News Feed") },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = Dimens.Padding8),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Top News (US)",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = Dimens.Padding8)
                        )
                        Switch(
                            modifier = Modifier.padding(end = Dimens.Padding8),
                            checked = state.isTopNews,
                            onCheckedChange = {
                                viewModel.processIntent(
                                    NewsFeedListIntent.ToggleTopNews
                                )
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isOffline) {
                OfflineBanner()
            }
            PullToRefreshBox(
                isRefreshing = state.isPullToRefreshVisible,
                onRefresh = { viewModel.processIntent(NewsFeedListIntent.Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                LaunchedEffect(listState) {
                    snapshotFlow {
                        val layoutInfo = listState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                        totalItems > 0 && lastVisible >= totalItems - 5
                    }
                        .distinctUntilChanged()
                        .collect { shouldLoad ->
                            Log.d("NewsListScreen", "shouldLoad = $shouldLoad")
                            if (shouldLoad) {
                                viewModel.processIntent(NewsFeedListIntent.LoadNextPage)
                            }
                        }
                }

                LazyVerticalGrid(
                    columns = gridCells,
                    state = listState,
                    contentPadding = PaddingValues(Dimens.Padding16),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Padding16),
                    verticalArrangement = Arrangement.spacedBy(Dimens.Padding16),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = state.articles, key = { it.uuid }) { article ->
                        NewsItem(
                            urlToImage = article.urlToImage,
                            title = article.title,
                            publishedAt = article.publishedAt,
                            sourceName = article.sourceName,
                            onClick = { onArticleClick(article.uuid) }
                        )
                    }

                    if (state.isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.Padding16),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                    }
                }
            }
        }
    }
}
