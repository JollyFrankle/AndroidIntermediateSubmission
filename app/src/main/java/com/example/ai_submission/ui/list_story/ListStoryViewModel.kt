package com.example.ai_submission.ui.list_story

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.ai_submission.data.StoryRepository
import com.example.ai_submission.data.retrofit.AllStoriesResponse
import com.example.ai_submission.data.retrofit.ApiConfig
import com.example.ai_submission.data.retrofit.Story
import com.example.ai_submission.data.retrofit.StoryPagingSource
import com.example.ai_submission.data.room.StoryDatabase
import com.example.ai_submission.utils.Event
import com.example.ai_submission.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListStoryViewModel(private val repo: StoryRepository): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

//    private val _results = MutableLiveData<List<Story>>()
    val results: LiveData<PagingData<Story>> = repo.getStories().cachedIn(viewModelScope)

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

//    init {
//        getStories()
//    }
//
//    fun getStories() {
//        _isLoading.value = true
//
//        viewModelScope.launch {
//            val token = Utils.getToken(application).first()
//            Log.e("TOKEN", token)
//
//            val client = ApiConfig.getApiService()
//            client.getStories(
//                token
//            ).enqueue(object: Callback<AllStoriesResponse> {
//                override fun onResponse(call: Call<AllStoriesResponse>, response: Response<AllStoriesResponse>) {
//                    _isLoading.value = false
//                    if (response.isSuccessful) {
//                        val body = response.body()
//                        if (body != null) {
//                            _results.value = body.listStory
//                        }
//                    } else {
//                        val body = response.errorBody()
//                        try {
//                            val error = JSONObject(body!!.string())
//                            _message.value = Event(error.getString("message"))
//                        } catch (e: Exception) {
//                            _message.value = Event(e.message.toString())
//                        }
//                    }
//                }
//
//                override fun onFailure(call: Call<AllStoriesResponse>, t: Throwable) {
//                    _isLoading.value = false
//                    _message.value = Event(t.message.toString())
//                }
//            })
//        }
//    }
}

class ListStoryVMSpecialFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListStoryViewModel::class.java)) {
//            StoryPagingSource(application, ApiConfig.getApiService())

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