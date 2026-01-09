package com.mako.newsfeed.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mako.newsfeed.core.NetworkMonitor
import com.mako.newsfeed.domain.usecase.GetArticleByIdUseCase
import com.mako.newsfeed.domain.usecase.GetArticleByIdUseCaseArgs
import com.mako.newsfeed.domain.usecase.SaveArticleUseCase
import com.mako.newsfeed.domain.usecase.SaveArticleUseCaseArgs
import com.mako.newsfeed.presentation.model.ArticlePresentationEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArticleDetailsState(
    val article: ArticlePresentationEntity? = null,
    val isSaving: Boolean = false,
    val isOffline: Boolean = false
)

sealed interface ArticleDetailsIntent {
    data object SaveArticle : ArticleDetailsIntent
}

sealed interface ArticleDetailsEffect {
    data class ShowSnackbar(val message: String) : ArticleDetailsEffect
}

@HiltViewModel(assistedFactory = ArticleDetailsViewModel.Factory::class)
class ArticleDetailsViewModel
@AssistedInject constructor(
    private val getArticleByIdUseCase: GetArticleByIdUseCase,
    private val saveArticleUseCase: SaveArticleUseCase,
    private val networkMonitor: NetworkMonitor,
    @Assisted private val articleId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(articleId: String): ArticleDetailsViewModel
    }

    private val _state = MutableStateFlow(ArticleDetailsState())
    val state: StateFlow<ArticleDetailsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<ArticleDetailsEffect>()
    val effect: SharedFlow<ArticleDetailsEffect> = _effect.asSharedFlow()

    init {
        loadArticle()
        observeNetworkStatus()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _state.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    private fun loadArticle() {
        viewModelScope.launch {
            try {
                val article = getArticleByIdUseCase(GetArticleByIdUseCaseArgs(articleId))
                _state.update { it.copy(article = article) }
            } catch (e: Exception) {
                Log.d("ArticleDetailsViewModel", "Failed to load article: ${e.message}")
                _effect.emit(ArticleDetailsEffect.ShowSnackbar("Failed to load article"))
            }
        }
    }

    fun processIntent(intent: ArticleDetailsIntent) {
        when (intent) {
            is ArticleDetailsIntent.SaveArticle -> saveArticle()
        }
    }

    private fun saveArticle() {
        val article = _state.value.article ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            try {
                saveArticleUseCase(
                    SaveArticleUseCaseArgs(
                        uuid = article.uuid,
                        sourceName = article.sourceName,
                        author = article.author,
                        title = article.title,
                        description = article.description,
                        url = article.url,
                        urlToImage = article.urlToImage,
                        publishedAt = article.publishedAt,
                        content = article.content
                    )
                )
                _effect.emit(ArticleDetailsEffect.ShowSnackbar("Done"))
            } catch (e: Exception) {
                Log.d("ArticleDetailsViewModel", "Failed to save: ${e.message}")
                _effect.emit(ArticleDetailsEffect.ShowSnackbar("Failed to save"))
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
}
