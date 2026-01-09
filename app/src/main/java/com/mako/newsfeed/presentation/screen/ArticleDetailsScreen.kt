package com.mako.newsfeed.presentation.screen

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mako.newsfeed.core.fixUrl
import com.mako.newsfeed.core.ui.OfflineBanner
import com.mako.newsfeed.presentation.viewmodel.ArticleDetailsEffect
import com.mako.newsfeed.presentation.viewmodel.ArticleDetailsIntent
import com.mako.newsfeed.presentation.viewmodel.ArticleDetailsViewModel
import com.mako.newsfeed.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsScreen(
    articleId: String,
    onNavigateBack: () -> Unit,
    viewModel: ArticleDetailsViewModel =
        hiltViewModel(
            creationCallback = { factory: ArticleDetailsViewModel.Factory ->
                factory.create(articleId)
            }
        )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ArticleDetailsEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article (Web view)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isOffline) {
                OfflineBanner()
            }

            state.article?.let { article ->
                Column(modifier = Modifier.fillMaxSize()) {
                    ArticleWebView(
                        url = article.url, modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.Padding16)
                    ) {
                        Button(
                            onClick = {
                                viewModel.processIntent(ArticleDetailsIntent.SaveArticle)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.Height48),
                            enabled = !state.isSaving
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (state.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(Dimens.LoadingIndicatorSize),
                                        strokeWidth = Dimens.Width2,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
                ?: run {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
        }
    }
}

@Composable
fun ArticleWebView(url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val webViewState = rememberSaveable { Bundle() }
    val webView = remember { WebView(context) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(webView) {
        webView.webViewClient =
            object : WebViewClient() {
                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: android.graphics.Bitmap?
                ) {
                    Log.d("WebView", "Page started loading: ${url?.fixUrl()}")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d("WebView", "Page finished loading: ${url?.fixUrl()}")
                    isLoading = false
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    view?.loadUrl(url.fixUrl());
                    return false
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    Log.e(
                        "WebView",
                        "Error loading resource:  ${request?.url.toString().fixUrl()}, " +
                                "errorCode=${error?.errorCode}, " +
                                "description=${error?.description}"
                    )
                    isLoading = false
                }

                override fun onReceivedHttpError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    errorResponse: android.webkit.WebResourceResponse?
                ) {
                    Log.w(
                        "WebView",
                        "HTTP error: ${request?.url.toString().fixUrl()}, " +
                                "statusCode=${errorResponse?.statusCode}, " +
                                "reasonPhrase=${errorResponse?.reasonPhrase}"
                    )
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: android.net.http.SslError?
                ) {
                    Log.w(
                        "WebView",
                        "SSL error: ${error?.url.toString().fixUrl()}, " +
                                "primaryError=${error?.primaryError}"
                    )
                    handler?.proceed()
                }
            }
        Log.d("WebView", "Configuring WebView settings to bypass CORS and security restrictions")
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        if (webViewState.isEmpty) {
            webView.loadUrl(url.fixUrl())
        } else {
            webView.restoreState(webViewState)
            isLoading = false
        }
    }

    DisposableEffect(Unit) { onDispose { webView.saveState(webViewState) } }

    Box(modifier = modifier) {
        AndroidView(factory = { webView })
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(Dimens.LoadingIndicatorBigSize)
            )
        }
    }
}

