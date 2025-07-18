package com.rgbc.cloudbackup



import com.rgbc.cloudbackup.file.FileIndexAdapter
import com.rgbc.cloudbackup.file.FileScannerViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.rgbc.cloudbackup.utils.BackupWorker
import com.rgbc.cloudbackup.worker.FileScannerWorker
import java.util.concurrent.TimeUnit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var recyclerView: RecyclerView
    private val viewModel: FileScannerViewModel by viewModels()
    private val fileAdapter = FileIndexAdapter()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){
            isGranted: Boolean ->
            if(isGranted){
                Log.i(TAG, "Permission Granted")
            }
            else{
                Log.i(TAG, "Permission Denied")
            }
        }

    private var permissionRequestCount = 0
    private lateinit var statusText: TextView

    // Single permission launcher for all cases
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if(isGranted) {
        }
        else{

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        recyclerView= findViewById<RecyclerView>(R.id.fileRecyclerView)

//        val layoutManager = LinearLayoutManager(this)
//        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = fileAdapter


        val fab: FloatingActionButton = findViewById(R.id.fab_start_scan)

        fab.setOnClickListener {
            Log.d(TAG, "FAB clicked")
            val scanRequest = OneTimeWorkRequest.from(FileScannerWorker::class.java)

            WorkManager.getInstance(this).enqueue(scanRequest)

        }

        viewModel.allFiles.observe(this){
            files ->
            files?.let{
                Log.d(TAG, "File list updated. Submitting ${it.size} files")
                fileAdapter.submitList(it)
            }
        }

        checkAndRequestPermissions()

        statusText = findViewById(R.id.statusText)
        val permissionButton = findViewById<Button>(R.id.permissionButton)
        val testButton = findViewById<Button>(R.id.testButton)

        permissionButton.setOnClickListener { requestStoragePermission() }
        testButton.setOnClickListener { testBackupImmediately() }

        // Delay permission request to ensure UI is ready
        Handler(Looper.getMainLooper()).postDelayed(::requestStoragePermission, 500)
    }

    private fun checkAndRequestPermissions() {
        when{
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {

            }
            else -> {
                requestPermissionsLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun requestStoragePermission() {
        // Check if we already have permissions
        if (hasRequiredPermissions()) {
            handlePermissionGranted()
            return
        }

        Log.d("PermissionFlow", "Requesting permission")
        statusText.text = "Requesting permission..."

        // Get required permissions based on Android version
//        val permissions = getRequiredPermissions()
//        requestPermissionsLauncher.launch(permissions)
    }

    private fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            handlePermissionGranted()
        } else {
            permissionRequestCount++
            Log.d("PermissionFlow", "Permission denied ($permissionRequestCount)")
            statusText.text = "Permission denied. Please grant access."

            if (permissionRequestCount >= 1) { // Show after first denial
                showPermissionRationale()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handlePermissionGranted() {
        Log.d("PermissionFlow", "Permission granted")
        statusText.text = "Permission granted. Backup scheduled!"
        Toast.makeText(this, "Backup scheduled!", Toast.LENGTH_SHORT).show()
        schedulePeriodicBackup()
        findViewById<Button>(R.id.testButton).isEnabled = true
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Storage Permission Needed")
            .setMessage("To backup your photos and videos, please grant storage access")
            .setPositiveButton("Grant Access") { _, _ -> requestStoragePermission() }
            .setNeutralButton("App Settings") { _, _ -> openAppSettings() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun schedulePeriodicBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
            .setRequiresCharging(true) // Only when charging
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            24, // Repeat interval
            TimeUnit.HOURS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "autoBackup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }

    private fun testBackupImmediately() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        val testRequest = OneTimeWorkRequestBuilder<BackupWorker>().build()
        WorkManager.getInstance(this).enqueue(testRequest)
        Toast.makeText(this, "Running test backup NOW", Toast.LENGTH_SHORT).show()
        Log.d("BackupTest", "Immediate backup triggered")
    }

}