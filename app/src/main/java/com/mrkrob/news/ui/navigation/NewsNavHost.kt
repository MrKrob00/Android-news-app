package com.mrkrob.news.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.ui.categories.CategoriesScreen
import com.mrkrob.news.ui.home.HomeScreen

@Composable
fun NewsNavHost(
    navController: NavHostController,
    onArticleClick: (Article) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
        enterTransition = { fadeIn(tween(TRANSITION_MILLIS)) },
        exitTransition = { fadeOut(tween(TRANSITION_MILLIS)) },
    ) {
        composable<HomeRoute> {
            HomeScreen(onArticleClick = onArticleClick, onShowSnackbar = onShowSnackbar)
        }
        composable<CategoriesRoute> {
            CategoriesScreen(onArticleClick = onArticleClick, onShowSnackbar = onShowSnackbar)
        }
    }
}

fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavDestination?.isOnTopLevel(destination: TopLevelDestination): Boolean =
    this?.hierarchy?.any { it.hasRoute(destination.route::class) } == true

private const val TRANSITION_MILLIS = 200
