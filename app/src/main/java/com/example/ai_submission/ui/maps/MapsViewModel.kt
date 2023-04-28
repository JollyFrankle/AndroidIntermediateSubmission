package com.example.ai_submission.ui.maps

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_submission.retrofit.AllStoriesResponse
import com.example.ai_submission.retrofit.ApiConfig
import com.example.ai_submission.retrofit.Story
import com.example.ai_submission.utils.Event
import com.example.ai_submission.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel(private val application: Application): ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _stories = MutableLiveData<List<Story>>()
    val stories: LiveData<List<Story>> = _stories

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    init {
        getStories()
    }

    fun getStories() {
        _isLoading.value = true

        viewModelScope.launch {
            val token = Utils.getToken(application).first()
            Log.e("TOKEN", token)

            val client = ApiConfig.getApiService()
            client.getStoriesAndLocation(
                token
            ).enqueue(object : Callback<AllStoriesResponse> {
                override fun onResponse(
                    call: Call<AllStoriesResponse>,
                    response: Response<AllStoriesResponse>
                ) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            _stories.value = body.listStory
                        }
                    } else {
                        val body = response.errorBody()
                        try {
                            val error = JSONObject(body!!.string())
                            _message.value = Event(error.getString("message"))
                        } catch (e: Exception) {
                            _message.value = Event(e.message.toString())
                        }
                    }
                }

                override fun onFailure(call: Call<AllStoriesResponse>, t: Throwable) {
                    _isLoading.value = false
                    _message.value = Event(t.message.toString())
                }
            })
        }
    }
}