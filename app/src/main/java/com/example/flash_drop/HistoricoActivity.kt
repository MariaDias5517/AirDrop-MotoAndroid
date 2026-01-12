package com.example.flash_drop

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HistoricoActivity : AppCompatActivity() {

    lateinit var adapter: HistoricoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtSwipeHint: TextView
    private val gson = Gson()
    private val PREFS_NAME = "FlashDropPrefs"
    private val HISTORY_KEY = "transfer_history"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historico)

        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        val btnSettings = findViewById<ImageButton>(R.id.btnSettings)
        txtSwipeHint = findViewById(R.id.txtSwipeHint)

        // Inicializar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewHistorico)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Criar adapter
        adapter = HistoricoAdapter(
            items = emptyList(),
            onItemClick = { transfer ->
                showTransferDetails(transfer)
            },
            onSwipeDelete = { position ->
                showDeleteDialog(position)
            },
            onSwipeArchive = { position ->
                showArchiveDialog(position)
            }
        )

        recyclerView.adapter = adapter

        // Configurar ItemTouchHelper para swipe
        setupSwipeActions()

        // Mostrar dica de swipe se houver itens
        showSwipeHint()

        btnHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        // Carregar histórico
        loadHistory()
    }

    private fun setupSwipeActions() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                when (direction) {
                    ItemTouchHelper.LEFT -> showArchiveDialog(position)
                    ItemTouchHelper.RIGHT -> showDeleteDialog(position)
                }

                // Restaurar a posição do item
                adapter.notifyItemChanged(position)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return 0.5f
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    viewHolder?.itemView?.alpha = 0.7f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showSwipeHint() {
        val history = loadHistoryFromPrefs()
        if (history.isNotEmpty()) {
            txtSwipeHint.visibility = View.VISIBLE
            txtSwipeHint.postDelayed({
                txtSwipeHint.visibility = View.GONE
            }, 5000)
        }
    }

    fun onDeleteClicked(position: Int) {
        showDeleteDialog(position)
    }

    fun onArchiveClicked(position: Int) {
        showArchiveDialog(position)
    }

    private fun showDeleteDialog(position: Int) {
        val transfer = adapter.getItemAt(position)
        if (transfer == null) {
            adapter.notifyItemChanged(position)
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Excluir Transferência")
            .setMessage("Tem certeza que deseja excluir esta transferência do histórico?\n\nArquivo: ${transfer.fileName}")
            .setPositiveButton("Excluir") { dialog, _ ->
                deleteTransfer(position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun showArchiveDialog(position: Int) {
        val transfer = adapter.getItemAt(position)
        if (transfer == null) {
            adapter.notifyItemChanged(position)
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Arquivar Transferência")
            .setMessage("Deseja arquivar esta transferência?\n\nArquivo: ${transfer.fileName}\n\nEla será marcada como arquivada.")
            .setPositiveButton("Arquivar") { dialog, _ ->
                archiveTransfer(position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyItemChanged(position)
            }
            .setNeutralButton("Apenas marcar") { dialog, _ ->
                markAsArchived(position)
                dialog.dismiss()
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun deleteTransfer(position: Int) {
        val transfer = adapter.getItemAt(position)
        if (transfer == null) {
            adapter.notifyItemChanged(position)
            return
        }

        val history = loadHistoryFromPrefs().toMutableList()
        history.removeAll { it.id == transfer.id }

        saveHistoryToPrefs(history)
        adapter.updateItems(history)

        Toast.makeText(this, "Transferência excluída", Toast.LENGTH_SHORT).show()

        if (history.isEmpty()) {
            txtSwipeHint.visibility = View.GONE
        }
    }

    private fun archiveTransfer(position: Int) {
        adapter.archiveItem(position)
        Toast.makeText(this, "Transferência arquivada", Toast.LENGTH_SHORT).show()
    }

    private fun markAsArchived(position: Int) {
        adapter.archiveItem(position)
        Toast.makeText(this, "Marcado como arquivado", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SetTextI18n")
    private fun showTransferDetails(transfer: TransferHistory) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(transfer.date)

        MaterialAlertDialogBuilder(this)
            .setTitle("Detalhes da Transferência")
            .setMessage(
                "📄 Arquivo: ${transfer.fileName}\n" +
                        "📱 Dispositivo: ${transfer.deviceName}\n" +
                        "📍 Endereço: ${transfer.deviceAddress}\n" +
                        "📊 Tamanho: ${formatFileSize(transfer.fileSize)}\n" +
                        "📝 Tipo: ${transfer.fileType}\n" +
                        "🕒 Data: $dateStr\n" +
                        "📌 Status: ${transfer.status}"
            )
            .setPositiveButton("OK", null)
            .setNegativeButton("Compartilhar") { _, _ ->
                shareTransferDetails(transfer)
            }
            .show()
    }

    private fun shareTransferDetails(transfer: TransferHistory) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(transfer.date)

        val shareText = """
            📤 Detalhes da Transferência FlashDrop
            
            📄 Arquivo: ${transfer.fileName}
            📱 Dispositivo: ${transfer.deviceName}
            📊 Tamanho: ${formatFileSize(transfer.fileSize)}
            📝 Tipo: ${transfer.fileType}
            🕒 Data: $dateStr
            
            Enviado via FlashDrop App
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(intent, "Compartilhar detalhes"))
    }

    private fun loadHistory() {
        val historyList = loadHistoryFromPrefs()
        adapter.updateItems(historyList)
    }

    private fun loadHistoryFromPrefs(): List<TransferHistory> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(HISTORY_KEY, "[]") ?: "[]"
        val type = object : TypeToken<List<TransferHistory>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private fun saveHistoryToPrefs(history: List<TransferHistory>) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = gson.toJson(history)
        prefs.edit().putString(HISTORY_KEY, json).apply()
    }

    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}