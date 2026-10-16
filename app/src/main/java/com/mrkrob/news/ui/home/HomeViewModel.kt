package com.mrkrob.news.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mrkrob.news.domain.NewsRepository
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsFeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: NewsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var query by mutableStateOf(savedStateHandle.get<String>(QUERY_KEY).orEmpty())
        private set

    val feed: StateFlow<NewsFeed> = snapshotFlow { query.trim() }
        .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MILLIS }
        .map(::feedFor)
        .stateIn(viewModelScope, SharingStarted.Eagerly, feedFor(query.trim()))

    val articles: Flow<PagingData<Article>> = feed
        .flatMapLatest(repository::articles)
        .cachedIn(viewModelScope)

    fun onQueryChange(query: String) {
        this.query = query
        savedStateHandle[QUERY_KEY] = query
    }

    private fun feedFor(query: String): NewsFeed =
        if (query.isEmpty()) NewsFeed.TopHeadlines else NewsFeed.Search(query)

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 500L
        private const val QUERY_KEY = "query"
    }
}
