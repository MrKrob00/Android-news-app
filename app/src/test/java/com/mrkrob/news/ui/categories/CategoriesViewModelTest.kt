package com.mrkrob.news.ui.categories

import androidx.lifecycle.SavedStateHandle
import com.mrkrob.news.domain.model.NewsCategory
import com.mrkrob.news.domain.model.NewsFeed
import com.mrkrob.news.testing.FakeNewsRepository
import com.mrkrob.news.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNewsRepository()

    @Test
    fun `loads the articles of the selected category`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = CategoriesViewModel(repository, SavedStateHandle())
        backgroundScope.launch { viewModel.articles.collect {} }
        runCurrent()

        viewModel.onCategorySelect(NewsCategory.SPORTS)
        runCurrent()

        assertEquals(NewsCategory.SPORTS, viewModel.selectedCategory.value)
        assertEquals(
            listOf(NewsFeed.Category(NewsCategory.BUSINESS), NewsFeed.Category(NewsCategory.SPORTS)),
            repository.requestedFeeds,
        )
    }
}
