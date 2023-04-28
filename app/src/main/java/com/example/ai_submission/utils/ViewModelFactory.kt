package com.example.ai_submission.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ai_submission.ui.add_story.AddStoryViewModel
import com.example.ai_submission.ui.detail_story.DetailStoryViewModel
import com.example.ai_submission.ui.list_story.ListStoryViewModel
import com.example.ai_submission.ui.login.MainViewModel
import com.example.ai_submission.ui.maps.MapsViewModel
import com.example.ai_submission.ui.register.RegisterViewModel

class ViewModelFactory(private val application: Application): ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
//            modelClass.isAssignableFrom(ListStoryViewModel::class.java) -> ListStoryViewModel(application) as T
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(application) as T
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> RegisterViewModel(application) as T
            modelClass.isAssignableFrom(DetailStoryViewModel::class.java) -> DetailStoryViewModel(application) as T
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> AddStoryViewModel(application) as T
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> MapsViewModel(application) as T
            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }
    }
}