package com.example.ai_submission.ui.detail_story

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.ai_submission.R
import com.example.ai_submission.databinding.ActivityDetailStoryBinding
import com.example.ai_submission.utils.Utils
import com.example.ai_submission.utils.ViewModelFactory

class DetailStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailStoryBinding
    private lateinit var viewModel: DetailStoryViewModel

    private lateinit var loader: AlertDialog

    private var id: String? = null

    companion object {
        const val EXTRA_ID = "id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ViewModelFactory(application))[DetailStoryViewModel::class.java]

        loader = Utils.generateLoader(this)

        id = intent.getStringExtra(EXTRA_ID)
        if(id == null) {
            finish()
            return
        }

        setupVMBinding()
        viewModel.getData(id!!)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    private fun setupVMBinding() {
        viewModel.data.observe(this) {
            binding.tvDetailName.text = it.name
            binding.tvDetailDescription.text = it.description
            binding.tvDetailCreatedAt.text = getString(R.string.created_at, it.createdAt)

            supportActionBar?.title = it.name

            Glide.with(this)
                .load(it.photoUrl)
                .into(binding.ivDetailPhoto)
        }

        viewModel.isLoading.observe(this) {
            if (it) {
                loader.show()
            } else {
                loader.dismiss()
            }
        }

        viewModel.message.observe(this) {
            it.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}