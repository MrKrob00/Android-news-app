package com.mrkrob.news.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.mrkrob.news.R
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsFeed
import com.mrkrob.news.ui.components.ArticleFeed
import com.mrkrob.news.ui.components.MessageView

@Composable
fun HomeScreen(
    onArticleClick: (Article) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    HomeScreen(
        query = viewModel.query,
        onQueryChange = viewModel::onQueryChange,
        feed = feed,
        articles = viewModel.articles.collectAsLazyPagingItems(),
        onArticleClick = onArticleClick,
        onShowSnackbar = onShowSnackbar,
    )
}

@Composable
internal fun HomeScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    feed: NewsFeed,
    articles: LazyPagingItems<Article>,
    onArticleClick: (Article) -> Unit,
    onShowSnackbar: suspend (message: String, actionLabel: String?) -> Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize()) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        val listState = rememberSaveable(feed, saver = LazyListState.Saver) { LazyListState() }
        ArticleFeed(
            articles = articles,
            listState = listState,
            onArticleClick = onArticleClick,
            onShowSnackbar = onShowSnackbar,
            header = { FeedTitle(feed) },
            emptyContent = {
                if (feed is NewsFeed.Search) {
                    MessageView(
                        illustration = R.drawable.illustration_search,
                        title = stringResource(R.string.empty_search_title),
                        message = stringResource(R.string.empty_search_message),
                    )
                } else {
                    MessageView(
                        illustration = R.drawable.illustration_news,
                        title = stringResource(R.string.empty_feed_title),
                        message = stringResource(R.string.empty_feed_message),
                    )
                }
            },
        )
    }
}

@Composable
private fun FeedTitle(feed: NewsFeed) {
    val title = when (feed) {
        is NewsFeed.Search -> stringResource(R.string.home_search_results, feed.query)
        else -> stringResource(R.string.home_top_headlines)
    }
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        maxLines = 2,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
    )
}
