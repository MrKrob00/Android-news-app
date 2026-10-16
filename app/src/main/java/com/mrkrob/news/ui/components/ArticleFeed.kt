package com.mrkrob.news.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.mrkrob.news.R
import com.mrkrob.news.domain.model.Article

@Composable
fun ArticleFeed(
    articles: LazyPagingItems<Article>,
    listState: LazyListState,
    onArticleClick: (Article) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    emptyContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val refresh = articles.loadState.refresh
    val hasArticles = articles.itemCount > 0

    val remoteRefresh = articles.loadState.mediator?.refresh
    if (hasArticles && remoteRefresh is LoadState.Error) {
        val message = stringResource(R.string.refresh_failed)
        val actionLabel = stringResource(R.string.retry)
        LaunchedEffect(remoteRefresh) {
            if (onShowSnackbar(message, actionLabel)) articles.retry()
        }
    }

    when {
        hasArticles -> Box(modifier.fillMaxSize()) {
            ArticleList(articles, listState, onArticleClick, header)
            if (refresh is LoadState.Loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
        refresh is LoadState.Loading -> ArticleFeedPlaceholder(modifier)
        refresh is LoadState.Error -> ErrorMessage(refresh.error, onRetry = articles::retry, modifier)
        else -> Box(modifier) { emptyContent() }
    }
}

@Composable
private fun ArticleList(
    articles: LazyPagingItems<Article>,
    listState: LazyListState,
    onArticleClick: (Article) -> Unit,
    header: (@Composable () -> Unit)?,
) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        if (header != null) {
            item(key = "header", contentType = "header") { header() }
        }
        items(
            count = articles.itemCount,
            key = articles.itemKey { it.id },
            contentType = { index -> if (index == 0) "hero" else "article" },
        ) { index ->
            val article = articles[index] ?: return@items
            if (index == 0) {
                ArticleHeroItem(article, onClick = { onArticleClick(article) })
            } else {
                Column {
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    ArticleItem(article, onClick = { onArticleClick(article) })
                }
            }
        }
        item(key = "footer", contentType = "footer") {
            AppendFooter(articles.loadState.append, onRetry = articles::retry)
        }
    }
}

@Composable
private fun AppendFooter(state: LoadState, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            is LoadState.Loading -> CircularProgressIndicator(Modifier.size(32.dp))
            is LoadState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.load_more_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            is LoadState.NotLoading -> Unit
        }
    }
}
