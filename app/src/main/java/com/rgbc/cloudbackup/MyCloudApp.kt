import android.app.Application
import androidx.work.*
import com.rgbc.cloudbackup.worker.FileScannerWorker
import java.util.concurrent.TimeUnit

class MyCloudApp : Application() {
    override fun onCreate() {
        super.onCreate()
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