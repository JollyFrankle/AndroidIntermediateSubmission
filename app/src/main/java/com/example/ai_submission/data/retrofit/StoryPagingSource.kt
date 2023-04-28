package com.example.ai_submission.data.retrofit

import android.app.Application
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.ai_submission.utils.Utils
import kotlinx.coroutines.flow.first

class StoryPagingSource(private val application: Application, private val apiService: ApiService): PagingSource<Int, Story>() {

    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {
        Log.w("StoryPagingSource", "getRefreshKey: called")
        return state.anchorPosition?.let { ap ->
            val anchorPage = state.closestPageToPosition(ap)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        Log.w("StoryPagingSource", "load: called")
        try {
            val token = Utils.getToken(application).first()
            println("Here 0 $token")

            val position = params.key ?: 1
            println("Here 1")
            val responseData = apiService.getStoriesWithPagination( // <-- APISERVICE ERROR
                token,
                position,
                params.loadSize
            )
            println("Here 2")

            return LoadResult.Page(
                data = responseData.listStory,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (responseData.listStory.isEmpty()) null else position + 1
            )

//            return LoadResult.Page(
//                data = listOf(),
//                prevKey = null,
//                nextKey = null
//            )
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }

}