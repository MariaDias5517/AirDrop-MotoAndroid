package com.example.flash_drop

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HistoricoAdapter(
    private var items: List<TransferHistory>,
    private val onItemClick: (TransferHistory) -> Unit,
    private val onSwipeDelete: (Int) -> Unit,
    private val onSwipeArchive: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    // Classes internas
    data class GroupedItem(
        val isHeader: Boolean,
        val headerDate: String? = null,
        val transfer: TransferHistory? = null
    )

    private val groupedItems = mutableListOf<GroupedItem>()

    init {
        updateGroupedItems()
    }

    private fun updateGroupedItems() {
        groupedItems.clear()

        // Agrupar por data
        val groupedByDate = items.groupBy {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it.date)
        }

        groupedByDate.forEach { (date, transfers) ->
            // Adicionar header
            groupedItems.add(GroupedItem(true, date))

            // Adicionar itens
            transfers.forEach { transfer ->
                groupedItems.add(GroupedItem(false, null, transfer))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (groupedItems[position].isHeader) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header_historico, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_historico_swipe, parent, false)
                ItemViewHolder(view, onItemClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                groupedItems[position].headerDate?.let { holder.bind(it) }
            }
            is ItemViewHolder -> {
                groupedItems[position].transfer?.let { holder.bind(it) }
            }
        }
    }

    override fun getItemCount(): Int = groupedItems.size

    fun updateItems(newItems: List<TransferHistory>) {
        items = newItems
        updateGroupedItems()
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        val item = groupedItems[position].transfer ?: return
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            val newItems = items.toMutableList().apply { removeAt(index) }
            updateItems(newItems)
        }
    }

    fun archiveItem(position: Int) {
        val item = groupedItems[position].transfer ?: return
        val newItems = items.toMutableList()
        val index = newItems.indexOfFirst { it.id == item.id }
        if (index != -1) {
            newItems[index] = item.copy(status = "Arquivado")
            updateItems(newItems)
        }
    }

    // Método para obter um item específico
    fun getItemAt(position: Int): TransferHistory? {
        return groupedItems.getOrNull(position)?.transfer
    }

    // ViewHolders
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtHeader: TextView = itemView.findViewById(R.id.txtHeader)

        fun bind(dateStr: String) {
            txtHeader.text = formatDateHeader(dateStr)
        }

        private fun formatDateHeader(dateStr: String): String {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val today = Calendar.getInstance().apply { time = Date() }
            val date = dateFormat.parse(dateStr) ?: Date()
            val targetDate = Calendar.getInstance().apply { time = date }

            return when {
                today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
                        today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR) -> "Hoje"

                today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
                        today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR) + 1 -> "Ontem"

                else -> {
                    val formatter = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR"))
                    formatter.format(date)
                }
            }
        }
    }

    class ItemViewHolder(
        itemView: View,
        private val onItemClick: (TransferHistory) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtFileName: TextView = itemView.findViewById(R.id.txtFileName)
        private val txtDevice: TextView = itemView.findViewById(R.id.txtDevice)
        private val txtTime: TextView = itemView.findViewById(R.id.txtTime)
        private val txtFileInfo: TextView = itemView.findViewById(R.id.txtFileInfo)
        private val txtArchive: TextView = itemView.findViewById(R.id.txtArchive)
        private val txtDelete: TextView = itemView.findViewById(R.id.txtDelete)

        init {
            // Configurar clique no item
            cardView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    (itemView.context as? HistoricoActivity)?.let { activity ->
                        val transfer = activity.adapter.getItemAt(position)
                        transfer?.let { onItemClick(it) }
                    }
                }
            }

            // Configurar botões de ação
            txtArchive.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    (itemView.context as? HistoricoActivity)?.onArchiveClicked(position)
                }
            }

            txtDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    (itemView.context as? HistoricoActivity)?.onDeleteClicked(position)
                }
            }
        }

        fun bind(transfer: TransferHistory) {
            // Definir ícone baseado no tipo de arquivo
            imgIcon.setImageResource(
                when (transfer.fileType) {
                    "Imagem" -> R.drawable.ic_image
                    "Áudio" -> R.drawable.music_file
                    else -> R.drawable.file
                }
            )

            txtFileName.text = transfer.fileName
            txtDevice.text = "Enviado para: ${transfer.deviceName}"
            txtTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(transfer.date)
            txtFileInfo.text = "${transfer.fileType} • ${formatFileSize(transfer.fileSize)}"

            // Mudar cor se estiver arquivado
            if (transfer.status == "Arquivado") {
                cardView.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
                txtFileName.setTextColor(Color.parseColor("#888888"))
            } else {
                cardView.setCardBackgroundColor(Color.WHITE)
                txtFileName.setTextColor(Color.BLACK)
            }
        }

        private fun formatFileSize(size: Long): String {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
                else -> "${size / (1024 * 1024 * 1024)} GB"
            }
        }
    }
}