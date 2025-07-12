import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "rgb_file_index")
data class FileIndex(
    @ColumnInfo(name="rgb_file_index_id")
    @PrimaryKey(autoGenerate = true)
    val fileIndexId:Long =0,

    @ColumnInfo(name="file_name")
    val fileName: String,

    @ColumnInfo(name="file_path")
    val filePath: String,

    @ColumnInfo(name="file_size")
    val fileSize: Long,

    @ColumnInfo(name="create_date")
    val createDate: Long,

    @ColumnInfo(name="file_checksum")
    val checksum : String,

    var shouldBackup: Boolean = true,
    var isBackedUp: Boolean = false,








    ){
    @Ignore
     val isSeletedForAction: Boolean = false
}