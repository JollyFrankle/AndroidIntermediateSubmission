package com.example.ai_submission.data

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.ai_submission.data.retrofit.ApiService
import com.example.ai_submission.data.retrofit.Story
import com.example.ai_submission.data.retrofit.StoryPagingSource
import com.example.ai_submission.data.room.StoryDatabase

class StoryRepository(
    private val application: Application,
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
) {
    fun getStories(): LiveData<PagingData<Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(application, storyDatabase, apiService),
            pagingSourceFactory = {
//                StoryPagingSource(application, apiService)
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }
}