package com.rgbc.cloudbackup.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rgbc.cloudbackup.file.FileUtils

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            if (!hasStoragePermission()) {
                Log.e("BackupWorker", "Permission not granted in worker context")
                return Result.failure()
            }

            Log.d("BackupWorker", "Starting media scan")
            val files = FileUtils.getMediaFiles(applicationContext)
            Log.d("BackupWorker", "Found ${files.size} media files")

            files.take(3).forEachIndexed { i, file ->
                Log.d("BackupWorker", "File $i: ${file.name}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("BackupWorker", "Backup failed", e)
            Result.retry()
        }
    }

    private fun hasStoragePermission(): Boolean {
        // Use applicationContext consistently
        val ctx = applicationContext

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}