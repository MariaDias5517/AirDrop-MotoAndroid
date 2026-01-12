package com.example.flash_drop

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnexoActivity : AppCompatActivity() {

    private lateinit var filesContainer: LinearLayout
    private lateinit var btnIniciar: Button
    private lateinit var btnHistoricoImg: ImageButton
    private lateinit var btnSettings: ImageButton

    private val selectedFiles = mutableListOf<Pair<String, Uri>>()
    private val handler = Handler(Looper.getMainLooper())
    private var deviceName = "Dispositivo"
    private var deviceAddress = ""

    private val gson = Gson()
    private val PREFS_NAME = "FlashDropPrefs"
    private val HISTORY_KEY = "transfer_history"

    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileName(it)
            addFileToList(fileName, it)
        }
    }

    private val pickMultipleFilesLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri>? ->
        uris?.forEach { uri ->
            val fileName = getFileName(uri)
            addFileToList(fileName, uri)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.anexo_arquivos)

        deviceName = intent.getStringExtra("deviceName") ?: "Dispositivo"
        deviceAddress = intent.getStringExtra("deviceAddress") ?: ""

        filesContainer = findViewById(R.id.filesContainer)
        btnIniciar = findViewById(R.id.btnIniciar)
        val btnAnexar = findViewById<Button>(R.id.btnAnexar)
        val btnHome = findViewById<ImageButton>(R.id.btnHome)
        btnHistoricoImg = findViewById<ImageButton>(R.id.btnHistoricoImg)
        btnSettings = findViewById<ImageButton>(R.id.btnSettings)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Enviar para: $deviceName"

        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        tvSubtitle.text = "Conectado via Bluetooth"

        btnHome.setOnClickListener {
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        btnHistoricoImg.setOnClickListener {
            goToHistorico()
        }

        btnSettings.setOnClickListener {
            goToSettings()
        }

        btnAnexar.setOnClickListener {
            showFilePickerOptions()
        }

        btnIniciar.setOnClickListener {
            if (selectedFiles.isEmpty()) {
                Toast.makeText(this, "Selecione pelo menos um arquivo", Toast.LENGTH_SHORT).show()
            } else {
                startTransferSimulation()
            }
        }
    }

    private fun showFilePickerOptions() {
        val options = arrayOf("Selecionar um arquivo", "Selecionar múltiplos arquivos")

        MaterialAlertDialogBuilder(this)
            .setTitle("Selecionar arquivos")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickFileLauncher.launch("*/*")
                    1 -> pickMultipleFilesLauncher.launch(arrayOf("*/*"))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun getFileName(uri: Uri): String {
        var name = "Arquivo"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    name = cursor.getString(displayNameIndex)
                }
            }
        }
        return name
    }

    private fun getFileSize(uri: Uri): Long {
        var size = 0L
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
        return size
    }

    private fun getFileType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", ignoreCase = true) ||
                    fileName.endsWith(".jpeg", ignoreCase = true) ||
                    fileName.endsWith(".png", ignoreCase = true) -> "Imagem"
            fileName.endsWith(".mp3", ignoreCase = true) ||
                    fileName.endsWith(".wav", ignoreCase = true) -> "Áudio"
            fileName.endsWith(".mp4", ignoreCase = true) ||
                    fileName.endsWith(".avi", ignoreCase = true) -> "Vídeo"
            fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
            fileName.endsWith(".doc", ignoreCase = true) ||
                    fileName.endsWith(".docx", ignoreCase = true) -> "Documento"
            fileName.endsWith(".txt", ignoreCase = true) -> "Texto"
            else -> "Arquivo"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addFileToList(fileName: String, uri: Uri) {
        val inflater = LayoutInflater.from(this)
        val fileItemView = inflater.inflate(R.layout.item_arquivo, null)

        val tvFileName = fileItemView.findViewById<TextView>(R.id.tvFileName)
        val tvFileSize = fileItemView.findViewById<TextView>(R.id.tvFileSize)
        val btnRemove = fileItemView.findViewById<ImageButton>(R.id.btnRemove)

        val fileSize = getFileSize(uri)
        tvFileName.text = fileName
        tvFileSize.text = formatFileSize(fileSize)

        selectedFiles.add(Pair(fileName, uri))

        btnRemove.setOnClickListener {
            filesContainer.removeView(fileItemView)
            selectedFiles.remove(Pair(fileName, uri))
            Toast.makeText(this, "Arquivo removido", Toast.LENGTH_SHORT).show()
            updateIniciarButton()
        }

        filesContainer.addView(fileItemView)
        updateIniciarButton()

        Toast.makeText(this, "Arquivo adicionado: $fileName", Toast.LENGTH_SHORT).show()
    }

    private fun updateIniciarButton() {
        if (selectedFiles.isNotEmpty()) {
            btnIniciar.isEnabled = true
            btnIniciar.alpha = 1f
            btnIniciar.text = "Iniciar Transferência (${selectedFiles.size})"
        } else {
            btnIniciar.isEnabled = false
            btnIniciar.alpha = 0.5f
            btnIniciar.text = "Iniciar Transferência"
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

    private fun startTransferSimulation() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transferencia, null)
        val tvProgress = dialogView.findViewById<TextView>(R.id.tvProgress)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val tvStatus = dialogView.findViewById<TextView>(R.id.tvStatus)
        val tvCurrentFile = dialogView.findViewById<TextView>(R.id.tvCurrentFile)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        var progress = 0
        var currentFileIndex = 0
        val totalFiles = selectedFiles.size

        tvStatus.text = "Preparando transferência..."
        tvCurrentFile.text = "Aguardando início..."

        val runnable = object : Runnable {
            override fun run() {
                progress += 2

                progressBar.progress = progress
                tvProgress.text = "$progress%"

                when {
                    progress < 20 -> {
                        tvStatus.text = "Conectando ao dispositivo..."
                        tvCurrentFile.text = "Iniciando conexão com $deviceName"
                    }
                    progress < 40 -> {
                        tvStatus.text = "Preparando arquivos..."
                        if (currentFileIndex < totalFiles) {
                            tvCurrentFile.text = "Preparando: ${selectedFiles[currentFileIndex].first}"
                        }
                    }
                    progress < 60 -> {
                        tvStatus.text = "Enviando dados..."
                        if (currentFileIndex < totalFiles) {
                            tvCurrentFile.text = "Enviando: ${selectedFiles[currentFileIndex].first}"
                        }
                    }
                    progress < 80 -> {
                        tvStatus.text = "Verificando integridade..."
                        if (currentFileIndex < totalFiles) {
                            tvCurrentFile.text = "Verificando: ${selectedFiles[currentFileIndex].first}"
                            if (progress % 20 == 0 && currentFileIndex < totalFiles - 1) {
                                currentFileIndex++
                            }
                        }
                    }
                    progress < 100 -> {
                        tvStatus.text = "Finalizando transferência..."
                        tvCurrentFile.text = "Processando finalização..."
                    }
                    progress >= 100 -> {
                        tvStatus.text = "Transferência concluída!"
                        tvCurrentFile.text = "$totalFiles arquivo(s) enviado(s) com sucesso"

                        handler.postDelayed({
                            dialog.dismiss()
                            saveTransferToHistory()
                            showSuccessDialog()
                        }, 1000)
                        return
                    }
                }

                if (progress < 100) {
                    handler.postDelayed(this, 50)
                }
            }
        }

        handler.postDelayed(runnable, 500)
    }

    private fun saveTransferToHistory() {
        // Salvar cada arquivo no histórico
        selectedFiles.forEach { (fileName, uri) ->
            val fileSize = getFileSize(uri)
            val fileType = getFileType(fileName)

            val transfer = TransferHistory(
                deviceName = deviceName,
                deviceAddress = deviceAddress,
                fileName = fileName,
                fileSize = fileSize,
                fileType = fileType
            )

            // Salvar no SharedPreferences
            val historyList = loadHistory()
            historyList.add(0, transfer) // Adicionar no início da lista

            // Limitar histórico aos 50 itens mais recentes
            val limitedList = if (historyList.size > 50) {
                historyList.subList(0, 50)
            } else {
                historyList
            }

            saveHistory(limitedList)
        }
    }

    private fun saveHistory(history: List<TransferHistory>) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = gson.toJson(history)
        prefs.edit().putString(HISTORY_KEY, json).apply()
    }

    private fun loadHistory(): MutableList<TransferHistory> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val json = prefs.getString(HISTORY_KEY, "[]")
        val type = object : TypeToken<MutableList<TransferHistory>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    private fun showSuccessDialog() {
        val totalFiles = selectedFiles.size
        val totalSize = selectedFiles.sumOf { getFileSize(it.second) }

        MaterialAlertDialogBuilder(this)
            .setTitle("✅ Transferência Concluída!")
            .setMessage(
                "Arquivos enviados com sucesso para:\n\n" +
                        "📱 Dispositivo: $deviceName\n" +
                        "📊 Total: $totalFiles arquivo(s)\n" +
                        "💾 Tamanho: ${formatFileSize(totalSize)}\n\n" +
                        "Todos os arquivos foram transferidos com segurança."
            )
            .setPositiveButton("Ver Histórico") { dialog, _ ->
                dialog.dismiss()

                // Limpar e ir para histórico
                selectedFiles.clear()
                filesContainer.removeAllViews()
                updateIniciarButton()

                // Ir para histórico com os dados mais recentes
                goToHistorico()
            }
            .setNegativeButton("Enviar mais arquivos") { dialog, _ ->
                dialog.dismiss()

                // Apenas limpar a lista
                selectedFiles.clear()
                filesContainer.removeAllViews()
                updateIniciarButton()

                Toast.makeText(this, "Pronto para enviar novos arquivos", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun goToHistorico() {
        Toast.makeText(this, "Abrindo Histórico...", Toast.LENGTH_SHORT).show()

        handler.postDelayed({
            val intent = Intent(this, HistoricoActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, 300)
    }

    private fun goToSettings() {
        Toast.makeText(this, "Abrindo Configurações...", Toast.LENGTH_SHORT).show()

        handler.postDelayed({
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }, 300)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}