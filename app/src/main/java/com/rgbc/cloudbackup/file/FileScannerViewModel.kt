import androidx.lifecycle.AndroidViewModel
import com.rgbc.cloudbackup.MainApplication
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import android.os.Environment
import android.util.Log


class FileScannerViewModel(application: Application): AndroidViewModel(application){
    private val TAG="FileScannerViewModel"
    private val db= AppDatabase.getDatabase(application)
    private val fileDao=db.fileIndexDao()
    private val cryptoManager=CryptoManager()

    val allFiles: LiveData<List<FileIndex>> = fileDao.getAllFiles().asLiveData()


    fun scanAndProcessFiles(){
        viewModelScope.launch(Dispatchers.IO){
            val context = getApplication<Application>().applicationContext
            val encryptedFilesDir = File(context.filesDir,"encrypted_storage").apply{
                mkdirs()
            }
            val sourceDir=
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if(!sourceDir.exists()) {
                Log.w("${TAG}.scanAndProcessFiles","Downloads directory not found")
                return@launch
            }
            Log.d("${TAG}.scanAndProcessFiles","Starting scan of ${sourceDir.absolutePath}...")
           for(file in sourceDir.listFiles() ?: emptyArray()) {
                if(file.isFile){
                    val checksum=ChecksumUtils.createChecksum(file)
                    if(checksum.isBlank()) {
                        Log.w("${TAG}.scanAndProcessFiles","Could not generate checksum fro file: ${file.name}")
                        continue
                    }

                    if(!fileDao.checksumExists(checksum)) {
                        Log.d("${TAG}.scanAndProcessFiles", "Found new file: ${file.name}. Processing...")

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
                        Log.d("${TAG}.scanAndProcessFiles","Successfully indexed file: ${file.name}")
                    }
                    else{
                        Log.d("${TAG}.scanAndProcessFiles","Duplicate file skipped: ${file.name}")

                    }
                }
            }
        }
        Log.d("${TAG}.scanAndProcessFiles","Scan complete")
    }
}