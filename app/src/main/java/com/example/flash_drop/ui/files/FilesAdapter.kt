import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flash_drop.R
import com.example.flash_drop.data.devices.FileItem

class FilesAdapter(
    private val files: List<FileItem>
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFile: ImageView = itemView.findViewById(R.id.imgFileIcon)
        val tvName: TextView = itemView.findViewById(R.id.tvFileName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvFileDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = files[position]
        holder.imgFile.setImageResource(file.iconRes)
        holder.tvName.text = file.fileName
        holder.tvDesc.text = file.description
    }

    override fun getItemCount() = files.size
}
