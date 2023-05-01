package com.example.ai_submission.ui.add_story

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_submission.data.retrofit.ApiConfig
import com.example.ai_submission.data.retrofit.GeneralResponse
import com.example.ai_submission.utils.Event
import com.example.ai_submission.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Callback
import java.io.File

class AddStoryViewModel(private val application: Application): ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _currentFile = MutableLiveData<File>()
    val currentFile: LiveData<File> = _currentFile

    private val _isSuccessful = MutableLiveData<Boolean>()
    val isSuccessful: LiveData<Boolean> = _isSuccessful

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    val latLngCoord = MutableLiveData<Pair<Double, Double>?>()

    fun setFile(file: File) {
        _currentFile.value = file
    }

    fun uploadStory(description: String) {
        if(_currentFile.value == null) {
            return
        }

        _isLoading.value = true
        val file = Utils.reduceFileImage(_currentFile.value!!)

        // Prepare file part
        val desc = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
        val imageMultipart = MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

        val lat = latLngCoord.value?.first?.toString()?.toRequestBody("text/plain".toMediaType())
        val lng = latLngCoord.value?.second?.toString()?.toRequestBody("text/plain".toMediaType())

        // Send request
        viewModelScope.launch {
            val token = Utils.getToken(application).first()

            val client = ApiConfig.getApiService()
            client.uploadStory(
                token,
                imageMultipart,
                desc,
                lat,
                lng
            ).enqueue(object: Callback<GeneralResponse> {
                override fun onResponse(call: retrofit2.Call<GeneralResponse>, response: retrofit2.Response<GeneralResponse>) {
                    _isLoading.value = false
                    if(response.isSuccessful) {
                        // Jika response code 2xx
                        val responseBody = response.body()
                        if (responseBody != null && !responseBody.error) {
                            _isSuccessful.value = true
                        }
                    } else {
                        // Jika response code tidak 2xx
                        val body = response.errorBody()
                        try {
                            val error = JSONObject(body!!.string())
                            _message.value = Event(error.getString("message"))
                        } catch (e: Exception) {
                            _message.value = Event(e.message.toString())
                        }
                    }
                }

                override fun onFailure(call: retrofit2.Call<GeneralResponse>, t: Throwable) {
                    // Jika gagal request (tidak ada koneksi internet)
                    _isLoading.value = false
                    _message.value = Event(t.message.toString())
                }
            })
        }
    }
}