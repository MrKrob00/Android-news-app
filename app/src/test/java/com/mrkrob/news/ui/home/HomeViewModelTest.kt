package com.mrkrob.news.ui.home

import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.SavedStateHandle
import com.mrkrob.news.domain.model.NewsFeed
import com.mrkrob.news.testing.FakeNewsRepository
import com.mrkrob.news.testing.MainDispatcherRule
import com.mrkrob.news.ui.home.HomeViewModel.Companion.SEARCH_DEBOUNCE_MILLIS
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeNewsRepository()

    @Test
    fun `shows top headlines without a query`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = HomeViewModel(repository, SavedStateHandle())
        runCurrent()

        assertEquals(NewsFeed.TopHeadlines, viewModel.feed.value)
    }

    @Test
    fun `searches once typing settles`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = HomeViewModel(repository, SavedStateHandle())
        runCurrent()

        viewModel.type("ko")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS / 2)
        viewModel.type("kotlin ")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
        assertEquals(NewsFeed.TopHeadlines, viewModel.feed.value)

        advanceTimeBy(2)
        assertEquals(NewsFeed.Search("kotlin"), viewModel.feed.value)
    }

    @Test
    fun `clearing the query shows top headlines right away`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = HomeViewModel(repository, SavedStateHandle(mapOf("query" to "kotlin")))
        runCurrent()

        viewModel.type("")
        runCurrent()

        assertEquals(NewsFeed.TopHeadlines, viewModel.feed.value)
    }

    @Test
    fun `restores the query`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = HomeViewModel(repository, SavedStateHandle(mapOf("query" to "kotlin")))
        runCurrent()

        assertEquals("kotlin", viewModel.query)
        assertEquals(NewsFeed.Search("kotlin"), viewModel.feed.value)
    }

    @Test
    fun `loads the articles of the current feed`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = HomeViewModel(repository, SavedStateHandle())
        backgroundScope.launch { viewModel.articles.collect {} }
        runCurrent()

        viewModel.type("kotlin")
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + 1)

        assertEquals(listOf(NewsFeed.TopHeadlines, NewsFeed.Search("kotlin")), repository.requestedFeeds)
    }

    private fun HomeViewModel.type(text: String) {
        onQueryChange(text)
        Snapshot.sendApplyNotifications()
    }
}
