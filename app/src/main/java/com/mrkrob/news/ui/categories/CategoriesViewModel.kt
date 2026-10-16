package com.mrkrob.news.ui.categories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mrkrob.news.domain.NewsRepository
import com.mrkrob.news.domain.model.Article
import com.mrkrob.news.domain.model.NewsCategory
import com.mrkrob.news.domain.model.NewsFeed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    repository: NewsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val selectedCategory: StateFlow<NewsCategory> =
        savedStateHandle.getStateFlow(CATEGORY_KEY, NewsCategory.entries.first())

    val articles: Flow<PagingData<Article>> = selectedCategory
        .flatMapLatest { category -> repository.articles(NewsFeed.Category(category)) }
        .cachedIn(viewModelScope)

    fun onCategorySelect(category: NewsCategory) {
        savedStateHandle[CATEGORY_KEY] = category
    }

    private companion object {
        const val CATEGORY_KEY = "category"
    }
}
