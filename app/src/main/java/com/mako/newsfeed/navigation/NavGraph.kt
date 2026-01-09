package com.mako.newsfeed.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.mako.newsfeed.presentation.screen.ArticleDetailsScreen
import com.mako.newsfeed.presentation.screen.NewsListScreen2
import com.mako.newsfeed.ui.SplashScreen
import kotlinx.serialization.Serializable

@Serializable
data object Splash : NavKey

@Serializable
data object List : NavKey

@Serializable
data class Details(val articleId: String) : NavKey

@Composable
fun NavGraph() {
    val backStack = rememberNavBackStack(Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen(
                    onNavigateToList = {
                        backStack.clear()
                        backStack.add(List)
                    }
                )
            }
            entry<List> {
                NewsListScreen2(
                    onArticleClick = { articleId -> backStack.add(Details(articleId)) }
                )
            }
            entry<Details> { details ->
                ArticleDetailsScreen(
                    articleId = details.articleId,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}