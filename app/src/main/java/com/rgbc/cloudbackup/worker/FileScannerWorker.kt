package com.rgbc.cloudbackup.worker

import com.rgbc.cloudbackup.db.FileIndex
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rgbc.cloudbackup.db.AppDatabase
import com.rgbc.cloudbackup.utils.ChecksumUtils

class FileScannerWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext,workerParams) {

    private val TAG="FileScannerWorker"
    private val TAG_METHOD=".doWork()"


    override suspend fun doWork(): Result {

        val appContext=applicationContext
        val fileDao= AppDatabase.getDatabase(appContext).fileIndexDao()

        Log.d(TAG+TAG_METHOD,"Worker starting: Beginning file scan")
        try{
            val sourceDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if(!sourceDir.exists()){
                Log.w(TAG+TAG_METHOD,"Source directory ${Environment.DIRECTORY_DOWNLOADS} does not exist")
                return Result.success()
            }
            for(file in sourceDir.listFiles() ?: emptyArray()){
                var checksum = ""
                if(file.isFile){
                    checksum= ChecksumUtils.createChecksum(file)
                    if(checksum.isBlank()) {
                        Log.w(
                            TAG + TAG_METHOD,
                            "Checksum failed for file ${file.absolutePath}, skipping"
                        )
                        continue
                    }
                }
                if(!checksum.isEmpty() && !fileDao.checksumExists(checksum)){
                    Log.d(TAG+TAG_METHOD, "Found new file: ${file.name}. Processing...")

                    val fileIndex  = FileIndex(
                        fileName = file.name,
                        filePath = "TBD",
                        fileSize = file.length(),
                        createDate = System.currentTimeMillis(),
                        checksum = checksum,
                        isBackedUp = false,
                        shouldBackup = true
                    )
                    fileDao.insert(fileIndex)

                }
            }
            Log.d(TAG+TAG_METHOD,"Worker finished: File scan complete")
            return Result.success()
        }
        catch(e: Exception){
            Log.e(TAG+TAG_METHOD,"Worker failed: ${e.message}")
            return Result.failure()

        }

    }
}