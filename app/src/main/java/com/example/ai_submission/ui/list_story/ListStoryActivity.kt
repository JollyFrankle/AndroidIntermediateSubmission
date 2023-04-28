package com.example.ai_submission.ui.list_story

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ai_submission.R
import com.example.ai_submission.data.LoadingStateAdapter
import com.example.ai_submission.databinding.ActivityListStoryBinding
import com.example.ai_submission.ui.add_story.AddStoryActivity
import com.example.ai_submission.ui.login.MainActivity
import com.example.ai_submission.ui.maps.MapsActivity
import com.example.ai_submission.utils.ListStoryAdapter
import com.example.ai_submission.utils.Utils
import com.example.ai_submission.utils.ViewModelFactory
import kotlinx.coroutines.launch

class ListStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListStoryBinding
    private lateinit var viewModel: ListStoryViewModel

    private lateinit var loader: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ListStoryVMSpecialFactory(application))[ListStoryViewModel::class.java]

        loader = Utils.generateLoader(this)

        setupVMBinding()
        setupRecyclerView()
    }

//    override fun onStart() {
//        super.onStart()
//
//        // Refresh data
//        viewModel.getStories()
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.om_add_new_btn -> {
                val intent = Intent(this, AddStoryActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                lifecycleScope.launch {
                    Utils.setToken(this@ListStoryActivity, "")
                    val intent = Intent(this@ListStoryActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                true
            }
            R.id.om_change_language_btn -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
                true
            }
            R.id.om_goto_maps_btn -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupVMBinding() {
        viewModel.message.observe(this) {
            it.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        val adapter = ListStoryAdapter()
        binding.rvListStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        viewModel.results.observe(this) {
            adapter.submitData(lifecycle, it)
            Toast.makeText(this, "Data updated", Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(this) {
            if (it) {
                loader.show()
            } else {
                loader.dismiss()
            }
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        binding.rvListStory.apply {
            this.layoutManager = layoutManager
        }
    }
}