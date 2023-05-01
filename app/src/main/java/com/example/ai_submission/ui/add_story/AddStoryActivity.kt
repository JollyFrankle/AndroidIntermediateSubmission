package com.example.ai_submission.ui.add_story

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.ai_submission.R
import com.example.ai_submission.databinding.ActivityAddStoryBinding
import com.example.ai_submission.ui.camera.CameraActivity
import com.example.ai_submission.utils.Utils
import com.example.ai_submission.utils.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import java.io.File

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var viewModel: AddStoryViewModel

    private lateinit var loader: AlertDialog

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var isTracking = false

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
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
        setupGPS()

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

        binding.swUseCurrentPosition.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                isTracking = true
                startLocationUpdates()
            } else {
                isTracking = false
                stopLocationUpdates()
                viewModel.latLngCoord.value = null
            }
        }
    }

    private fun setupGPS() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // https://tomas-repcik.medium.com/locationrequest-create-got-deprecated-how-to-fix-it-e4f814138764
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).apply {
            setMinUpdateDistanceMeters(10f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        client.checkLocationSettings(builder.build())
            .addOnSuccessListener {
//                getLastLocation()
                // Do nothing: kita belum mau mendapatkan lokasi user
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.tmp_failed, "GPS"), Toast.LENGTH_SHORT).show()
            }

        // Location Callback
        locationCallback = object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val location = p0.lastLocation
                if(location != null) {
                    // Update view model
                    viewModel.latLngCoord.value = Pair(location.latitude, location.longitude)
                }
            }
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

        viewModel.latLngCoord.observe(this) {
            if(it != null) {
                binding.tilLat.editText?.setText(it.first.toString())
                binding.tilLng.editText?.setText(it.second.toString())
            } else {
                binding.tilLat.editText?.setText("")
                binding.tilLng.editText?.setText("")
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if(allPermissionsGranted()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    // Update view model
                    viewModel.latLngCoord.value = Pair(location.latitude, location.longitude)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        getLastLocation()
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e("Location", "Error : " + exception.message)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if(isTracking) {
            startLocationUpdates()
        }
    }
}