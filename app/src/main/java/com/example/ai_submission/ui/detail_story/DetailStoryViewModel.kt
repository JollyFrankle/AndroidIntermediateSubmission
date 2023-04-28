package com.example.ai_submission.ui.detail_story

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_submission.retrofit.ApiConfig
import com.example.ai_submission.retrofit.DetailStoryResponse
import com.example.ai_submission.retrofit.Story
import com.example.ai_submission.utils.Event
import com.example.ai_submission.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailStoryViewModel(private val application: Application): ViewModel() {
    private val _data = MutableLiveData<Story>()
    val data: LiveData<Story> = _data

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    fun getData(id: String) {
        _isLoading.value = true

        viewModelScope.launch {
            val token = Utils.getToken(application).first()

            val client = ApiConfig.getApiService()
            Log.w("TAG", "getData: $token, $id")
            client.getStory(
                token,
                id
            ).enqueue(object: Callback<DetailStoryResponse> {
                override fun onResponse(call: Call<DetailStoryResponse>, response: Response<DetailStoryResponse>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            _data.value = body.story
                            Log.w("TAG", "onResponse: ${body}")
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

                override fun onFailure(call: Call<DetailStoryResponse>, t: Throwable) {
                    _isLoading.value = false
                    _message.value = Event(t.message.toString())
                }
            })
        }
    }
}