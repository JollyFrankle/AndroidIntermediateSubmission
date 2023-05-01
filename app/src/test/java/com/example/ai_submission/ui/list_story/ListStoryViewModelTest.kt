package com.example.ai_submission.ui.list_story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.ai_submission.DummyData
import com.example.ai_submission.MainDispatcherRule
import com.example.ai_submission.data.StoryRepository
import com.example.ai_submission.data.retrofit.Story
import com.example.ai_submission.getOrAwaitValue
import com.example.ai_submission.utils.ListStoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ListStoryViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Test
    fun `Berhasil memuat data cerita - 3 tests`() = runTest {
        val dummyStory = DummyData.generateDummyStoryResponse()
        val data = StoryPagingSource.snapshot(dummyStory)

        val expectedStory = MutableLiveData<PagingData<Story>>()
        expectedStory.value = data

        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStory)

        val vm = ListStoryViewModel(storyRepository)
        val actualStory = vm.results.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        // Testing
        assertNotNull(actualStory) // <-- Memastikan data tidak null
        assertEquals(dummyStory.size, differ.snapshot().size) // <-- Memastikan jumlah data sesuai dengan yang diharapkan
        assertEquals(dummyStory[0], differ.snapshot()[0]!!) // <-- Memastikan data pertama yang dikembalikan sesuai
    }

    @Test
    fun `Ketika tidak ada data cerita - 1 test`() = runTest {
        val data = PagingData.from(emptyList<Story>())
        val expectedStory = MutableLiveData<PagingData<Story>>()
        expectedStory.value = data
        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStory)

        val vm = ListStoryViewModel(storyRepository)
        val actualStory = vm.results.getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        // Testing
        assertEquals(0, differ.snapshot().size) // <-- Memastikan jumlah data yang dikembalikan nol.
    }
}

class StoryPagingSource: PagingSource<Int, LiveData<List<Story>>>() {

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<Story>>>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<Story>>> {
        return LoadResult.Page(
            data = emptyList(),
            prevKey = null,
            nextKey = null
        )
    }

    companion object {
        fun snapshot(story: List<Story>): PagingData<Story> {
            return PagingData.from(story)
        }
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}