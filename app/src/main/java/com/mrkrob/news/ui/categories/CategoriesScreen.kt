package com.mrkrob.news.ui.categories

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.mrkrob.news.R
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsCategory
import com.mrkrob.news.ui.components.ArticleFeed
import com.mrkrob.news.ui.components.MessageView

@Composable
fun CategoriesScreen(
    onArticleClick: (Article) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    CategoriesScreen(
        selectedCategory = selectedCategory,
        onCategorySelect = viewModel::onCategorySelect,
        articles = viewModel.articles.collectAsLazyPagingItems(),
        onArticleClick = onArticleClick,
        onShowSnackbar = onShowSnackbar,
    )
}

@Composable
internal fun CategoriesScreen(
    selectedCategory: NewsCategory,
    onCategorySelect: (NewsCategory) -> Unit,
    articles: LazyPagingItems<Article>,
    onArticleClick: (Article) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        )
        CategoryChips(
            selectedCategory = selectedCategory,
            onCategorySelect = onCategorySelect,
        )
        val listState = rememberSaveable(selectedCategory, saver = LazyListState.Saver) { LazyListState() }
        ArticleFeed(
            articles = articles,
            listState = listState,
            onArticleClick = onArticleClick,
            onShowSnackbar = onShowSnackbar,
            emptyContent = {
                MessageView(
                    illustration = R.drawable.illustration_news,
                    title = stringResource(R.string.empty_feed_title),
                    message = stringResource(R.string.empty_feed_message),
                )
            },
        )
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: NewsCategory,
    onCategorySelect: (NewsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedCategory.ordinal)
    LazyRow(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(NewsCategory.entries, key = { it.name }) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelect(category) },
                label = { Text(stringResource(category.titleRes)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(category.iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
            )
        }
    }
}

@get:StringRes
private val NewsCategory.titleRes: Int
    get() = when (this) {
        NewsCategory.BUSINESS -> R.string.category_business
        NewsCategory.ENTERTAINMENT -> R.string.category_entertainment
        NewsCategory.GENERAL -> R.string.category_general
        NewsCategory.HEALTH -> R.string.category_health
        NewsCategory.SCIENCE -> R.string.category_science
        NewsCategory.SPORTS -> R.string.category_sports
        NewsCategory.TECHNOLOGY -> R.string.category_technology
    }

@get:DrawableRes
private val NewsCategory.iconRes: Int
    get() = when (this) {
        NewsCategory.BUSINESS -> R.drawable.ic_category_business
        NewsCategory.ENTERTAINMENT -> R.drawable.ic_category_entertainment
        NewsCategory.GENERAL -> R.drawable.ic_category_general
        NewsCategory.HEALTH -> R.drawable.ic_category_health
        NewsCategory.SCIENCE -> R.drawable.ic_category_science
        NewsCategory.SPORTS -> R.drawable.ic_category_sports
        NewsCategory.TECHNOLOGY -> R.drawable.ic_category_technology
    }
