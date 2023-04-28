package com.example.ai_submission.ui.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.ai_submission.R
import com.example.ai_submission.databinding.ActivityRegisterBinding
import com.example.ai_submission.utils.Utils
import com.example.ai_submission.utils.ViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var viewModel: RegisterViewModel

    private lateinit var loader: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ViewModelFactory(application))[RegisterViewModel::class.java]

        loader = Utils.generateLoader(this)

        setupVMBinding()
        setupAnimation()

        binding.btnRegister.setOnClickListener {
            val name = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()

            if(name.isBlank()) {
                binding.edRegisterName.error = getString(R.string.tmp_invalid_empty, "name")
                return@setOnClickListener
            }

            if(email.isBlank()) {
                binding.edRegisterEmail.error = getString(R.string.tmp_invalid_empty, "email")
                return@setOnClickListener
            }

            if(password.isBlank()) {
                binding.edRegisterPassword.error = getString(R.string.tmp_invalid_empty, "password")
                return@setOnClickListener
            }

            if(binding.edRegisterEmail.isError || binding.edRegisterPassword.isError) {
                return@setOnClickListener
            }

            viewModel.register(name, email, password)
        }

        binding.btnMoveToLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupVMBinding() {
        viewModel.isRegister.observe(this) {
            if (it) {
                Toast.makeText(this, getString(R.string.tmp_success, getString(R.string.tmps_register)), Toast.LENGTH_SHORT).show()
                loader.dismiss()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.tmp_failed, getString(R.string.tmps_register)), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.message.observe(this) {
            it.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this) {
            if (it) {
                loader.show()
            } else {
                loader.dismiss()
            }
        }
    }

    private fun setupAnimation() {
        val tilEmail = ObjectAnimator.ofFloat(binding.tilRegisterEmail, View.ALPHA, 1f).setDuration(300)
        val tilName = ObjectAnimator.ofFloat(binding.tilRegisterName, View.ALPHA, 1f).setDuration(300)
        val tilPassword = ObjectAnimator.ofFloat(binding.tilRegisterPassword, View.ALPHA, 1f).setDuration(300)
        val btnRegister = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(300)
        val btnMoveToLogin = ObjectAnimator.ofFloat(binding.btnMoveToLogin, View.ALPHA, 1f).setDuration(300)

        val together = AnimatorSet().apply {
            playTogether(btnRegister, btnMoveToLogin)
        }

        AnimatorSet().apply {
            startDelay = 300
            playSequentially(tilName, tilEmail, tilPassword, together)
            start()
        }
    }
}