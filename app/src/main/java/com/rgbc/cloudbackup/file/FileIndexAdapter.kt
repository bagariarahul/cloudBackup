import android.text.format.Formatter.formatFileSize
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rgbc.cloudbackup.R
import java.text.CharacterIterator
import java.text.StringCharacterIterator

class FileIndexAdapter : ListAdapter<FileIndex, FileIndexAdapter.FileViewHolder>
    (FileDiffCallback()) {
    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.file_name)
        private val detailsTextView: TextView = itemView.findViewById(R.id.file_details)
        private val statusIcon: ImageView = itemView.findViewById(R.id.backup_status_icon)

        fun bind(file: FileIndex) {
            //val decryptedName= CryptoManager().decryptFileNames(file.fileName)
            nameTextView.text = file.fileName
            detailsTextView.text = humanReadableFileSize(file.fileSize)
            if (file.isBackedUp) {
                //statusIcon.setImageResource(R.drawable.ic_cloud_done)
            } else {
                //statusIcon.setImageResource(R.drawable.ic_cloud_upload

            }

        }

        private fun humanReadableFileSize(bytes: Long): String
        {
            if(bytes in -999..999){
                return "$bytes B"
            }
            val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
            var tempBytes=bytes
            while (tempBytes <= -999950 || tempBytes >= 999950) {
                tempBytes /= 1000
                ci.next()
            }
            return String.format("%.1f %cB", tempBytes / 1000.0, ci.current())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = getItem(position)
        holder.bind(file)

    }


    class FileDiffCallback : DiffUtil.ItemCallback<FileIndex>() {
        override fun areItemsTheSame(oldItem: FileIndex, newItem: FileIndex): Boolean {
            return oldItem.fileIndexId == newItem.fileIndexId
        }

        override fun areContentsTheSame(oldItem: FileIndex, newItem: FileIndex): Boolean {
            return oldItem == newItem
        }
    }

    private fun formatFileSize(bytes: Long): String {
        var  size=bytes
        if(-1000 <size && size <1000){
            return "$size B"
        }
        val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
        while (size <= -999950 || size >= 999950) {
            size /= 1000
            ci.next()
        }
        return String.format("%.1f %cB", size / 1000.0, ci.current())

    }

}