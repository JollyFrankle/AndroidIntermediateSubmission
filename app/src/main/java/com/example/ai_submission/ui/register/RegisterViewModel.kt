package com.example.ai_submission.ui.register

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ai_submission.retrofit.ApiConfig
import com.example.ai_submission.retrofit.GeneralResponse
import com.example.ai_submission.utils.Event
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel(private val application: Application): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRegister = MutableLiveData<Boolean>()
    val isRegister: LiveData<Boolean> = _isRegister

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    fun register(name: String, email: String, password: String) {
        _isLoading.value = true

        val client = ApiConfig.getApiService()
        client.register(
            name, email, password
        ).enqueue(object : Callback<GeneralResponse> {
            override fun onResponse(
                call: Call<GeneralResponse>,
                response: Response<GeneralResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (!body.error) {
                            _isRegister.value = true
                        } else {
                            _isRegister.value = false
                            _message.value = Event(body.message)
                        }
                    }
                } else {
                    _isRegister.value = false
                    val body = response.errorBody()
                    try {
                        val error = JSONObject(body!!.string())
                        _message.value = Event(error.getString("message"))
                    } catch (e: Exception) {
                        _message.value = Event(e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<GeneralResponse>, t: Throwable) {
                _isLoading.value = false
                _isRegister.value = false
                _message.value = Event(t.message.toString())
            }
        })
    }
}
