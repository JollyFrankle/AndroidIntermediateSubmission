package com.example.ai_submission.ui.login

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_submission.retrofit.ApiConfig
import com.example.ai_submission.retrofit.LoginResponse
import com.example.ai_submission.utils.Event
import com.example.ai_submission.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val application: Application): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isLogin = MutableLiveData<Boolean>()
    val isLogin: LiveData<Boolean> = _isLogin

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    suspend fun isTokenAvailable(): Boolean {
        val token = Utils.getToken(application)
        return token.first().isNotEmpty()
    }

    fun checkLogin(email: String, password: String) {
        _isLoading.value = true

        val client = ApiConfig.getApiService()
        client.login(email, password).enqueue(object: Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        if (!body.error) {
                            _isLogin.value = true

                            // Set token
                            viewModelScope.launch {
                                Utils.setToken(application, "Bearer ${body.loginResult.token}")
                            }
                        } else {
                            _isLogin.value = false
                            _message.value = Event(body.message)
                        }
                    }
                } else {
                    _isLogin.value = false
                    val body = response.errorBody()
                    Log.w("TAG", "onResponse: ${body}")
                    try {
                        val error = JSONObject(body!!.string())
                        _message.value = Event(error.getString("message"))
                    } catch (e: Exception) {
                        _message.value = Event(e.message.toString())
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _isLoading.value = false
                _isLogin.value = false
                _message.value = Event(t.message.toString())
            }
        })
    }
}