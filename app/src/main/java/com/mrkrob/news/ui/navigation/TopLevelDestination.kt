package com.mrkrob.news.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.mrkrob.news.R
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object CategoriesRoute

enum class TopLevelDestination(
    val route: Any,
    @DrawableRes val icon: Int,
    @DrawableRes val selectedIcon: Int,
    @StringRes val label: Int,
) {
    HOME(
        route = HomeRoute,
        icon = R.drawable.ic_home,
        selectedIcon = R.drawable.ic_home_filled,
        label = R.string.nav_home,
    ),
    CATEGORIES(
        route = CategoriesRoute,
        icon = R.drawable.ic_category,
        selectedIcon = R.drawable.ic_category_filled,
        label = R.string.nav_categories,
    ),
}
