package com.example.ai_submission.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.ai_submission.R
import com.example.ai_submission.databinding.ActivityMainBinding
import com.example.ai_submission.ui.list_story.ListStoryActivity
import com.example.ai_submission.ui.register.RegisterActivity
import com.example.ai_submission.utils.Utils
import com.example.ai_submission.utils.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    private lateinit var loader: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // First: check if user id already logged in
        viewModel = ViewModelProvider(this, ViewModelFactory(application))[MainViewModel::class.java]
        CoroutineScope(Dispatchers.Unconfined).launch {
            if(viewModel.isTokenAvailable()) {
                val intent = Intent(this@MainActivity, ListStoryActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loader = Utils.generateLoader(this)

        setupVMBinding()
        setupAnimation()

        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if(email.isBlank()) {
                binding.edLoginEmail.error = getString(R.string.tmp_invalid_empty, "email")
                return@setOnClickListener
            }

            if(password.isBlank()) {
                binding.edLoginPassword.error = getString(R.string.tmp_invalid_empty, "password")
                return@setOnClickListener
            }

            if(binding.edLoginEmail.isError || binding.edLoginPassword.isError) {
                return@setOnClickListener
            }

            viewModel.checkLogin(email, password)
        }

        binding.btnMoveToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun setupVMBinding() {
        viewModel.isLogin.observe(this) {
            if (it) {
                val intent = Intent(this, ListStoryActivity::class.java)
                loader.dismiss()
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.tmp_failed, getString(R.string.tmps_login)), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.message.observe(this) {
            it.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) {
            if(it) {
                loader.show()
            } else {
                loader.dismiss()
            }
        }
    }

    private fun setupAnimation() {
        val tilEmail = ObjectAnimator.ofFloat(binding.tilLoginEmail, View.ALPHA, 1f).setDuration(300)
        val tilPassword = ObjectAnimator.ofFloat(binding.tilLoginPassword, View.ALPHA, 1f).setDuration(300)
        val btnLogin = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(300)
        val btnMoveToRegister = ObjectAnimator.ofFloat(binding.btnMoveToRegister, View.ALPHA, 1f).setDuration(300)

        val together = AnimatorSet().apply {
            playTogether(btnLogin, btnMoveToRegister)
        }

        AnimatorSet().apply {
            startDelay = 300
            playSequentially(tilEmail, tilPassword, together)
            start()
        }
    }
}