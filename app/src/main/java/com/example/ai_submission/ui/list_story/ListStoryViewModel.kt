package com.example.ai_submission.ui.list_story

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.ai_submission.data.StoryRepository
import com.example.ai_submission.data.retrofit.ApiConfig
import com.example.ai_submission.data.retrofit.Story
import com.example.ai_submission.data.room.StoryDatabase
import com.example.ai_submission.utils.Event

class ListStoryViewModel(private val repo: StoryRepository): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val results: LiveData<PagingData<Story>> = repo.getStories().cachedIn(viewModelScope)

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message
}

class ListStoryVMSpecialFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListStoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListStoryViewModel(
                StoryRepository(
                    application,
                    StoryDatabase.getDatabase(application),
                    ApiConfig.getApiService()
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}