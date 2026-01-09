package com.mako.newsfeed.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mako.newsfeed.core.NetworkMonitor
import com.mako.newsfeed.domain.repository.UpgradeRequiredException
import com.mako.newsfeed.domain.usecase.GetHeadlinesUseCase
import com.mako.newsfeed.domain.usecase.GetHeadlinesUseCaseArgs
import com.mako.newsfeed.presentation.model.NewsArticlePresentationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsFeedListState(
    val isTopNews: Boolean = true,
    val isLoading: Boolean = false,
    val isPullToRefreshVisible: Boolean = false,
    val articles: List<NewsArticlePresentationEntity> = emptyList(),
    val isOffline: Boolean = false
)

sealed interface NewsFeedListIntent {
    object ToggleTopNews : NewsFeedListIntent
    object LoadNextPage : NewsFeedListIntent
    object Refresh : NewsFeedListIntent
}

sealed interface NewsFeedListEffect {
    data class ShowToast(val message: String) : NewsFeedListEffect
    object ScrollToTop : NewsFeedListEffect
}

@HiltViewModel
class NewsListViewModel
@Inject
constructor(
    private val getHeadlinesUseCase: GetHeadlinesUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow(NewsFeedListState())
    val state: StateFlow<NewsFeedListState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<NewsFeedListEffect>()
    val effect: SharedFlow<NewsFeedListEffect> = _effect.asSharedFlow()

    private var currentPage = 1
    private var canLoadMore = true

    init {
        Log.d("NewsListViewModel", "VM::init")
        observeNetworkStatus()
        loadHeadlines(page = 1, isTopNews = state.value.isTopNews)
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _state.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun processIntent(intent: NewsFeedListIntent) {
        when (intent) {
            is NewsFeedListIntent.LoadNextPage -> onLoadNext()
            is NewsFeedListIntent.Refresh -> onRefresh()
            is NewsFeedListIntent.ToggleTopNews -> onToggleNews()
        }
    }

    private fun onToggleNews() {
        currentPage = 1
        canLoadMore = true
        val isTopNews = !_state.value.isTopNews
        _state.update { it.copy(isTopNews = isTopNews) }
        loadHeadlines(page = 1, isTopNews = isTopNews, isRefresh = true)
        viewModelScope.launch { _effect.emit(NewsFeedListEffect.ScrollToTop) }
    }

    private fun onRefresh() {
        if (!_state.value.isLoading) {
            currentPage = 1
            canLoadMore = true
            _state.update { it.copy(isPullToRefreshVisible = true) }
            loadHeadlines(page = 1, isTopNews = state.value.isTopNews, isRefresh = true)
        }
    }

    private fun onLoadNext() {
        Log.d(
            "NewsListViewModel",
            "VM::LoadNextPage #1 (${currentPage + 1}) isL = ${_state.value.isLoading}, isP = ${_state.value.isPullToRefreshVisible}, canL = $canLoadMore"
        )
        if (!_state.value.isLoading && !_state.value.isPullToRefreshVisible) {
            if (canLoadMore) {
                Log.d("NewsListViewModel", "VM::LoadNextPage #2")
                loadHeadlines(
                    page = currentPage + 1,
                    isRefresh = false,
                    isTopNews = state.value.isTopNews
                )
            } else {
                viewModelScope.launch {
                    _effect.emit(NewsFeedListEffect.ShowToast("There is no more articles"))
                }
            }
        }
    }

    private fun loadHeadlines(page: Int, isRefresh: Boolean = true, isTopNews: Boolean) {
        Log.d("NewsListViewModel", "VM::loadHeadlines #1")
        viewModelScope.launch {
            if (isRefresh) {
                _state.update { it.copy(isPullToRefreshVisible = true) }
            } else {
                _state.update { it.copy(isLoading = true) }
            }

            try {
                val newArticles: List<NewsArticlePresentationEntity> =
                    getHeadlinesUseCase(GetHeadlinesUseCaseArgs(page, isTopNews))

                _state.update { currentState ->
                    val updatedArticles =
                        if (page == 1) {
                            newArticles
                        } else {
                            currentState.articles + newArticles
                        }
                    currentState.copy(
                        articles = updatedArticles,
                        isPullToRefreshVisible = false,
                        isLoading = false,
                    )
                }
                currentPage = page
                canLoadMore = newArticles.isNotEmpty()
                Log.d(
                    "NewsListViewModel",
                    "VM::loadHeadlines canLoadMore = $canLoadMore (${newArticles.size})"
                )
            } catch (e: Exception) {
                Log.d("NewsListViewModel", "Error loading headlines: ${e.message} ${e.stackTrace}")
                if (e is UpgradeRequiredException) {
                    _effect.emit(NewsFeedListEffect.ShowToast("Free limit reached"))
                }
                _state.update {
                    it.copy(
                        isPullToRefreshVisible = false,
                        isLoading = false,
                    )
                }
            }
        }
    }
}
