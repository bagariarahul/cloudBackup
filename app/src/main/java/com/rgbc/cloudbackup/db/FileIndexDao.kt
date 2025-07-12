import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FileIndexDao{
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fileIndex: FileIndex): Long

    @Update
    suspend fun update(fileIndex: FileIndex)

    @Query("Select * from rgb_file_index where rgb_file_index_id = :id")
    suspend fun getFileById(id: Long): FileIndex?

    @Query("Select * from rgb_file_index where shouldBackup = 1 And isBackedUp=0")
    suspend fun getFilesToBackup(): Flow<List<FileIndex>>

    @Query("Select EXISTS(SELECT 1 FROM rgb_file_index WHERE file_checksum = :checksum LIMIT 1)")
    suspend fun checksumExists(checksum: String): Boolean

    @Query("Select * from rgb_file_index ORDER BY create_date desc" )
    fun getAllFiles(): Flow<List<FileIndex>>
}