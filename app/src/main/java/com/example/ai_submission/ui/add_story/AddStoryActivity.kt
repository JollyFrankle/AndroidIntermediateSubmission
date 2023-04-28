package com.example.ai_submission.ui.add_story

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ai_submission.R
import com.example.ai_submission.databinding.ActivityAddStoryBinding
import com.example.ai_submission.ui.camera.CameraActivity
import com.example.ai_submission.utils.Utils
import com.example.ai_submission.utils.ViewModelFactory
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var viewModel: AddStoryViewModel

    private lateinit var loader: AlertDialog

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, getString(R.string.no_perms), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private val launcherCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getSerializableExtra("picture")
            } as? File
            val isBackCamera = result.data?.getBooleanExtra("isBackCamera", true) ?: true

            file.let { f ->
                Utils.rotateFile(f!!, isBackCamera)
                val bitmap = BitmapFactory.decodeFile(f.path)
                binding.previewImage.setImageBitmap(bitmap)

                viewModel.setFile(f)
            }
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if(result.resultCode == RESULT_OK) {
            val selectedImg = result.data?.data as Uri
            selectedImg.let {
                val myFile = Utils.uriToFile(it, this@AddStoryActivity)
                binding.previewImage.setImageURI(it)

                viewModel.setFile(myFile)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, ViewModelFactory(application))[AddStoryViewModel::class.java]

        loader = Utils.generateLoader(this)

        if(!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        setupVMBinding()

        binding.btnCamera.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            launcherCameraX.launch(intent)
        }

        binding.btnRotate.setOnClickListener {
            if(viewModel.currentFile.value != null) {
                Utils.rotateFile(viewModel.currentFile.value!!, true) // rotate 90deg
                val bitmap = BitmapFactory.decodeFile(viewModel.currentFile.value!!.path)
                binding.previewImage.setImageBitmap(bitmap)
            }
        }

        binding.btnGallery.setOnClickListener {
            val intent = Intent()
            intent.apply {
                action = Intent.ACTION_GET_CONTENT
                type = "image/*"
            }
            val chooser = Intent.createChooser(intent, getString(R.string.select_from_gallery))
            launcherGallery.launch(chooser)
        }

        binding.buttonAdd.setOnClickListener {
            if(viewModel.currentFile.value == null) {
                Toast.makeText(this, getString(R.string.tmp_invalid_empty, "Image (gambar)"), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val description = binding.edAddDescription.text.toString()
            if(description.isBlank()) {
                Toast.makeText(this, getString(R.string.tmp_invalid_empty, "Description (deskripsi)"), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.uploadStory(description)
        }
    }

    private fun setupVMBinding() {
        viewModel.isLoading.observe(this) {
            if (it) {
                loader.show()
            } else {
                loader.dismiss()
            }
        }

        viewModel.isSuccessful.observe(this) {
            if (it) {
                Toast.makeText(this, getString(R.string.tmp_success, getString(R.string.tmps_add_story)), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, getString(R.string.tmp_failed, getString(R.string.tmps_add_story)), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.currentFile.observe(this) {
            if(it != null) {
                binding.btnRotate.apply {
                    isEnabled = true
                    alpha = 1f
                }
            } else {
                binding.btnRotate.apply {
                    isEnabled = false
                    alpha = 0.5f
                }
            }
        }

        viewModel.message.observe(this) {
            it.getContentIfNotHandled()?.let { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}