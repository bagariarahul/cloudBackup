package com.rgbc.cloudbackup

import android.app.Application
import android.util.Log
import androidx.work.*
import com.rgbc.cloudbackup.worker.FileScannerWorker
import java.util.concurrent.TimeUnit

class MyCloudApp : Application() {
    private val TAG="MyCloudApp"
    override fun onCreate() {
        super.onCreate()
        Log.d("MyCloudApp", "Application onCreate")
        setupPeriodicWorker()
    }

    private fun setupPeriodicWorker() {
        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val scanRequest = PeriodicWorkRequestBuilder<FileScannerWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FileScannerWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            scanRequest
        )
    }
}